import os, re, shutil, sys
ROOT=os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)),".."))
SRC=os.path.join(ROOT,"src/main/java")
OUT=os.path.join(ROOT,"verification/.build/src")
shutil.rmtree(OUT, ignore_errors=True)
EXCLUDE={"ParikshaGuruApplication.java","SecurityConfig.java","JwtAuthFilter.java","JwtService.java","GlobalExceptionHandler.java","AppConfig.java","AppProperties.java"}
KEEP_IMPORT_PREFIX=("com.parikshaguru","java.","org.springframework.data.domain","org.springframework.data.jpa.repository",
  "org.slf4j","org.springframework.http.HttpStatus","org.springframework.security.crypto.password","org.springframework.boot.CommandLineRunner",
  "jakarta.servlet.http.HttpServletRequest","org.springframework.web.context.request")

def strip_annotations(s):
    out=[];i=0;n=len(s)
    def skip_parens(i):
        depth=0
        while i<n:
            c=s[i]
            if s.startswith('"""',i):
                j=s.find('"""',i+3); i=j+3; continue
            if c=='"':
                i+=1
                while s[i]!='"':
                    i+=2 if s[i]=='\\' else 1
                i+=1; continue
            if c=="'":
                i+=1
                while s[i]!="'":
                    i+=2 if s[i]=='\\' else 1
                i+=1; continue
            if c=='(': depth+=1
            elif c==')':
                depth-=1
                if depth==0: return i+1
            i+=1
        return i
    while i<n:
        c=s[i]
        if s.startswith('//',i):
            j=s.find('\n',i); j=n if j<0 else j; out.append(s[i:j]); i=j; continue
        if s.startswith('/*',i):
            j=s.find('*/',i)+2; out.append(s[i:j]); i=j; continue
        if s.startswith('"""',i):
            j=s.find('"""',i+3)+3; out.append(s[i:j]); i=j; continue
        if c=='"':
            j=i+1
            while s[j]!='"': j+=2 if s[j]=='\\' else 1
            out.append(s[i:j+1]); i=j+1; continue
        if c=="'":
            j=i+1
            while s[j]!="'": j+=2 if s[j]=='\\' else 1
            out.append(s[i:j+1]); i=j+1; continue
        if c=='@' and not s.startswith('@interface',i):
            m=re.match(r'@\s*[A-Za-z_][\w.]*',s[i:])
            if m:
                j=i+m.end()
                k=j
                while k<n and s[k] in ' \t\r\n': k+=1
                if k<n and s[k]=='(': j=skip_parens(k)
                out.append(' '); i=j; continue
        out.append(c); i+=1
    return ''.join(out)

FIELD=re.compile(r'^\s*private\s+(?!static)(final\s+)?([\w<>,.? \[\]]+?)\s+(\w+)\s*(=[^;]*)?;',re.M)
def delombok(orig, s):
    if '@Getter' not in orig and '@Setter' not in orig: return s
    cls=re.search(r'(?:public\s+)?(?:abstract\s+)?class\s+(\w+)',s).group(1)
    gen=[]
    for m in FIELD.finditer(s):
        fin,typ,name=m.group(1),m.group(2).strip(),m.group(3)
        cap=name[0].upper()+name[1:]
        g=('is' if typ=='boolean' else 'get')+cap
        gen.append(f"public {typ} {g}() {{ return this.{name}; }}")
        if not fin: gen.append(f"public void set{cap}({typ} v) {{ this.{name}=v; }}")
    gen.append(f"public {cls}() {{}}")
    idx=s.rstrip().rfind('}')
    return s[:idx]+"\n"+"\n".join(gen)+"\n}\n"

def fix_imports(s):
    res=[]
    for line in s.split('\n'):
        m=re.match(r'\s*import\s+(static\s+)?([\w.*]+);',line)
        if m and not m.group(2).startswith(KEEP_IMPORT_PREFIX): continue
        res.append(line)
    return '\n'.join(res)

for root,_,files in os.walk(SRC):
    for f in files:
        if not f.endswith('.java') or f in EXCLUDE: continue
        p=os.path.join(root,f); rel=os.path.relpath(p,SRC)
        orig=open(p).read()
        s=strip_annotations(orig)
        s=delombok(orig,s)
        if '@RequiredArgsConstructor' in orig: s=s.replace('private final ','private ')
        s=fix_imports(s)
        o=os.path.join(OUT,rel); os.makedirs(os.path.dirname(o),exist_ok=True); open(o,'w').write(s)
print("prepared")
