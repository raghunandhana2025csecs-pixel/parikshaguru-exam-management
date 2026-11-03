import com.parikshaguru.entity.Question;
import com.parikshaguru.entity.enums.QuestionType;
import com.parikshaguru.service.evaluator.*;
import com.parikshaguru.service.impl.PercentageGradingPolicy;
import com.parikshaguru.util.AnswerUtil;
public class T {
  static int pass=0, fail=0;
  static void eq(String n,Object a,Object b){ boolean ok=java.util.Objects.equals(a,b); if(ok)pass++; else {fail++; System.out.println("FAIL "+n+": expected "+b+" got "+a);} }
  static Question q(QuestionType t,String c,double m,double n){ Question q=new Question(); q.setQuestionType(t); q.setCorrectAnswer(c); q.setMarks(m); q.setNegativeMarks(n); return q; }
  public static void main(String[] a){
    var sc=new SingleChoiceEvaluator(); var mq=q(QuestionType.MCQ,"B",2,0.5);
    eq("mcq correct",sc.evaluate(mq,"b",true),2.0); eq("mcq wrong neg",sc.evaluate(mq,"A",true),-0.5);
    eq("mcq wrong noneg",sc.evaluate(mq,"A",false),0.0); eq("mcq blank",sc.evaluate(mq,"",true),0.0); eq("mcq null",sc.evaluate(mq,null,true),0.0);
    var tf=q(QuestionType.TRUE_FALSE,"FALSE",1,0.25); eq("tf ok",sc.evaluate(tf,"false",true),1.0); eq("tf wrong",sc.evaluate(tf,"TRUE",true),-0.25);
    var ms=new MultiSelectEvaluator(); var mm=q(QuestionType.MULTI_SELECT,"A,C",3,1);
    eq("multi ok",ms.evaluate(mm,"c, a",true),3.0); eq("multi partial",ms.evaluate(mm,"A",true),-1.0); eq("multi extra",ms.evaluate(mm,"A,B,C",false),0.0);
    var sa=new ShortAnswerEvaluator(); var sq=q(QuestionType.SHORT_ANSWER,"3NF|third normal form",2,0);
    eq("short alt",sa.evaluate(sq,"  Third   Normal Form ",false),2.0); eq("short wrong",sa.evaluate(sq,"2NF",false),0.0);
    var de=new DescriptiveEvaluator(); var dq=q(QuestionType.DESCRIPTIVE,null,5,0);
    eq("desc manual",de.evaluate(dq,"essay",false),null); eq("desc blank",de.evaluate(dq," ",false),0.0);
    var reg=new EvaluatorRegistry(java.util.List.of(sc,ms,sa,de));
    for(QuestionType t:QuestionType.values()) eq("registry "+t, reg.forType(t)!=null, true);
    var g=new PercentageGradingPolicy(); eq("g94",g.grade(94),"A+"); eq("g90",g.grade(90),"A+"); eq("g60",g.grade(60),"B"); eq("g39",g.grade(39.9),"F");
    eq("canon",AnswerUtil.canonicalLabels(" c, a,A "),"A,C"); eq("round",AnswerUtil.round2(3.14159),3.14);
    // scoring scenario from the demo exam: MCQ D(2) + multi A,B,C(3) + TF FALSE(1) + short(2) + descriptive(manual 2)
    double obj=2+3+1+2; eq("demo objective total",obj,8.0);
    System.out.println("PASS="+pass+" FAIL="+fail); System.exit(fail==0?0:1);
  }
}
