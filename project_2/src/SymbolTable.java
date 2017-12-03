import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    public HashMap<String, ClassSymTable> classes;

    SymbolTable() {
        classes = new HashMap<String, ClassSymTable>();
    }

    void PrintSymbolTable() {
        for (Map.Entry entry : classes.entrySet()) {
            Object key = entry.getKey();
            System.out.println("Class: " + key);
            ClassSymTable classSym = classes.get(key);
            System.out.println("Fields:");
            for (Map.Entry classEntryFields : classSym.fields.entrySet()) {
                System.out.println("   " + classEntryFields.getKey() + ", " + classEntryFields.getValue());
            }
            System.out.println("Functions:");
            for (Map.Entry classEntryFunctions : classSym.functions.entrySet()) {
                System.out.println("   " + classEntryFunctions.getKey() + ", " + classEntryFunctions.getValue());
            }
            System.out.println();
        }
    }

    public static class ClassSymTable {
        public String className;
        public String parentClassName;
        public HashMap<String, String> fields;
        public HashMap<String, MethodSymTable> functions;

        ClassSymTable() {
            className = null;
            parentClassName = null;
            fields = new HashMap<String, String>();
            functions = new HashMap<String, MethodSymTable>();
        }
    }

    public static class MethodSymTable {
        public HashMap<String, String> parameters;

        MethodSymTable() {
            parameters = new HashMap<String, String>();
        }
    }


}
