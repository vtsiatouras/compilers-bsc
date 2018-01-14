import syntaxtree.*;
import visitor.GJDepthFirst;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("Duplicates") // Remove IntelliJ warning about duplicate code

public class LLVMGenerateVisitor extends GJDepthFirst<String, String> {
    private VTables vTables;
    private SymbolTable symbolTable;
    private String fileName;
    private File fileptr;
    private int register;
    private int loopLabel;
    private int ifLabel;
    private int andLabel;
    private int boundsLabel;
    private int arrAllocLabel;
    private String currentClass;
    private String currentMethod;
    private boolean returnPrimaryExpr;
    private boolean returnFieldValue;
    private LinkedHashMap<String, String> registerTypes;
    private ArrayList<String> methodArgs;

    LLVMGenerateVisitor(String fileName, VTables vTables, SymbolTable symbolTable) {
        this.register = 0;
        this.loopLabel = 0;
        this.ifLabel = 0;
        this.andLabel = 0;
        this.boundsLabel = 0;
        this.arrAllocLabel = 0;
        this.vTables = vTables;
        this.symbolTable = symbolTable;
        this.fileName = fileName;
        this.returnPrimaryExpr = false;
        this.returnFieldValue = false;
        this.registerTypes = new LinkedHashMap<>();
        // Create "out" directory to store generated LLVM code
        File dir = new File("LLVM");
        // If the directory does not exist, create it
        if (!dir.exists()) {
            dir.mkdir();
        }
        try {
            // Create file to store the V-Table
            this.fileptr = new File("LLVM/" + fileName + ".ll");
            if (!this.fileptr.exists()) {
                this.fileptr.createNewFile();
            }
            // If file with same name already exists delete its contents
            else {
                PrintWriter writer = new PrintWriter(this.fileptr);
                writer.print("");
                writer.close();
            }
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    void emit(String buffer) {
        try {
            FileWriter fw = new FileWriter(this.fileptr, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);
            pw.print(buffer);
            pw.close();
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    String get_register() {
        int retVal = this.register;
        this.register++;
        return "%_" + retVal;
    }

    String get_loop_label() {
        int retVal = this.loopLabel;
        this.loopLabel++;
        return "loop" + retVal;
    }

    String get_if_label() {
        int retVal = this.ifLabel;
        this.ifLabel++;
        return "if" + retVal;
    }

    String get_and_label() {
        int retVal = this.andLabel;
        this.andLabel++;
        return "andclause" + retVal;
    }

    String get_bound_label() {
        int retVal = this.boundsLabel;
        this.boundsLabel++;
        return "oob" + retVal;
    }

    String get_arr_alloc_label() {
        int retVal = this.arrAllocLabel;
        this.arrAllocLabel++;
        return "arr_alloc" + retVal;
    }

    // This method lookups field and var names within a class which is visited this time of execution
    private String[] look_up_identifier(String identifier, SymbolTable symbolTable) throws Exception {
        // Lookup if this identifier is declared before
        SymbolTable.ClassSymTable curClass = symbolTable.classes.get(this.currentClass);
        SymbolTable.MethodSymTable curMethod = curClass.methods.get(this.currentMethod);
        // If you find it one of the below cases, return its type and the place you found it
        // Check if this identifier is a parameter or a variable
        if (curMethod.parameters.containsKey(identifier)) {
            return new String[]{curMethod.parameters.get(identifier), "parameter", curMethod.methodName};
        }
        if (curMethod.variables.containsKey(identifier)) {
            return new String[]{curMethod.variables.get(identifier), "variable", curMethod.methodName};
        }
        // Check if it is field in the class
        if (curClass.fields.containsKey(identifier)) {
            return new String[]{curClass.fields.get(identifier), "field", curClass.className};
        }
        // Check if it has parent class with this field
        while (curClass.parentClassName != null) {
            SymbolTable.ClassSymTable parentClass = symbolTable.classes.get(curClass.parentClassName);
            if (parentClass.fields.containsKey(identifier)) {
                return new String[]{parentClass.fields.get(identifier), "field", parentClass.className};
            }
            curClass = parentClass;
        }
        // If you are here then this identifier was not found...
        throw new Exception("Identifier not found!");
    }

    private int get_method_vtable_position(String identifier, String type, VTables vTables) throws Exception {
        int position = 0;
        VTables.ClassVTable classVTable = vTables.classesTables.get(type);
        for (Map.Entry classEntryMethods : classVTable.methodsTable.entrySet()) {
            String name = classEntryMethods.getKey().toString();
            if (name.equals(identifier)) {
                break;
            }
            position++;
        }
        return position;
    }

    private int get_number_of_methods(String className, SymbolTable symbolTable) throws Exception {
        SymbolTable.ClassSymTable classSymTable = symbolTable.classes.get(className);
        int numberOfMethods = classSymTable.methods.size();
        if (classSymTable.parentClassName != null) {
            while (classSymTable.parentClassName != null) {
                SymbolTable.ClassSymTable parentClass = symbolTable.classes.get(classSymTable.parentClassName);
                numberOfMethods += parentClass.methods.size();
                classSymTable = parentClass;
            }
        }
        return numberOfMethods;
    }

    private int get_offset(String identifier, String type, VTables vTables) throws Exception {
        VTables.ClassVTable classVTable = vTables.classesTables.get(type);
        // Check if it is field
        if (classVTable.fieldsTable.containsKey(identifier)) {
            int offset = Integer.parseInt(classVTable.fieldsTable.get(identifier).toString());
            offset += 8;
            System.err.println(offset);
            return offset;
        }
        // Deprecated
        else if (classVTable.methodsTable.containsKey(identifier)) {
            int offset = Integer.parseInt(classVTable.methodsTable.get(identifier).toString());
            offset /= 8;
            return offset;
        }
        throw new Exception("Identifier not found!");
    }

    private int get_class_size(String className, SymbolTable symbolTable) {
        SymbolTable.ClassSymTable curClass = symbolTable.classes.get(className);
        int size = 0;
        // If this class overrides, get parents size
        if (curClass.parentClassName != null) {
            size = get_class_size(curClass.parentClassName, this.symbolTable);
        }

        for (Map.Entry classEntryFields : curClass.fields.entrySet()) {
            String type = classEntryFields.getValue().toString();
            if (type.equals("int")) {
                size += 4;
            } else if (type.equals("boolean")) {
                size += 1;
            } else {
                size += 8;
            }
        }
        size += 8;
        return size;
    }

    void llvm_create_v_tables() {
        for (Map.Entry entry : this.vTables.classesTables.entrySet()) {
            Object key = entry.getKey();
            String className = entry.getKey().toString();
            VTables.ClassVTable classVTable = this.vTables.classesTables.get(key);
            if (classVTable.isMainClass) {
                emit("@." + className + "_vtable = global [0 x i8*] []\n");
                continue;
            }
            int numberOfFuncs = classVTable.methodsTable.size();
//            System.err.println("num of funcs" + numberOfFuncs);
            String buffer = "@." + className + "_vtable = global ["; // + numberOfFuncs + " x i8*] [";
            emit(buffer);
            buffer = "";
            // Retrieve data from symbol table
            SymbolTable.ClassSymTable classSymTable = this.symbolTable.classes.get(className);
//            for (Map.Entry classVTableEntryFields : classVTable.fieldsTable.entrySet()) {
//                String fieldName = classVTableEntryFields.getKey().toString();
//                Integer offset = Integer.parseInt(classVTableEntryFields.getValue().toString());
//            }
            boolean printComa = false;
            String extendBuffer = "";
//            boolean extendClass = false;
//            SymbolTable.MethodSymTable methodSymTable = classSymTable.methods.get();
            // If you extend another class
            if (classSymTable.parentClassName != null) {
//                extendBuffer = "[";
                // Check if this method is in parent class
                while (classSymTable.parentClassName != null) {
                    SymbolTable.ClassSymTable parentClass = symbolTable.classes.get(classSymTable.parentClassName);
                    classSymTable = parentClass;
                    className = classSymTable.className;
                    for (Map.Entry classEntryMethods : classSymTable.methods.entrySet()) {
                        numberOfFuncs++;
                        String methodName = classEntryMethods.getKey().toString();
//                        Integer offset = Integer.parseInt(classVTableEntryMethods.getValue().toString());
                        SymbolTable.MethodSymTable methodSymTable = classSymTable.methods.get(methodName);
                        String methodRetType = methodSymTable.returnType;
                        if (printComa) {
                            extendBuffer += ", ";
                        }
                        // Return type
                        if (methodRetType.equals("int")) {
                            extendBuffer += "i8* bitcast (i32 (i8*";
                        } else if (methodRetType.equals("boolean")) {
                            extendBuffer += "i8* bitcast (i1 (i8*";
                        } else if (methodRetType.equals("int[]")) { //todo
                            extendBuffer += "i8* bitcast (i32* (i8*";
                        } else {
                            extendBuffer += "i8* bitcast (i8* (i8*";
                        }
                        // Set up parameters
                        for (Map.Entry methodParams : methodSymTable.parameters.entrySet()) {
                            String paramType = methodParams.getValue().toString();
                            if (paramType.equals("int")) {
                                extendBuffer += ",i32";
                            } else if (paramType.equals("boolean")) {
                                extendBuffer += ",i1";
                            } else if (paramType.equals("int[]")) { //todo
                                extendBuffer += ",i32*";
                            } else {
                                extendBuffer += ",i8*";
                            }
                        }
                        extendBuffer += ")* @" + className + "." + methodName + " to i8*)";

                        printComa = true;
//                        emit(buffer);
//                        buffer = "";
                    }
//                    extendBuffer +="]\n";
//
                }
            }
            emit(numberOfFuncs + " x i8*] [" + extendBuffer);
            for (Map.Entry classEntryMethods : classSymTable.methods.entrySet()) {
                String methodName = classEntryMethods.getKey().toString();
//                Integer offset = Integer.parseInt(classEntryMethods.getValue().toString());
                SymbolTable.MethodSymTable methodSymTable = classSymTable.methods.get(methodName);
                String methodRetType = methodSymTable.returnType;
                if (printComa) {
                    buffer += ", ";
                }
                // Return type
                if (methodRetType.equals("int")) {
                    buffer += "i8* bitcast (i32 (i8*";
                } else if (methodRetType.equals("boolean")) {
                    buffer += "i8* bitcast (i1 (i8*";
                } else if (methodRetType.equals("int[]")) { //todo
                    buffer += "i8* bitcast (i32* (i8*";
                } else {
                    buffer += "i8* bitcast (i8* (i8*";
                }
                // Set up parameters
                for (Map.Entry methodParams : methodSymTable.parameters.entrySet()) {
                    String paramType = methodParams.getValue().toString();
                    if (paramType.equals("int")) {
                        buffer += ",i32";
                    } else if (paramType.equals("boolean")) {
                        buffer += ",i1";
                    } else if (paramType.equals("int[]")) { //todo
                        buffer += ",i32*";
                    } else {
                        buffer += ",i8*";
                    }
                }
                buffer += ")* @" + className + "." + methodName + " to i8*)";

                printComa = true;
                emit(buffer);
                buffer = "";
            }
            emit("]\n");
        }
    }

    void llvm_helper_methods() {
        String buffer = "\n" +
                "declare i8* @calloc(i32, i32)\n" +
                "declare i32 @printf(i8*, ...)\n" +
                "declare void @exit(i32)\n" +
                "\n" +
                "@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n" +
                "@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n" +
                "define void @print_int(i32 %i) {\n" +
                "    %_str = bitcast [4 x i8]* @_cint to i8*\n" +
                "    call i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n" +
                "    ret void\n" +
                "}\n" +
                "\n" +
                "define void @throw_oob() {\n" +
                "    %_str = bitcast [15 x i8]* @_cOOB to i8*\n" +
                "    call i32 (i8*, ...) @printf(i8* %_str)\n" +
                "    call void @exit(i32 1)\n" +
                "    ret void\n" +
                "}\n";
        emit(buffer);
    }

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    public String visit(Goal n, String str) throws Exception {
        String _ret = null;
        llvm_create_v_tables();
        llvm_helper_methods();
        n.f0.accept(this, null);
        n.f1.accept(this, null);
        n.f2.accept(this, null);
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
    public String visit(MainClass n, String str) throws Exception {
        emit("\ndefine i32 @main() {\n");
        this.currentClass = n.f1.accept(this, null);
        this.currentMethod = "main";
        n.f14.accept(this, null);
        n.f15.accept(this, null);

        emit("\n\tret i32 0\n}\n");
        this.register = 0;
        this.loopLabel = 0;
        this.ifLabel = 0;
        this.andLabel = 0;
        this.boundsLabel = 0;
        this.arrAllocLabel = 0;
        this.registerTypes.clear();
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
    public String visit(ClassDeclaration n, String str) throws Exception {
        this.currentClass = n.f1.accept(this, null);
        // Visit only Methods
        n.f4.accept(this, null);
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
    public String visit(ClassExtendsDeclaration n, String str) throws Exception {
        this.currentClass = n.f1.accept(this, null);
        // Visit only Methods
        n.f6.accept(this, null);
        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit(VarDeclaration n, String str) throws Exception {
        String type = n.f0.accept(this, null);
        String identifier = n.f1.accept(this, null);
        String buffer = "\t%" + identifier + " = alloca ";
        if (type.equals("int")) {
            buffer += "i32\n";
        } else if (type.equals("boolean")) {
            buffer += "i1\n";
        } else if (type.equals("int[]")) { //todo
            buffer += "i32*\n";
        } else {
            buffer += "i8*\n";
        }
        emit(buffer);
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
    public String visit(MethodDeclaration n, String str) throws Exception {
        String buffer = "\ndefine ";

        String methodType = n.f1.accept(this, null);
        String llvmMethType;
        if (methodType.equals("int")) {
            buffer += "i32";
            llvmMethType = "i32";
        } else if (methodType.equals("boolean")) {
            buffer += "i1";
            llvmMethType = "i1";
        } else if (methodType.equals("int[]")) {
            buffer += "i32*";
            llvmMethType = "i32*";
        } else {
            buffer += "i8*";
            llvmMethType = "i8*";
        }

        this.currentMethod = n.f2.accept(this, null);
        buffer += " @" + this.currentClass + "." + this.currentMethod + "(i8* %this";

        SymbolTable.ClassSymTable classSymTable = this.symbolTable.classes.get(this.currentClass);
        SymbolTable.MethodSymTable methodSymTable = classSymTable.methods.get(this.currentMethod);
        // Set up parameters
        for (Map.Entry methodParams : methodSymTable.parameters.entrySet()) {
            String paramType = methodParams.getValue().toString();
            String paramName = methodParams.getKey().toString();
            if (paramType.equals("int")) {
                buffer += ", i32";
            } else if (paramType.equals("boolean")) {
                buffer += ", i1";
            } else if (paramType.equals("int[]")) {
                buffer += ", i32*";
            } else {
                buffer += ", i8*";
            }
            buffer += " %." + paramName;
        }
        buffer += ") {\n";
        emit(buffer);
        // Allocate parameters

        for (Map.Entry methodParams : methodSymTable.parameters.entrySet()) {
            String paramType = methodParams.getValue().toString();
            String paramName = methodParams.getKey().toString();
            buffer = "\t%" + paramName + " = alloca ";
            if (paramType.equals("int")) {
                buffer += "i32\n";
                buffer += "\tstore i32 %." + paramName + ", i32* %" + paramName;
            } else if (paramType.equals("boolean")) {
                buffer += "i1\n";
                buffer += "\tstore i1 %." + paramName + ", i1* %" + paramName;
            } else if (methodType.equals("int[]")) { //todo na to tsekarw
                buffer += "i32*\n";
                buffer += "\tstore i32* %." + paramName + ", i32** %" + paramName;
            } else {
                buffer += "i8*\n";
                buffer += "\tstore i8* %." + paramName + ", i8** %" + paramName;
            }
            buffer += "\n";
            emit(buffer);
        }

        // Visit var declarations & statements
        n.f7.accept(this, null);
        n.f8.accept(this, null);

        // Return
//        emit("\n");
        this.returnFieldValue = true;
        String retExpr = n.f10.accept(this, null);
//        String retRegister = get_register();
//        buffer = "\t" + retRegister + " = load " + llvmMethType + ", " + llvmMethType + "* " + retExpr + '\n';
        buffer = "\n\tret " + llvmMethType + " " + retExpr + "\n}\n";
        emit(buffer);
        this.register = 0;
        this.loopLabel = 0;
        this.ifLabel = 0;
        this.andLabel = 0;
        this.boundsLabel = 0;
        this.arrAllocLabel = 0;
        this.registerTypes.clear();
        return null;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    public String visit(AssignmentStatement n, String str) throws Exception {
        String buffer;
        returnPrimaryExpr = false;
        String identifier = n.f0.accept(this, null);
        String results[] = look_up_identifier(identifier, this.symbolTable);

//        System.err.println(expr);
        String llvmType;
        String targetRegister;
        if (results[1].equals("field")) {
            String reg1 = get_register();
            String reg2 = get_register();
            if (results[0].equals("int")) {
                llvmType = "i32*";
            } else if (results[0].equals("boolean")) {
                llvmType = "i1*";
            } else if (results[0].equals("int[]")) {
                llvmType = "i32**";
            } else {
                llvmType = "i8**";
            }
            // todo na tsekarw to this an einai swsto!!!
            buffer = "\t" + reg1 + " = getelementptr i8, i8* %this, i32 " + get_offset(identifier, results[2], this.vTables) + "\n";

            buffer += "\t" + reg2 + " = bitcast i8* " + reg1 + " to " + llvmType + "\n";
            targetRegister = reg2;
            emit(buffer);
        }
        // Variable or parameter
        else {
            if (results[0].equals("int")) {
                llvmType = "i32*";
            } else if (results[0].equals("boolean")) {
                llvmType = "i1*";
            } else if (results[0].equals("int[]")) {
                llvmType = "i32**";
            } else {
                llvmType = "i8**";
            }
            targetRegister = "%" + identifier;
        }

        this.returnFieldValue = true;
        String expr = n.f2.accept(this, null);

        buffer = "\tstore " + llvmType.substring(0, llvmType.length() - 1) + " " + expr + ", " + llvmType + " " + targetRegister + "\n";
        emit(buffer);
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
    public String visit(ArrayAssignmentStatement n, String str) throws Exception {
        String lbl1 = get_bound_label();
        String lbl2 = get_bound_label();
        String lbl3 = get_bound_label();
        String tempreg1;
        String tempreg2 = get_register();
        String cmpReg = get_register();
        String tempreg3 = get_register();
        String tempreg4 = get_register();


        String reg1 = n.f0.accept(this, null);
        String results[] = look_up_identifier(reg1, this.symbolTable);
        String buffer;
        if (results[1].equals("field")) {
            String fieldReg1 = get_register();
            String fieldReg2 = get_register();
            String fieldReg3 = get_register();
            buffer = "\t" + fieldReg1 + " = getelementptr i8, i8* %this, i32 " + get_offset(reg1, results[2], this.vTables) + "\n";
            buffer += "\t" + fieldReg2 + " = bitcast i8* " + fieldReg1 + " to i32**\n";
            buffer += "\t" + fieldReg3 + " = load i32*, i32** " + fieldReg2 + "\n";
            tempreg1 = fieldReg3;
            emit(buffer);
        }
        // Parameter or variable
        else {
            String varReg = get_register();
            emit("\t" + varReg + " = load i32*, i32** %" + reg1 + "\n");
            tempreg1 = varReg;
        }
        System.err.println(tempreg2);

        String reg2 = n.f2.accept(this, null);
        emit("\t" + tempreg2 + " = load i32, i32* " + tempreg1 + "\n");

        emit("\t" + cmpReg + " = icmp ult i32 " + reg2 + ", " + tempreg2 + "\n");
        emit("\tbr i1 " + cmpReg + ", label %" + lbl1 + ", label %" + lbl2 + "\n");
        emit("\n" + lbl1 + ":\n");

        String reg3 = n.f5.accept(this, null);
        emit("\t" + tempreg3 + " = add i32 " + reg2 + ", 1\n");
        emit("\t" + tempreg4 + " = getelementptr i32, i32* " + tempreg1 + ", i32 " + tempreg3 + "\n");
        emit("\tstore i32 " + reg3 + ", i32* " + tempreg4 + "\n");

        emit("\tbr label %" + lbl3 + "\n");

        emit("\n" + lbl2 + ":\n");
//        emit("\tcall void (i32) @print_int(i32 " + tempreg2 + ")\n"); //todo!!
        emit("\tcall void @throw_oob()\n");
        emit("\tbr label %" + lbl3 + "\n");

        emit("\n" + lbl3 + ":\n");

        return tempreg4;
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
    public String visit(IfStatement n, String str) throws Exception {
        String label1 = get_if_label();
        String label2 = get_if_label();
        String label3 = get_if_label();
        this.returnFieldValue = true;
        String regExpr = n.f2.accept(this, null);
        emit("\tbr i1 " + regExpr + ", label %" + label1 + ", label %" + label2 + "\n");

        // if
        emit("\n" + label1 + ":\n");
        n.f4.accept(this, null);
        emit("\n\tbr label %" + label3 + "\n");
        emit("\n" + label2 + ":\n");

        // else
        n.f6.accept(this, null);
        emit("\n\tbr label %" + label3 + "\n");
        emit("\n" + label3 + ":\n");
        return null;
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    public String visit(WhileStatement n, String str) throws Exception {
        String loop1 = get_loop_label();
        String loop2 = get_loop_label();
        String loop3 = get_loop_label();

        emit("\n\tbr label %" + loop1 + "\n");
        emit("\n" + loop1 + ":\n");
        String reg1 = n.f2.accept(this, null);
        emit("\tbr i1 " + reg1 + ", label %" + loop2 + ", label %" + loop3 + "\n");

        emit("\n" + loop2 + ":\n");
        String reg2 = n.f4.accept(this, null);
        emit("\n\tbr label %" + loop1 + "\n");

        emit("\n" + loop3 + ":\n");

        return null;
    }

    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    public String visit(PrintStatement n, String str) throws Exception {
        this.returnFieldValue = true;
        String reg = n.f2.accept(this, null);
        emit("\tcall void (i32) @print_int(i32 " + reg + ")\n");
        return null;
    }

    /**
     * f0 -> Clause()
     * f1 -> "&&"
     * f2 -> Clause()
     */
    public String visit(AndExpression n, String str) throws Exception {
        String andLbl1 = get_and_label();
        String andLbl2 = get_and_label();
        String andLbl3 = get_and_label();
        String andLbl4 = get_and_label();
        String phiReg = get_register();

        this.returnFieldValue = true;
        String reg1 = n.f0.accept(this, null);
        emit("\tbr label %" + andLbl1 + "\n");
        emit("\n" + andLbl1 + ":\n");
        emit("\tbr i1 " + reg1 + ", label %" + andLbl2 + ", label %" + andLbl3 + "\n");

        this.returnFieldValue = true;
        emit("\n" + andLbl2 + ":\n");
        String reg2 = n.f2.accept(this, null);
        emit("\tbr label %" + andLbl3 + "\n");

        emit("\n" + andLbl3 + ":\n");
        emit("\tbr label %" + andLbl4 + "\n");

        emit("\n" + andLbl4 + ":\n");
        emit("\t" + phiReg + " = phi i1 [ 0, %" + andLbl1 + " ], [ " + reg2 + ", %" + andLbl3 + " ]\n");

        return phiReg;
    }

    /**
     * f0 -> "!"
     * f1 -> Clause()
     */
    public String visit(NotExpression n, String str) throws Exception {
        String reg = n.f1.accept(this, null);
        String xorReg = get_register();
        emit("\t" + xorReg + " = xor i1 1, " + reg + "\n");
        return xorReg;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public String visit(CompareExpression n, String str) throws Exception {
        this.returnFieldValue = true;
        String reg1 = n.f0.accept(this, null);
        this.returnFieldValue = true;
        String reg2 = n.f2.accept(this, null);
        String resultReg = get_register();
        emit("\t" + resultReg + " = icmp slt i32 " + reg1 + ", " + reg2 + "\n");
        return resultReg;
    }


    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression n, String str) throws Exception {
        this.returnFieldValue = true;
        String reg1 = n.f0.accept(this, null);
        this.returnFieldValue = true;
        String reg2 = n.f2.accept(this, null);
        String resultReg = get_register();
        emit("\t" + resultReg + " = add i32 " + reg1 + ", " + reg2 + "\n");
        return resultReg;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression n, String str) throws Exception {
        this.returnFieldValue = true;
        String reg1 = n.f0.accept(this, null);
        this.returnFieldValue = true;
        String reg2 = n.f2.accept(this, null);
        String resultReg = get_register();
        emit("\t" + resultReg + " = sub i32 " + reg1 + ", " + reg2 + "\n");
        return resultReg;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression n, String str) throws Exception {
        this.returnFieldValue = true;
        String reg1 = n.f0.accept(this, null);
        this.returnFieldValue = true;
        String reg2 = n.f2.accept(this, null);
        String resultReg = get_register();
        emit("\t" + resultReg + " = mul i32 " + reg1 + ", " + reg2 + "\n");
        return resultReg;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public String visit(ArrayLookup n, String str) throws Exception {
        String lbl1 = get_bound_label();
        String lbl2 = get_bound_label();
        String lbl3 = get_bound_label();
        String tempreg1 = get_register();
        String tempreg2 = get_register();
        String cmpReg = get_register();
        String tempreg3 = get_register();
        String tempreg4 = get_register();

        this.returnFieldValue = true;
        String reg1 = n.f0.accept(this, null);
        emit("\t" + tempreg1 + " = load i32, i32* " + reg1 + "\n");

        this.returnFieldValue = true;
        String reg2 = n.f2.accept(this, null);


        emit("\t" + cmpReg + " = icmp ult i32 " + reg2 + ", " + tempreg1 + "\n");
        emit("\tbr i1 " + cmpReg + ", label %" + lbl1 + ", label %" + lbl2 + "\n");

        emit("\n" + lbl1 + ":\n");
        emit("\t" + tempreg2 + " = add i32 " + reg2 + ", 1\n");
        emit("\t" + tempreg3 + " = getelementptr i32, i32* " + reg1 + ", i32 " + tempreg2 + "\n");
        emit("\t" + tempreg4 + " = load i32, i32* " + tempreg3 + "\n");
        emit("\tbr label %" + lbl3 + "\n");

        emit("\n" + lbl2 + ":\n");
        emit("\tcall void (i32) @print_int(i32 " + "12" + ")\n"); //todo!!
        emit("\tcall void @throw_oob()\n");
        emit("\tbr label %" + lbl3 + "\n");

        emit("\n" + lbl3 + ":\n");

        return tempreg4;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public String visit(ArrayLength n, String str) throws Exception {
        String tempreg = get_register();
        this.returnFieldValue = true;
        String reg = n.f0.accept(this, null);
        emit("\t" + tempreg + " = load i32, i32* " + reg + "\n");
        return tempreg;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    public String visit(MessageSend n, String str) throws Exception {
//        this.returnPrimaryExpr = true;
        // Visit PrimaryExpression

        String register = n.f0.accept(this, null);
        String registerType = this.registerTypes.get(register);
        // Use current Class type if 'this' is used
        if (register.equals("%this")) {
            registerType = this.currentClass;
        }
        if (registerType == null) { // TODO BUG HERE!
            String results[] = look_up_identifier(register, this.symbolTable);
            registerType = results[0];
        }

        String methName = n.f2.accept(this, null);
        SymbolTable.ClassSymTable classSymTable = this.symbolTable.classes.get(registerType);
        SymbolTable.MethodSymTable methodSymTable = classSymTable.methods.get(methName);
        // If you didn't found method in current class,
        // then this method belongs to a superclass and this class is child class
        if (methodSymTable == null && classSymTable.parentClassName != null) {
            // Check if this method is in parent class
            while (classSymTable.parentClassName != null) {
                SymbolTable.ClassSymTable parentClass = symbolTable.classes.get(classSymTable.parentClassName);
                if (parentClass.methods.containsKey(methName)) {
                    // Return your type and class name
//                    return new String[]{parentClass.methods.get(methodName).returnType, parentClass.className};
                    classSymTable = parentClass;
//                    registerType = classSymTable.className;
                    methodSymTable = classSymTable.methods.get(methName);
                    break;
                }
                classSymTable = parentClass;
            }
        }
        String methodType = methodSymTable.returnType;

        // Create an array to hold up the types of parameters
        // If it is already created then it is assumed where are in nested call
        // Hold the data of the current array and create a new one to type check the nested call
        ArrayList<String> backupMethodArgs = null;
        boolean methodArgsTempFlag = false;
        if (this.methodArgs != null) {
            methodArgsTempFlag = true;
            backupMethodArgs = new ArrayList<>(this.methodArgs);
        }
        this.methodArgs = new ArrayList<>();


//        int offset = get_offset(methName, registerType, this.vTables);
        int offset = get_method_vtable_position(methName, registerType, this.vTables);
        System.err.println("offset " + offset);
        emit("\n\t; " + registerType + "." + methName + " : " + offset + "\n");
        String reg1 = get_register();
        String reg2 = get_register();
        String reg3 = get_register();
        String reg4 = get_register();
        String reg5 = get_register();
        String reg6 = get_register();
        String buffer = "\t" + reg1 + " = bitcast i8* " + register + " to i8***\n";
        buffer += "\t" + reg2 + " = load i8**, i8*** " + reg1 + "\n";
        buffer += "\t" + reg3 + " = getelementptr i8*, i8** " + reg2 + ", i32 " + offset + "\n";
        buffer += "\t" + reg4 + " = load i8*, i8** " + reg3 + "\n";
        buffer += "\t" + reg5 + " = bitcast i8* " + reg4 + " to ";

        String llvmMethType;
        if (methodType.equals("int")) {
            buffer += "i32 (i8*";
            llvmMethType = "i32";
        } else if (methodType.equals("boolean")) {
            buffer += "i1 (i8*";
            llvmMethType = "i1";
        } else if (methodType.equals("int[]")) { //todo na to tsekarw
            buffer += "i32* (i8*";
            llvmMethType = "i32*";
        } else {
            buffer += "i8* (i8*";
            llvmMethType = "i8*";
        }
        for (Map.Entry methodEntryFunctions : methodSymTable.parameters.entrySet()) {
            String paramType = methodEntryFunctions.getValue().toString();
            if (paramType.equals("int")) {
                buffer += ", i32";
            } else if (paramType.equals("boolean")) {
                buffer += ", i1";
            } else if (methodType.equals("int[]")) { //todo na to tsekarw
                buffer += ", i32*";
            } else {
                buffer += ", i8*";
            }
        }
        buffer += ")*\n";
        emit(buffer);

        // Visit parameters
        n.f4.accept(this, null);
        buffer = "\t" + reg6 + " = call " + llvmMethType + " " + reg5 + "(i8* " + register;

        // Insert parameters
        for (int i = 0; i < this.methodArgs.size(); i++) {
            String paramType = (new ArrayList<>(methodSymTable.parameters.values())).get(i);
            String reg = this.methodArgs.get(i);
            if (paramType.equals("int")) {
                buffer += ", i32 " + reg;
            } else if (paramType.equals("boolean")) {
                buffer += ", i1 " + reg;
            } else if (methodType.equals("int[]")) { //todo na to tsekarw
                buffer += ", i32* " + reg;
            } else {
                buffer += ", i8* " + reg;
            }
        }
        buffer += ")\n";
        emit(buffer);
//        emit("\tcall void (i32) @print_int(i32 " + 12 + ")\n");

        // Erase the array
        this.methodArgs = null;
        // Restore previous array if was existed before the MessageSend visit
        if (methodArgsTempFlag) {
            this.methodArgs = new ArrayList<>(backupMethodArgs);
        }
        this.returnPrimaryExpr = false;
        this.registerTypes.put(reg6, methodType);
        return reg6;
    }

    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    public String visit(ExpressionList n, String str) throws Exception {
        this.returnPrimaryExpr = false;
        this.returnFieldValue = true;
        String register = n.f0.accept(this, null);
        this.methodArgs.add(register);
        n.f1.accept(this, null);
        return null;
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    public String visit(ExpressionTerm n, String str) throws Exception {
        this.returnFieldValue = true;
        this.methodArgs.add(n.f1.accept(this, null));
        return null;
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
    public String visit(PrimaryExpression n, String str) throws Exception {
        String expression = n.f0.accept(this, str);
        // Return immediately if child visitor forced you
        if (this.returnPrimaryExpr) {
            this.returnPrimaryExpr = false;
            return expression;
        }
        if (expression.matches("-?\\d+")) {
            return expression;
        } else if (expression.equals("true")) {
            return "1";
        } else if (expression.equals("false")) {
            return "0";
        } else if (expression.equals("%this")) {
            return "%this";
        }
        // Not primitive
        else {
            String results[] = look_up_identifier(expression, this.symbolTable);
            String buffer;
            if (results[1].equals("field")) {
                String llvmType;
                String reg1 = get_register();
                String reg2 = get_register();
                emit("\n");
                buffer = "\t" + reg1 + " = getelementptr i8, i8* %this, i32 " + get_offset(expression, results[2], this.vTables) + "\n";
                if (results[0].equals("int")) {
                    llvmType = "i32*";
                } else if (results[0].equals("boolean")) {
                    llvmType = "i1*";
                } else if (results[0].equals("int[]")) { //todo
                    llvmType = "i32**";
                } else {
                    llvmType = "i8**";
                }
                buffer += "\t" + reg2 + " = bitcast i8* " + reg1 + " to " + llvmType + "\n";
                emit(buffer);
                if (this.returnFieldValue) {
                    String tmpReg = get_register();
                    this.returnFieldValue = false;
                    if (results[0].equals("int")) {
                        emit("\t" + tmpReg + " = load i32, i32* " + reg2 + "\n");
                    } else if (results[0].equals("boolean")) {
                        emit("\t" + tmpReg + " = load i1, i1* " + reg2 + "\n");
                    } else if (results[0].equals("int[]")) {
                        emit("\t" + tmpReg + " = load i32*, i32** " + reg2 + "\n");
                    } else {
                        emit("\t" + tmpReg + " = load i8*, i8** " + reg2 + "\n");
                    }
                    reg2 = tmpReg;
                }
                this.registerTypes.put(reg2, results[0]);
                return reg2;
            }
            // Parameter or variable
            else {
                String reg1 = get_register();
                buffer = "\n\t" + reg1 + " = load ";
                if (results[0].equals("int")) {
                    buffer += "i32, i32* ";
                } else if (results[0].equals("boolean")) {
                    buffer += "i1, i1* ";
                } else if (results[0].equals("int[]")) { //todo
                    buffer += "i32*, i32** ";
                } else {
                    buffer += "i8*, i8** ";
                }
                buffer += "%" + expression + "\n";
                emit(buffer);
                this.registerTypes.put(reg1, results[0]);
                return reg1;
            }
        }
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public String visit(ArrayAllocationExpression n, String str) throws Exception {
        String lbl1 = get_arr_alloc_label();
        String lbl2 = get_arr_alloc_label();
        String cmpReg = get_register();
        String reg1 = get_register();
        String reg2 = get_register();
        String reg3 = get_register();
//        String reg4 = get_register();

        String reg = n.f3.accept(this, null);
        emit("\t" + cmpReg + " = icmp slt i32 " + reg + ", 0\n");
        emit("\tbr i1 " + cmpReg + ", label %" + lbl1 + ", label %" + lbl2 + "\n");

        emit("\n" + lbl1 + ":\n");
        emit("\tcall void @throw_oob()\n");
        emit("\tbr label %" + lbl2 + "\n");

        emit("\n" + lbl2 + ":\n");
        emit("\t" + reg1 + " = add i32 " + reg + ", 1\n");
        emit("\t" + reg2 + " = call i8* @calloc(i32 4, i32 " + reg1 + ")\n");
        emit("\t" + reg3 + " = bitcast i8* " + reg2 + " to i32*\n");
        emit("\tstore i32 " + reg + ", i32* " + reg3 + "\n");
//        emit("\tcall void (i32) @print_int(i32 "+ reg1 + ")\n"); //todo!!
        this.returnPrimaryExpr = true;
        this.registerTypes.put(reg3, "int[]");
        return reg3;
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public String visit(AllocationExpression n, String str) throws Exception {
        String className = n.f1.accept(this, str);
//        emit("\tcall void (i32) @print_int(i32 " + 11 + ")\n"); //todo
        // Get class size and the number of methods that are contained
        int classSize = get_class_size(className, this.symbolTable);
//        VTables.ClassVTable classVTable = this.vTables.classesTables.get(className);
//        int numberOfMethods = classVTable.methodsTable.size();
        int numberOfMethods = get_number_of_methods(className,this.symbolTable);

        String buffer;
        String reg1 = get_register();
        String reg2 = get_register();
        String reg3 = get_register();
        emit("\n");
        buffer = "\t" + reg1 + " = call i8* @calloc(i32 1, i32 " + classSize + ")\n";
        buffer += "\t" + reg2 + " = bitcast i8* " + reg1 + " to i8***\n";
        buffer += "\t" + reg3 + " = getelementptr [" + numberOfMethods + " x i8*], [" + numberOfMethods + " x i8*]* @." + className + "_vtable, i32 0, i32 0\n";
        buffer += "\tstore i8** " + reg3 + ", i8*** " + reg2 + "\n";
        emit(buffer);
        this.returnPrimaryExpr = true;
        this.registerTypes.put(reg1, className);
        return reg1;
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public String visit(BracketExpression n, String str) throws Exception {
        String register = n.f1.accept(this, null);
        this.returnPrimaryExpr = true;
        return register;
    }

    /**
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
     */
    public String visit(ArrayType n, String str) throws Exception {
        return "int[]";
    }

    /**
     * f0 -> "boolean"
     */
    public String visit(BooleanType n, String str) throws Exception {
        return "boolean";
    }

    /**
     * f0 -> "int"
     */
    public String visit(IntegerType n, String str) throws Exception {
        return "int";
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public String visit(IntegerLiteral n, String str) throws Exception {
        return n.f0.toString();
    }

    /**
     * f0 -> "true"
     */
    public String visit(TrueLiteral n, String str) throws Exception {
        return "true";
    }

    /**
     * f0 -> "false"
     */
    public String visit(FalseLiteral n, String str) throws Exception {
        return "false";
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier n, String str) throws Exception {
        return n.f0.toString();
    }

    /**
     * f0 -> "this"
     */
    public String visit(ThisExpression n, String str) throws Exception {
        return "%this";
    }
}
