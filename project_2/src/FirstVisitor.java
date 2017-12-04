import syntaxtree.*;
import visitor.GJDepthFirst;

import java.util.HashMap;
import java.util.Iterator;

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
            throw new Exception("Main class has already been declared!");
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
        curClass.parentClassName = null;

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
    public String visit(ClassExtendsDeclaration n, SymbolTable symbolTable) throws Exception {
        String childClassName, parentClassName;
        childClassName = n.f1.accept(this, symbolTable);
        parentClassName = n.f3.accept(this, symbolTable);
        // Check if the parent class does not exists
        if (!symbolTable.classes.containsKey(parentClassName)) {
            throw new Exception("Class '" + parentClassName + "' has not been declared!");
        }
        // Check if child class was declared before
        if (symbolTable.classes.containsKey(childClassName)) {
            throw new Exception("Class '" + childClassName + "' already declared!");
        }
        // Store child class in the symbol table
        symbolTable.classes.put(childClassName, new SymbolTable.ClassSymTable());
        SymbolTable.ClassSymTable curClass = symbolTable.classes.get(childClassName);
        curClass.className = childClassName;
        curClass.parentClassName = parentClassName;

        // Set up visitor's fields to be aware where to check in the symbol table
        this.currentClassName = childClassName;
        this.classVar = true;
        this.currentFunctionName = null;
        this.functionParam = false;
        this.functionVar = false;

        // Visit VarDeclaration
        String varDecl = n.f5.accept(this, symbolTable);

        // Visit MethodDeclaration
        String funDecl = n.f6.accept(this, symbolTable);

//        System.out.println("Class Name: " + _ret1 + "extends " + _ret2);
        return parentClassName;
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
//        if (this.functionParam) {
//
//        }

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
        String type = n.f1.accept(this, symbolTable);
        String methodName = n.f2.accept(this, symbolTable);
        // If method declared already inside the class
        SymbolTable.ClassSymTable curClass = symbolTable.classes.get(this.currentClassName);
        if (curClass.methods.containsKey(methodName)) {
            throw new ParseException("Method '" + methodName + "' already declared!");
        }

        // Store method to the symbol table
        curClass.methods.put(methodName, new SymbolTable.MethodSymTable());
        SymbolTable.MethodSymTable curMethod = curClass.methods.get(methodName);
        curMethod.methodName = methodName;
        curMethod.returnType = type;

        // Set up visitor's fields to be aware where to check in the symbol table
        this.classVar = false;
        this.currentFunctionName = methodName;
        this.functionParam = true;
        this.functionVar = false;
        // Visit ParameterList
        String params = n.f4.accept(this, symbolTable);

        // If the method's class is extended of another class
        if (curClass.parentClassName != null) {
            SymbolTable.ClassSymTable parentClass = symbolTable.classes.get(curClass.parentClassName);
            // If the method with the same name was declared in parent class
            // Then OVERRIDE is only allowed and NOT overloading
            if (parentClass.methods.containsKey(methodName)) {
                // Now must be compared return types, and parameters. They must be the exact same!
                SymbolTable.MethodSymTable parentMethod = parentClass.methods.get(methodName);
                // Compare types
                if (!parentMethod.returnType.equals(curMethod.returnType)) {
                    throw new Exception("Method '" + curMethod.methodName + "' is type of '" + curMethod.returnType + "'. Declared before at parent class '" + parentClass.parentClassName + "' with type of '" + parentMethod.returnType + "'");
                }
                // Compare number of parameters
                if (curMethod.parameters.size() != parentMethod.parameters.size()){
                    throw new Exception("Method '" + curMethod.methodName + "' in class '"+curClass.className + "' can't override because it has different parameters");
                }
                // Compare the type of the parameters
                Iterator iterator1 = curMethod.parameters.keySet().iterator();
                Iterator iterator2 = parentMethod.parameters.keySet().iterator();
                while (iterator1.hasNext() && iterator2.hasNext()) {
                    String key = iterator1.next().toString();
                    String value1 = curMethod.parameters.get(key);
                    key = iterator2.next().toString();
                    String value2 = parentMethod.parameters.get(key);
                    if (!value1.equals(value2)) {
                        throw new Exception("Method '" + curMethod.methodName + "' in class '" + curClass.className + "' can't override because it has different parameters");
                    }
                }
            }
        }

        // Set up visitor's fields to be aware where to check in the symbol table
        this.classVar = false;
        this.currentFunctionName = methodName;
        this.functionParam = false;
        this.functionVar = true;
        // Visit VarDeclaration
        String vars = n.f7.accept(this, symbolTable);

        return methodName;
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
