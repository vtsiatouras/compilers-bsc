import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;

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

    void calculate_offsets(String fileName) {
        // Create "out" directory to store generated Main.java
        File dir = new File("v-tables");
        // If the directory does not exist, create it
        if (!dir.exists()) {
            dir.mkdir();
        }
        try {
            // Create file to store the V-Table
            File file = new File("v-tables/" + fileName + ".txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file, false);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);

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
                    fieldOffset = curOffset.get(0);
                    methodOffset = curOffset.get(1);
                } else {
                    fieldOffset = 0;
                    methodOffset = 0;
                }
                pw.println("-----------Class " + classSym.className + "-----------");
                pw.println("---Variables---");
                for (Map.Entry classEntryFields : classSym.fields.entrySet()) {
                    String type = classEntryFields.getValue().toString();
                    String var = classEntryFields.getKey().toString();
                    if (type.equals("int")) {
                        pw.println(classSym.className + "." + var + " : " + fieldOffset);
                        fieldOffset += 4;
                    } else if (type.equals("boolean")) {
                        pw.println(classSym.className + "." + var + " : " + fieldOffset);
                        fieldOffset += 1;
                    } else {
                        pw.println(classSym.className + "." + var + " : " + fieldOffset);
                        fieldOffset += 8;
                    }
                }
                pw.println("---Methods---");
                for (Map.Entry classEntryFunctions : classSym.methods.entrySet()) {
                    Object keyMethod = classEntryFunctions.getKey();
                    MethodSymTable methSym = classSym.methods.get(keyMethod);
                    // Ignore overriding methods
                    if (methSym.override) {
                        continue;
                    }
                    pw.println(classSym.className + "." + methSym.methodName + " : " + methodOffset);
                    methodOffset += 8;
                }
                pw.println();
                // Store offsets
                ArrayList<Integer> offsets = new ArrayList<>();
                offsets.add(fieldOffset);
                offsets.add(methodOffset);
                offsetTable.put(classSym.className, offsets);
            }
            pw.close();
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    public static class ClassSymTable {
        public String className;
        public String parentClassName;
        public Boolean mainClass;
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
