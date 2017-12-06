import syntaxtree.*;
import visitor.GJDepthFirst;

@SuppressWarnings("Duplicates") // Remove IntelliJ warning about duplicate code

public class SecondVisitor extends GJDepthFirst<String, SymbolTable> {

    private String currentClassName;
    private String currentFunctionName;
    private Boolean classVar;
    private Boolean functionVar;
    private String exprType;
    private Boolean returnPrimaryExpr;

    public String look_up_identifier(String identifier, SymbolTable symbolTable) throws Exception {
        // Lookup if this identifier is declared before
        SymbolTable.ClassSymTable curClass = symbolTable.classes.get(this.currentClassName);
        SymbolTable.MethodSymTable curMethod = curClass.methods.get(this.currentFunctionName);
        // If you find it one of the below cases, return its type
        // Check if this identifier is a parameter or a variable
        if (curMethod.parameters.containsKey(identifier)) {
            return curMethod.parameters.get(identifier);
        }
        if (curMethod.variables.containsKey(identifier)) {
            return curMethod.variables.get(identifier);
        }
        // Check if it is field in the class
        if (curClass.fields.containsKey(identifier)) {
            return curClass.fields.get(identifier);
        }
        // Check if it has parent class with this field
        while (curClass.parentClassName != null) {
            SymbolTable.ClassSymTable parentClass = symbolTable.classes.get(curClass.parentClassName);
            if (parentClass.fields.containsKey(identifier)) {
                return parentClass.fields.get(identifier);
            }
            curClass = parentClass;
        }
        // If you are here then this identifier was not found...
        return null;
//        throw new Exception("Unknown symbol '" + identifier + "'");
    }

    public String look_up_methods(String methodName, String className, SymbolTable symbolTable) throws Exception {
        SymbolTable.ClassSymTable classSym = symbolTable.classes.get(className);
        if (classSym.methods.containsKey(methodName)) {
            return classSym.methods.get(methodName).returnType;

        }
        // Check if it has parent class with this method
        while (classSym.parentClassName != null) {
            SymbolTable.ClassSymTable parentClass = symbolTable.classes.get(classSym.parentClassName);
            if (parentClass.methods.containsKey(methodName)) {
                return parentClass.methods.get(methodName).returnType;
            }
            classSym = parentClass;
        }
        return null;
//        throw new Exception("Unknown method '" + methodName + "'");
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
    public String visit(MainClass n, SymbolTable symbolTable) throws Exception {
        this.currentClassName = n.f1.accept(this, symbolTable);
        this.currentFunctionName = "main";
        this.classVar = false;
        this.functionVar = true;
        // Visit Statement
        n.f15.accept(this, symbolTable);
        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    public String visit(ClassDeclaration n, SymbolTable symbolTable) throws Exception {
        this.currentClassName = n.f1.accept(this, symbolTable);
        this.classVar = false;
        this.functionVar = true;
        // Visit MethodDeclaration
        n.f4.accept(this, symbolTable);
        return null;
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
    public String visit(ClassExtendsDeclaration n, SymbolTable symbolTable) throws Exception {
        this.currentClassName = n.f1.accept(this, symbolTable);
        this.classVar = false;
        this.functionVar = true;
        // Visit MethodDeclaration
        n.f6.accept(this, symbolTable);
        return null;
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    public String visit(MethodDeclaration n, SymbolTable symbolTable) throws Exception {
        this.currentFunctionName = n.f2.accept(this, symbolTable);
        this.classVar = false;
        this.functionVar = true;
        // Visit Statement
        n.f8.accept(this, symbolTable);
        return null;
    }

    /**
     * f0 -> Block()
     * | AssignmentStatement()
     * | ArrayAssignmentStatement()
     * | IfStatement()
     * | WhileStatement()
     * | PrintStatement()
     */
    public String visit(Statement n, SymbolTable symbolTable) throws Exception {
        return n.f0.accept(this, symbolTable);
    }

    /**
     * f0 -> "{"
     * f1 -> ( Statement() )*
     * f2 -> "}"
     */
    public String visit(Block n, SymbolTable symbolTable) throws Exception {
        String _ret = null;
        n.f0.accept(this, symbolTable);
        n.f1.accept(this, symbolTable);
        n.f2.accept(this, symbolTable);
        return _ret;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    public String visit(AssignmentStatement n, SymbolTable symbolTable) throws Exception {
        String identifier = n.f0.accept(this, symbolTable);
        String type = look_up_identifier(identifier, symbolTable);
        System.out.println(identifier + " " + type);
        this.ExprType = type;
//        n.f0.accept(this, symbolTable);
//        n.f1.accept(this, symbolTable);
        n.f2.accept(this, symbolTable);
//        n.f3.accept(this, symbolTable);

        return null;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
     */
    public String visit(ArrayAssignmentStatement n, SymbolTable symbolTable) throws Exception {
        String _ret = null;
        n.f0.accept(this, symbolTable);
        n.f1.accept(this, symbolTable);
        n.f2.accept(this, symbolTable);
        n.f3.accept(this, symbolTable);
        n.f4.accept(this, symbolTable);
        n.f5.accept(this, symbolTable);
        n.f6.accept(this, symbolTable);
        return _ret;
    }

    /**
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     */
    public String visit(IfStatement n, SymbolTable symbolTable) throws Exception {
        String _ret = null;
        n.f0.accept(this, symbolTable);
        n.f1.accept(this, symbolTable);
        n.f2.accept(this, symbolTable);
        n.f3.accept(this, symbolTable);
        n.f4.accept(this, symbolTable);
        n.f5.accept(this, symbolTable);
        n.f6.accept(this, symbolTable);
        return _ret;
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    public String visit(WhileStatement n, SymbolTable symbolTable) throws Exception {
        String _ret = null;
        n.f0.accept(this, symbolTable);
        n.f1.accept(this, symbolTable);
        n.f2.accept(this, symbolTable);
        n.f3.accept(this, symbolTable);
        n.f4.accept(this, symbolTable);
        return _ret;
    }

    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    public String visit(PrintStatement n, SymbolTable symbolTable) throws Exception {
        String _ret = null;
        n.f0.accept(this, symbolTable);
        n.f1.accept(this, symbolTable);
        n.f2.accept(this, symbolTable);
        n.f3.accept(this, symbolTable);
        n.f4.accept(this, symbolTable);
        return _ret;
    }

    /**
     * f0 -> AndExpression()
     * | CompareExpression()
     * | PlusExpression()
     * | MinusExpression()
     * | TimesExpression()
     * | ArrayLookup()
     * | ArrayLength()
     * | MessageSend()
     * | Clause()
     */
    public String visit(Expression n, SymbolTable symbolTable) throws Exception {
        return n.f0.accept(this, symbolTable);
    }

    /**
     * f0 -> Clause()
     * f1 -> "&&"
     * f2 -> Clause()
     */
    public String visit(AndExpression n, SymbolTable symbolTable) throws Exception {
        String _ret = null;
        n.f0.accept(this, symbolTable);
        n.f1.accept(this, symbolTable);
        n.f2.accept(this, symbolTable);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public String visit(CompareExpression n, SymbolTable symbolTable) throws Exception {
        String _ret = null;
        n.f0.accept(this, symbolTable);
        n.f1.accept(this, symbolTable);
        n.f2.accept(this, symbolTable);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression n, SymbolTable symbolTable) throws Exception {
        String _ret = null;
        n.f0.accept(this, symbolTable);
        n.f1.accept(this, symbolTable);
        n.f2.accept(this, symbolTable);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression n, SymbolTable symbolTable) throws Exception {
        String _ret = null;
        n.f0.accept(this, symbolTable);
        n.f1.accept(this, symbolTable);
        n.f2.accept(this, symbolTable);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression n, SymbolTable symbolTable) throws Exception {
        String _ret = null;
        n.f0.accept(this, symbolTable);
        n.f1.accept(this, symbolTable);
        n.f2.accept(this, symbolTable);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public String visit(ArrayLookup n, SymbolTable symbolTable) throws Exception {
        String _ret = null;
        n.f0.accept(this, symbolTable);
        n.f1.accept(this, symbolTable);
        n.f2.accept(this, symbolTable);
        n.f3.accept(this, symbolTable);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public String visit(ArrayLength n, SymbolTable symbolTable) throws Exception {
        String _ret = null;
        n.f0.accept(this, symbolTable);
        n.f1.accept(this, symbolTable);
        n.f2.accept(this, symbolTable);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    public String visit(MessageSend n, SymbolTable symbolTable) throws Exception {
        String _ret = null;
        n.f0.accept(this, symbolTable);
        n.f1.accept(this, symbolTable);
        n.f2.accept(this, symbolTable);
        n.f3.accept(this, symbolTable);
        n.f4.accept(this, symbolTable);
        n.f5.accept(this, symbolTable);
        return _ret;
    }

    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    public String visit(ExpressionList n, SymbolTable symbolTable) throws Exception {
        String _ret = null;
        n.f0.accept(this, symbolTable);
        n.f1.accept(this, symbolTable);
        return _ret;
    }

    /**
     * f0 -> ( ExpressionTerm() )*
     */
    public String visit(ExpressionTail n, SymbolTable symbolTable) throws Exception {
        return n.f0.accept(this, symbolTable);
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    public String visit(ExpressionTerm n, SymbolTable symbolTable) throws Exception {
        String _ret = null;
        n.f0.accept(this, symbolTable);
        n.f1.accept(this, symbolTable);
        return _ret;
    }

    /**
     * f0 -> NotExpression()
     * | PrimaryExpression()
     */
    public String visit(Clause n, SymbolTable symbolTable) throws Exception {
        return n.f0.accept(this, symbolTable);
    }

    /**
     * f0 -> IntegerLiteral()
     * | TrueLiteral()
     * | FalseLiteral()
     * | Identifier()
     * | ThisExpression()
     * | ArrayAllocationExpression()
     * | AllocationExpression()
     * | BracketExpression()
     */
    public String visit(PrimaryExpression n, SymbolTable symbolTable) throws Exception {
        String expression = n.f0.accept(this, symbolTable);
        if (expression != null) {
            if (expression.equals("true") || expression.equals("false")) {
                if (!this.ExprType.equals("boolean")) {
                    throw new Exception("Operations between 'boolean' and '"+this.ExprType+"' are not permitted");
                }
            }
            else {
                String type = look_up_identifier(expression, symbolTable);
                if (!this.ExprType.equals(this.ExprType)) {
                    throw new Exception("Operations between '"+type+ "' and '"+this.ExprType+"' are not permitted");
                }
            }
        }
        return null;
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public String visit(IntegerLiteral n, SymbolTable symbolTable) throws Exception {
        return n.f0.accept(this, symbolTable);
    }

    /**
     * f0 -> "true"
     */
    public String visit(TrueLiteral n, SymbolTable symbolTable) throws Exception {
        return "true";
    }

    /**
     * f0 -> "false"
     */
    public String visit(FalseLiteral n, SymbolTable symbolTable) throws Exception {
        return "false";
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier n, SymbolTable symbolTable) throws Exception {
        return n.f0.toString();
    }

}
