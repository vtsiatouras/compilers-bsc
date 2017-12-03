import syntaxtree.*;
import visitor.GJDepthFirst;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("Duplicates") // Remove IntelliJ warning about duplicate code

public class FirstVisitor extends GJDepthFirst<String, SymbolTable> {

    public String currentClassName;
    public String currentFunctionName;
    public Boolean classVar;
    public Boolean functionParam;
    public Boolean functionVar;

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    public String visit(Goal n, SymbolTable symbolTable) throws Exception {
        String _ret = null;
        _ret = n.f0.accept(this, symbolTable);
        _ret = n.f1.accept(this, symbolTable);
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
    public String visit(MainClass n, SymbolTable symbolTable) throws Exception {
        String mainClassName = n.f1.accept(this, symbolTable);
        // Check if class was declared before
        if (symbolTable.classes.containsKey(mainClassName)) {
            throw new Exception("Main class already declared!");
        }
        // Store class in the symbol table
        symbolTable.classes.put(mainClassName, new SymbolTable.ClassSymTable());
        // Set up visitor's fields to be aware where to check in the symbol table
        this.currentClassName = mainClassName;
        this.classVar = true;
        this.currentFunctionName = null;
        this.functionParam = false;
        this.functionVar = false;

        return mainClassName;
    }

    /**
     * f0 -> ClassDeclaration()
     * | ClassExtendsDeclaration()
     */
    public String visit(TypeDeclaration n, SymbolTable symbolTable) throws Exception {
        return n.f0.accept(this, symbolTable);
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
        String className;
        className = n.f1.accept(this, symbolTable);
        // Check if class was declared before
        if (symbolTable.classes.containsKey(className)) {
            throw new Exception("Class '" + className + "' already declared!");
        }
        // Store class in the symbol table
        symbolTable.classes.put(className, new SymbolTable.ClassSymTable());
        SymbolTable.ClassSymTable curClass = symbolTable.classes.get(className);
        curClass.className = className;

        // Set up visitor's fields to be aware where to check in the symbol table
        this.currentClassName = className;
        this.classVar = true;
        this.currentFunctionName = null;
        this.functionParam = false;
        this.functionVar = false;

        // Visit VarDeclaration
        String varDecl = n.f3.accept(this, symbolTable);

        // Visit MethodDeclaration
        String funDecl = n.f4.accept(this, symbolTable);

        return varDecl;
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
    //todo!!
    public String visit(ClassExtendsDeclaration n, SymbolTable symbolTable) throws Exception {
        String _ret1, _ret2;
        _ret1 = n.f1.accept(this, symbolTable);
        _ret2 = n.f3.accept(this, symbolTable);
        System.out.println("Class Name: " + _ret1 + "extends " + _ret2);
        return _ret1;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit(VarDeclaration n, SymbolTable symbolTable) throws Exception {
        String type = n.f0.accept(this, symbolTable);
        String identifier = n.f1.accept(this, symbolTable);

        // Class variable
        if (this.classVar) {
            SymbolTable.ClassSymTable curClass = symbolTable.classes.get(this.currentClassName);
            if (curClass.fields.containsKey(identifier)) {
                throw new Exception("Field '" + identifier + "' already declared!");
            }
            curClass.fields.put(identifier, type);
        }

        // todo auto den xreiazetai telika!
        // Method parameter
        if (this.functionParam) {

        }

        // Method variable
        if (this.functionVar) {
            SymbolTable.ClassSymTable curClass = symbolTable.classes.get(this.currentClassName);
            SymbolTable.MethodSymTable curMethod = curClass.methods.get(this.currentFunctionName);
            if (curMethod.variables.containsKey(identifier)) {
                throw new Exception("Variable '" + identifier + "' already declared!");
            }
            curMethod.variables.put(identifier, type);


        }

        return identifier;
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
        String identifier = n.f2.accept(this, symbolTable);
        SymbolTable.ClassSymTable curClass = symbolTable.classes.get(this.currentClassName);
        if (curClass.methods.containsKey(identifier)) {
            throw new Exception("Method '" + identifier + "' already declared!");
        }
        curClass.methods.put(identifier, new SymbolTable.MethodSymTable());

        // Set up visitor's fields to be aware where to check in the symbol table
        this.classVar = false;
        this.currentFunctionName = identifier;
        this.functionParam = true;
        this.functionVar = false;
        // Visit ParameterList
        String params = n.f4.accept(this, symbolTable);

        // Set up visitor's fields to be aware where to check in the symbol table
        this.classVar = false;
        this.currentFunctionName = identifier;
        this.functionParam = false;
        this.functionVar = true;
        // Visit VarDeclaration
        String vars = n.f7.accept(this, symbolTable);

        return identifier;
    }


    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    public String visit(FormalParameter n, SymbolTable symbolTable) throws Exception {
        String type = n.f0.accept(this, symbolTable);
        String identifier = n.f1.accept(this, symbolTable);
        SymbolTable.ClassSymTable curClass = symbolTable.classes.get(this.currentClassName);
        SymbolTable.MethodSymTable curMethod = curClass.methods.get(this.currentFunctionName);
        if (curMethod.parameters.containsKey(identifier)) {
            throw new Exception("Parameter '" + identifier + "' already declared!");
        }
        curMethod.parameters.put(identifier, type);
        return type;
    }

    /**
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
     */
    public String visit(ArrayType n, SymbolTable symbolTable) throws Exception {
        return "int[]";
    }

    /**
     * f0 -> "boolean"
     */
    public String visit(BooleanType n, SymbolTable symbolTable) throws Exception {
        return "boolean";
    }

    /**
     * f0 -> "int"
     */
    public String visit(IntegerType n, SymbolTable symbolTable) throws Exception {
        return "int";
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier n, SymbolTable symbolTable) throws Exception {
        return n.f0.toString();
    }
}
//todo na tsekarw kapoia axrhsta return
