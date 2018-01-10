import syntaxtree.*;
import visitor.GJNoArguDepthFirst;

import java.io.*;
import java.util.Map;

@SuppressWarnings("Duplicates") // Remove IntelliJ warning about duplicate code

public class LLVMGenerateVisitor extends GJNoArguDepthFirst<String> {
    public VTables vTables;
    public SymbolTable symbolTable;
    public String fileName;
    public File fileptr;
    public int register;
    public int label;

    LLVMGenerateVisitor(String fileName, VTables vTables, SymbolTable symbolTable) {
        this.vTables = vTables;
        this.symbolTable = symbolTable;
        this.fileName = fileName;
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
        return "_%" + retVal;
    }

    String get_loop_label() {
        int retVal = this.register;
        this.register++;
        return "loop" + retVal;
    }

    String get_if_label() {
        int retVal = this.register;
        this.register++;
        return "if" + retVal;
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
            String buffer = "@." + className + "_vtable = global [" + numberOfFuncs + " x i8*] [";
            // Retrieve data from symbol table
            SymbolTable.ClassSymTable classSymTable = this.symbolTable.classes.get(className);
//            for (Map.Entry classVTableEntryFields : classVTable.fieldsTable.entrySet()) {
//                String fieldName = classVTableEntryFields.getKey().toString();
//                Integer offset = Integer.parseInt(classVTableEntryFields.getValue().toString());
//            }
            boolean printComa = false;
            for (Map.Entry classVTableEntryMethods : classVTable.methodsTable.entrySet()) {
                String methodName = classVTableEntryMethods.getKey().toString();
                Integer offset = Integer.parseInt(classVTableEntryMethods.getValue().toString());
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
                } else {
                    buffer = buffer + "i8* bitcast (i8* (i8*";
                }
                // Set up parameters
                for (Map.Entry methodParams : methodSymTable.parameters.entrySet()) {
                    String paramType = methodParams.getValue().toString();
                    if (paramType.equals("int")) {
                        buffer += ",i32";
                    } else if (paramType.equals("boolean")) {
                        buffer += ",i1";
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
    public String visit(Goal n) throws Exception {
        String _ret = null;
        llvm_create_v_tables();
        llvm_helper_methods();
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        return _ret;
    }


}
