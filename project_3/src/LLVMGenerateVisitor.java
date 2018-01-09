import syntaxtree.*;
import visitor.GJDepthFirst;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

@SuppressWarnings("Duplicates") // Remove IntelliJ warning about duplicate code

public class LLVMGenerateVisitor extends GJDepthFirst<String, VTables> {
    public String fileName;
    public File fileptr;
    public int register;
    public int label;

    LLVMGenerateVisitor(String fileName) {
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
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    void emit(String buffer) {
        try {
            FileWriter fw = new FileWriter(this.fileptr, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);
            pw.println(buffer);
            pw.close();
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    String get_register(){
        int retVal = this.register;
        this.register++;
        return "_%"+retVal;
    }

    String get_loop_label(){
        int retVal = this.register;
        this.register++;
        return "loop"+retVal;
    }

    String get_if_label(){
        int retVal = this.register;
        this.register++;
        return "if"+retVal;
    }

    void LLVMCreateVTables(VTables vTables) {
        for (Map.Entry entry : vTables.classesTables.entrySet()) {
            Object key = entry.getKey();
            String className = entry.getKey().toString();
            VTables.ClassVTable classVTable = vTables.classesTables.get(key);
            if (classVTable.isMainClass) {
                emit("."+ className + "_vtable = global [0 x i8*] []");
                continue;
            }
            emit("@."+ className + "_vtable = global [0 x i8*] []");
            for (Map.Entry classVTableEntryFields : classVTable.fieldsTable.entrySet()) {
                String fieldName = classVTableEntryFields.getKey().toString();
                Integer offset = Integer.parseInt(classVTableEntryFields.getValue().toString());
            }
            for (Map.Entry classVTableEntryMethods : classVTable.methodsTable.entrySet()) {
                String methodName = classVTableEntryMethods.getKey().toString();
                Integer offset = Integer.parseInt(classVTableEntryMethods.getValue().toString());
            }
        }
    }

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    public String visit(Goal n, VTables vTables) throws Exception {
        String _ret = null;
        LLVMCreateVTables(vTables);
        n.f0.accept(this, vTables);
        n.f1.accept(this, vTables);
        n.f2.accept(this, vTables);
        return _ret;
    }

}
