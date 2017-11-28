import syntaxtree.*;
import visitor.GJDepthFirst;

@SuppressWarnings("Duplicates") // Remove IntelliJ warning about duplicate code

public class FirstVisitor extends GJDepthFirst<String, String> {

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    public String visit(Goal n, String argu) throws Exception {
        String _ret = null;
        _ret = n.f0.accept(this, argu);
        _ret = n.f1.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    public String visit(MainClass n, String argu) throws Exception {
        String _ret = n.f1.accept(this, argu);
        System.out.println("Main Class Name: " + _ret);
        return _ret;
    }

    /**
     * f0 -> ClassDeclaration()
     * | ClassExtendsDeclaration()
     */
    public String visit(TypeDeclaration n, String argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    public String visit(ClassDeclaration n, String argu) throws Exception {
        String _ret;
        _ret = n.f1.accept(this, argu);
        System.out.println("Class Name: " + _ret);
        System.out.println("vars:");
        _ret = n.f3.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    public String visit(ClassExtendsDeclaration n, String argu) throws Exception {
        String _ret1, _ret2;
        _ret1 = n.f1.accept(this, argu);
        _ret2 = n.f3.accept(this, argu);
        System.out.println("Class Name: " + _ret1 + "extends "+ _ret2);
        return _ret1;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit(VarDeclaration n, String argu) throws Exception {
        String _ret=null;
//        n.f0.accept(this, argu);
        _ret = n.f1.accept(this, argu);
        System.out.println("    "+_ret);
//        n.f2.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier n, String argu) throws Exception {
        String _ret;
        _ret = n.f0.toString();
        return _ret;
    }
}
