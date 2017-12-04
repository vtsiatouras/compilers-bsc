import syntaxtree.*;
import visitor.GJDepthFirst;

public class EvalVisitor extends GJDepthFirst<Integer, Integer>{

   /**
    * f0 -> Exp()
    * f1 -> <EOF>
    */
    public Integer visit(Goal n, Integer argu){
        return n.f0.accept(this, null);
    }

   /**
    * f0 -> Term()
    * f1 -> [ Exp2() ]
    */
    public Integer visit(Exp n, Integer argu){
    	Integer term = n.f0.accept(this, null);
    	if(n.f1.present())
    	    return n.f1.accept(this, term);
    	else
    	    return term;
    }

   /**
    * f0 -> "+"
    * f1 -> Term()
    * f2 -> [ Exp2() ]
    */
    public Integer visit(PlusExp n, Integer lTerm){
    	Integer rTerm = n.f1.accept(this, null);
    	Integer rv = lTerm + rTerm;
    	if(n.f2.present()){
    	    rv = n.f2.accept(this, rv);
    	}
    	return rv;
    }

   /**
    * f0 -> "-"
    * f1 -> Term()
    * f2 -> [ Exp2() ]
    */
    public Integer visit(MinusExp n, Integer lTerm){
    	Integer rTerm = n.f1.accept(this, null);
    	Integer rv = lTerm - rTerm;
    	if(n.f2.present()){
    	    rv = n.f2.accept(this, rv);
    	}
    	return rv;
    }

   /**
    * f0 -> Factor()
    * f1 -> [ Term2() ]
    */
    public Integer visit(Term n, Integer argu){
    	Integer factor = n.f0.accept(this, null);
    	if(n.f1.present())
    	    return n.f1.accept(this, factor);
    	else
    	    return factor;
    }

   /**
    * f0 -> "*"
    * f1 -> Factor()
    * f2 -> [ Term2() ]
    */
    public Integer visit(TimesExp n, Integer lFactor){
    	Integer rFactor = n.f1.accept(this, null);
    	Integer rv = lFactor * rFactor;
    	if(n.f2.present())
    	    rv = n.f2.accept(this, rv);
    	return rv;
    }

    /**
    * f0 -> "/"
    * f1 -> Factor()
    * f2 -> [ Term2() ]
    */
    public Integer visit(DivExp n, Integer lFactor){
    	Integer rFactor = n.f1.accept(this, null);
    	Integer rv = lFactor / rFactor;
    	if(n.f2.present())
    	    rv = n.f2.accept(this, rv);
    	return rv;
    }

   /**
    * f0 -> <NUMBER>
    */
    public Integer visit(Num n, Integer argu){
	   return Integer.parseInt(n.f0.toString());

    }

   /**
    * f0 -> "("
    * f1 -> Exp()
    * f2 -> ")"
    */
    public Integer visit(ParExp n, Integer argu) {
        return n.f1.accept(this, null);
   }

}
