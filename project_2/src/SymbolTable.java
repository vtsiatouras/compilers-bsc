import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

//todo na tsekarw pou 8elei linkedhashmap gia na exw order

public class SymbolTable {

    public HashMap<String, ClassSymTable> classes;

    SymbolTable() {
        classes = new HashMap<String, ClassSymTable>();
    }

    void PrintSymbolTable() {
        for (Map.Entry entry : classes.entrySet()) {
            Object key = entry.getKey();
            System.out.println("CLASS: " + key);
            ClassSymTable classSym = classes.get(key);
            System.out.println("\nFIELDS:");
            for (Map.Entry classEntryFields : classSym.fields.entrySet()) {
                System.out.println("   " + classEntryFields.getValue() + " " + classEntryFields.getKey());
            }
            System.out.println("\nFUNCTIONS:");
            for (Map.Entry classEntryFunctions : classSym.methods.entrySet()) {
                Object keyMethod = classEntryFunctions.getKey();
                System.out.print("    " + keyMethod + "(");
                MethodSymTable methSym = classSym.methods.get(keyMethod);
//                System.out.println("   PARAMETERS:");
                boolean flag = false;
                for (Map.Entry methodEntryFunctions : methSym.parameters.entrySet()) {
                    if (flag) {
                        System.out.print(", ");
                    }
                    flag = true;
                    System.out.print(methodEntryFunctions.getValue() + " " + methodEntryFunctions.getKey());
                }
                System.out.print(")\n");
                System.out.println("    VAR DECLS:");
                for (Map.Entry methodEntryFunctions : methSym.variables.entrySet()) {
                    System.out.println("        " + methodEntryFunctions.getValue() + " " + methodEntryFunctions.getKey());
                }
                System.out.println();
            }
            System.out.println("====================================");
        }
    }

    public static class ClassSymTable {
        public String className;
        public String parentClassName;
        public HashMap<String, String> fields;
        public HashMap<String, MethodSymTable> methods;

        ClassSymTable() {
            className = null;
            parentClassName = null;
            fields = new HashMap<String, String>();
            methods = new HashMap<String, MethodSymTable>();
        }
    }

    public static class MethodSymTable {
        public String methodName;
        public String returnType;
        public LinkedHashMap<String, String> parameters;
        public LinkedHashMap<String, String> variables;

        MethodSymTable() {
            parameters = new LinkedHashMap<String, String>();
            variables = new LinkedHashMap<String, String>();
        }
    }

}
