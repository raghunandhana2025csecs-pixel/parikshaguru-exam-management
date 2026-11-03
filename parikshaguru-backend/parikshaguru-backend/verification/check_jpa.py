import re, os, glob
J="src/main/java/com/parikshaguru"
def snake(n): return re.sub(r'([a-z0-9])([A-Z])',r'\1_\2',n).lower()
# ---------- parse entities ----------
ents={}
def parse_entity(path):
    s=open(path).read()
    m=re.search(r'class\s+(\w+)(?:\s+extends\s+(\w+))?',s); name,parent=m.group(1),m.group(2)
    t=re.search(r'@Table\(\s*name\s*=\s*"(\w+)"',s)
    fields={}; cols={}
    # field: collect annotation block preceding "private Type name"
    for fm in re.finditer(r'((?:\s*@[\w.]+(?:\((?:[^()]|\([^()]*\))*\))?)*)\s*private\s+(?!static)(?:final\s+)?([\w<>,.? ]+?)\s+(\w+)\s*(?:=[^;]*)?;',s):
        ann,typ,fn=fm.groups()
        if '@OneToMany' in ann or '@ManyToMany' in ann: fields[fn]=(typ,None); continue
        col=None
        jc=re.search(r'@JoinColumn\(\s*name\s*=\s*"(\w+)"',ann); c=re.search(r'@Column\([^)]*?name\s*=\s*"(\w+)"',ann)
        col=jc.group(1) if jc else (c.group(1) if c else snake(fn))
        fields[fn]=(typ,col)
    return dict(name=name,parent=parent,table=t.group(1) if t else None,fields=fields,src=s)
for p in glob.glob(J+"/entity/*.java"):
    e=parse_entity(p); ents[e['name']]=e
base=ents.pop('BaseEntity')
for e in ents.values():
    e['fields'].update(base['fields'])
print("entities:",len(ents))
# ---------- parse schema ----------
sql=open("database/schema.sql").read()
tables={}
order=[]
for m in re.finditer(r'CREATE TABLE IF NOT EXISTS (\w+) \((.*?)\) ENGINE',sql,re.S):
    name,body=m.groups(); order.append(name)
    cols=set(re.findall(r'(?:^|,)\s*(\w+)\s+(?:BIGINT|VARCHAR|INT|DOUBLE|TEXT|DATETIME|BIT)\b',body,re.M))
    refs=re.findall(r'REFERENCES (\w+)\(',body)
    keys=re.findall(r'KEY \w+ \(([^)]*)\)',body)
    tables[name]=dict(cols=cols,refs=refs,keys=keys)
print("sql tables:",len(tables))
errs=[]
for e in ents.values():
    t=e['table']
    if t not in tables: errs.append(f"table {t} missing in schema"); continue
    entcols={c for (ty,c) in e['fields'].values() if c}
    if entcols!=tables[t]['cols']: errs.append(f"{t}: entity-only={sorted(entcols-tables[t]['cols'])} sql-only={sorted(tables[t]['cols']-entcols)}")
    # unique / index columns exist
    for cl in re.findall(r'columnNames\s*=\s*(?:\{([^}]*)\}|"([^"]*)")',e['src']):
        for c in re.findall(r'"(\w+)"',cl[0] or '"'+cl[1]+'"'):
            if c not in tables[t]['cols']: errs.append(f"{t}: unique column {c} not in sql")
    for cl in re.findall(r'columnList\s*=\s*"([^"]*)"',e['src']):
        for c in [x.strip() for x in cl.split(',')]:
            if c not in tables[t]['cols']: errs.append(f"{t}: index column {c} not in sql")
tabs={e['table'] for e in ents.values()}|{'subject_faculty'}
if set(tables)!=tabs: errs.append(f"table set differs: {set(tables)^tabs}")
# FK ordering
seen=set()
for t in order:
    for r in tables[t]['refs']:
        if r not in seen and r!=t: errs.append(f"{t} references {r} before it is created")
    seen.add(t)
print("SQL/entity problems:",errs or "none")
# ---------- derived query names & JPQL ----------
ent_by_name=ents
def resolve_path(entity,path):
    """path like ['exam','id'] ; returns error or None"""
    cur=entity
    for i,seg in enumerate(path):
        if cur is None: return f"cannot go past scalar at {seg}"
        if seg=='id' : return None
        f=ents[cur]['fields'].get(seg)
        if not f: return f"no property '{seg}' on {cur}"
        ty=f[0]; inner=re.sub(r'.*<(\w+)>.*',r'\1',ty)
        cur=inner if inner in ents else None
    return None
rep_errs=[]
NQ=0
for p in glob.glob(J+"/repository/*Repository.java"):
    s=open(p).read(); rn=os.path.basename(p)[:-5]
    m=re.search(r'JpaRepository<(\w+),',s)
    if not m: continue
    E=m.group(1)
    # derived
    for dm in re.finditer(r'(?:^|\s)(?:[\w<>,\[\] ?]+?)\s+((?:find|exists|count|delete)By\w+)\(',s,re.M):
        name=dm.group(1)
        # skip if a @Query precedes this method
        pre=s[:dm.start(1)]
        if '@Query' in pre.split(';')[-1]: continue
        body=re.sub(r'^(find|exists|count|delete)By','',name); body=re.sub(r'OrderBy.*$','',body)
        for part in re.split(r'And|Or',body):
            part=re.sub(r'(IgnoreCase|In|True|False|Not|IsNull)$','',part)
            # split camel into segments and resolve greedily
            segs=re.findall(r'[A-Z][a-z0-9]*',part)
            path=[];cur=E;i=0;ok=True
            while i<len(segs):
                matched=False
                for j in range(len(segs),i,-1):
                    cand=''.join(segs[i:j]); cand=cand[0].lower()+cand[1:]
                    f=ents[cur]['fields'].get(cand) if cur else None
                    if f:
                        ty=f[0]; inner=re.sub(r'.*<(\w+)>.*',r'\1',ty)
                        cur=inner if inner in ents else None; i=j; matched=True; break
                if not matched: ok=False; break
            if not ok: rep_errs.append(f"{rn}.{name}: cannot resolve '{part}' on {E}")
    # JPQL
    for qm in re.finditer(r'@Query\(\s*(?:"""(.*?)"""|((?:"[^"]*"\s*\+?\s*)+(?:SCOPE)?))',s,re.S):
        q=qm.group(1) or qm.group(2)
        q=re.sub(r'"\s*\+\s*SCOPE','"'+re.search(r'SCOPE\s*=\s*"([^"]*)"',s).group(1)+'"',q) if 'SCOPE' in q and re.search(r'SCOPE\s*=\s*"',s) else q
        q=' '.join(re.findall(r'"([^"]*)"',q)) if not qm.group(1) else qm.group(1)
        if re.search(r'\bSCOPE\b',qm.group(0)) and re.search(r'SCOPE\s*=\s*"([^"]*)"',s): q+=' '+re.search(r'SCOPE\s*=\s*"([^"]*)"',s).group(1)
        aliases={}
        NQ+=1
        for am in re.finditer(r'\bfrom\s+(\w+)\s+(\w+)',q): aliases[am.group(2)]=am.group(1)
        # joins: iterate to fixpoint
        for _ in range(3):
            for jm in re.finditer(r'\b(?:left\s+)?(?:join|fetch)\s+(?:fetch\s+)?(\w+)\.(\w+)\s+(\w+)',q):
                a,prop,al=jm.groups()
                if a in aliases and aliases[a] in ents:
                    f=ents[aliases[a]]['fields'].get(prop)
                    if f: aliases[al]=re.sub(r'.*<(\w+)>.*',r'\1',f[0])
                    else: rep_errs.append(f"{rn} JPQL: no property {prop} on {aliases[a]}")
            for fm in re.finditer(r'\bjoin\s+fetch\s+(\w+)\.(\w+)\s+(\w+)',q): pass
        for pm in re.finditer(r'\b([a-z]\w*)\.([a-z]\w*(?:\.[a-z]\w*)*)',q):
            al,path=pm.group(1),pm.group(2).split('.')
            if al in aliases and aliases[al] in ents:
                r_=resolve_path(aliases[al],path)
                if r_: rep_errs.append(f"{rn} JPQL '{al}.{pm.group(2)}': {r_}")
print("JPQL queries parsed:",NQ)
print("repository/JPQL problems:",rep_errs or "none")
