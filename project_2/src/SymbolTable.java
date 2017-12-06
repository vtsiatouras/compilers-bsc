import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

//todo na tsekarw pou 8elei linkedhashmap gia na exw order

public class SymbolTable {

    public LinkedHashMap<String, ClassSymTable> classes;

    SymbolTable() {
        classes = new LinkedHashMap<>();
    }

    void print_symbol_table() {
        for (Map.Entry entry : classes.entrySet()) {
            Object key = entry.getKey();
            System.out.println("CLASS: " + key);
            ClassSymTable classSym = classes.get(key);
            if (classSym.parentClassName != null) {
                System.out.println("Extends class '" + classSym.parentClassName + "'");
            }
            System.out.println("\nFIELDS:");
            for (Map.Entry classEntryFields : classSym.fields.entrySet()) {
                System.out.println("   " + classEntryFields.getValue() + " " + classEntryFields.getKey());
            }
            System.out.println("\nFUNCTIONS:");
            for (Map.Entry classEntryFunctions : classSym.methods.entrySet()) {
                Object keyMethod = classEntryFunctions.getKey();
                MethodSymTable methSym = classSym.methods.get(keyMethod);
                System.out.print("    " + methSym.returnType + " " + methSym.methodName + "(");

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
            System.out.println("--------------------");
        }
    }

    // This method checks the types of the fields, methods and variables
    // that stored in the symbol table after the first visit
    void type_check_symbol_table() throws Exception {
        for (Map.Entry entry : classes.entrySet()) {
            Object key = entry.getKey();
            ClassSymTable classSym = classes.get(key);
            // Check the fields
            for (Map.Entry classEntryFields : classSym.fields.entrySet()) {
                String fieldType = classEntryFields.getValue().toString();
                if (!classes.containsKey(fieldType) && !fieldType.equals("int") && !fieldType.equals("boolean") && !fieldType.equals("int[]")) {
                    throw new Exception("Unkown type name '" + fieldType + "'");
                }
            }
            // Check the methods
            for (Map.Entry classEntryFunctions : classSym.methods.entrySet()) {
                Object keyMethod = classEntryFunctions.getKey();
                MethodSymTable methSym = classSym.methods.get(keyMethod);
                String methodType = methSym.returnType;
                // Ignore main's type
                if (!methSym.methodName.equals("main")) {
                    if (!classes.containsKey(methodType) && !methodType.equals("int") && !methodType.equals("boolean") && !methodType.equals("int[]")) {
                        throw new Exception("Unkown type name '" + methodType + "'");
                    }
                    // Check parameters inside methods
                    for (Map.Entry methodEntryFunctions : methSym.parameters.entrySet()) {
                        String paramType = methodEntryFunctions.getValue().toString();
                        if (!classes.containsKey(paramType) && !paramType.equals("int") && !paramType.equals("boolean") && !paramType.equals("int[]")) {
                            throw new Exception("Unkown type name '" + paramType + "'");
                        }
                    }
                }
                for (Map.Entry methodEntryFunctions : methSym.variables.entrySet()) {
                    String varType = methodEntryFunctions.getValue().toString();
                    if (!classes.containsKey(varType) && !varType.equals("int") && !varType.equals("boolean") && !varType.equals("int[]")) {
                        throw new Exception("Unkown type name '" + varType + "'");
                    }
                }
            }
        }
    }

    public static class ClassSymTable {
        public String className;
        public String parentClassName;
        public LinkedHashMap<String, String> fields;
        public LinkedHashMap<String, MethodSymTable> methods;

        ClassSymTable() {
            className = null;
            parentClassName = null;
            fields = new LinkedHashMap<>();
            methods = new LinkedHashMap<>();
        }
    }

    public static class MethodSymTable {
        public String methodName;
        public String returnType;
        public LinkedHashMap<String, String> parameters;
        public LinkedHashMap<String, String> variables;

        MethodSymTable() {
            methodName = null;
            returnType = null;
            parameters = new LinkedHashMap<>();
            variables = new LinkedHashMap<>();
        }
    }

}
