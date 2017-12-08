import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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

    void calculate_offsets() {
        int fieldOffset, methodOffset;
        String mainClassName = null;
        // Local data structure to store offsets for each class
        HashMap<String, ArrayList<Integer>> offsetTable = new HashMap<>();
        for (Map.Entry entry : classes.entrySet()) {
            Object key = entry.getKey();
            ClassSymTable classSym = classes.get(key);
            // Ignore main class
            if (classSym.mainClass) {
                mainClassName = classSym.className;
                continue;
            }
            // If it is child class get parent's offset
            if (classSym.parentClassName != null && !classSym.parentClassName.equals(mainClassName)) {
                ArrayList<Integer> curOffset = offsetTable.get(classSym.parentClassName);
                System.out.println(curOffset.get(0)+" "+curOffset.get(1));
                fieldOffset = curOffset.get(0);
                methodOffset = curOffset.get(1);
            } else {
                fieldOffset = 0;
                methodOffset = 0;
            }
            System.out.println("-----------Class "+ classSym.className + "-----------");
            System.out.println("---Variables---");
            for (Map.Entry classEntryFields : classSym.fields.entrySet()) {
                String type = classEntryFields.getValue().toString();
                String var = classEntryFields.getKey().toString();
                if(type.equals("int")){
                    System.out.println(classSym.className + "." + var + " : "+ fieldOffset);
                    fieldOffset += 4;
                }
                else if(type.equals("boolean")){
                    System.out.println(classSym.className + "." + var + " : "+ fieldOffset);
                    fieldOffset += 1;
                }
                else {
                    System.out.println(classSym.className + "." + var + " : "+ fieldOffset);
                    fieldOffset += 8;
                }
            }
            System.out.println("---Methods---");
            for (Map.Entry classEntryFunctions : classSym.methods.entrySet()) {
                Object keyMethod = classEntryFunctions.getKey();
                MethodSymTable methSym = classSym.methods.get(keyMethod);
                System.out.println(classSym.className + "." + methSym.methodName + " : "+ methodOffset);
                methodOffset += 8;
            }
            // Store offsets
            ArrayList<Integer> offsets = new ArrayList<>();
            offsets.add(fieldOffset);
            offsets.add(methodOffset);
            offsetTable.put(classSym.className, offsets);
        }
    }

    public static class ClassSymTable {
        public String className;
        public String parentClassName;
        public boolean mainClass;
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
        public Boolean override;
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
