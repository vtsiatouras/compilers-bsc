import syntaxtree.*;
import visitor.GJDepthFirst;

public class EvalVisitor extends GJDepthFirst<Integer, Integer> {

    /**
    * f0 -> <NUMBER>
    * f1 -> [ TernTail() ]
    */
    public Integer visit(Tern n, Integer argu) {

        Integer rv = Integer.parseInt(n.f0.toString());
        if(n.f1.present()) {
            rv = n.f1.accept(this, rv);
        }
        return rv;
    }

    /**
    * f0 -> "?"
    * f1 -> Tern()
    * f2 -> ":"
    * f3 -> Tern()
    */
    public Integer visit(TernTail n, Integer cond) {

        Integer thenPrt = n.f1.accept(this, null);
        Integer elsePrt = n.f3.accept(this, null);

        return cond != 0 ? thenPrt : elsePrt;
    }

}
