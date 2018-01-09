import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class VTables {

    public LinkedHashMap<String, ClassVTable> classesTables;

    VTables() {
        classesTables = new LinkedHashMap<>();
    }

    public static class ClassVTable {
        public LinkedHashMap<String, Integer> fieldsTable;
        public LinkedHashMap<String, Integer> methodsTable;

        ClassVTable() {
            fieldsTable = new LinkedHashMap<>();
            methodsTable = new LinkedHashMap<>();
        }
    }

    VTables create_v_tables(SymbolTable symbolTable) {

        VTables vTables = new VTables();
        int fieldOffset, methodOffset;
        String mainClassName = null;
        // Local data structure to store offsets for each class
        HashMap<String, ArrayList<Integer>> offsetTable = new HashMap<>();
        for (Map.Entry entry : symbolTable.classes.entrySet()) {
            Object key = entry.getKey();
            SymbolTable.ClassSymTable classSym = symbolTable.classes.get(key);
            // Ignore main class
            if (classSym.mainClass) {
                mainClassName = classSym.className;
                continue;
            }
            vTables.classesTables.put(classSym.className, new VTables.ClassVTable());
            VTables.ClassVTable classVTable = vTables.classesTables.get(classSym.className);
            // If it is child class get parent's offset
            if (classSym.parentClassName != null && !classSym.parentClassName.equals(mainClassName)) {
                ArrayList<Integer> curOffset = offsetTable.get(classSym.parentClassName);
                fieldOffset = curOffset.get(0);
                methodOffset = curOffset.get(1);
            } else {
                fieldOffset = 0;
                methodOffset = 0;
            }
            for (Map.Entry classEntryFields : classSym.fields.entrySet()) {
                String type = classEntryFields.getValue().toString();
                String var = classEntryFields.getKey().toString();
                if (type.equals("int")) {
                    classVTable.fieldsTable.put(var, fieldOffset);
                    fieldOffset += 4;
                } else if (type.equals("boolean")) {
                    classVTable.fieldsTable.put(var, fieldOffset);
                    fieldOffset += 1;
                } else {
                    classVTable.fieldsTable.put(var, fieldOffset);
                    fieldOffset += 8;
                }
            }
            for (Map.Entry classEntryFunctions : classSym.methods.entrySet()) {
                Object keyMethod = classEntryFunctions.getKey();
                SymbolTable.MethodSymTable methSym = classSym.methods.get(keyMethod);
                // Ignore overriding methods
                if (methSym.override) {
                    continue;
                }
                classVTable.methodsTable.put(methSym.methodName, methodOffset);
                methodOffset += 8;

            }
            // Store offsets
            ArrayList<Integer> offsets = new ArrayList<>();
            offsets.add(fieldOffset);
            offsets.add(methodOffset);
            offsetTable.put(classSym.className, offsets);
        }
        return vTables;
    }

    void print_v_tables() {
        for (Map.Entry entry : classesTables.entrySet()) {
            Object key = entry.getKey();
            String className = entry.getKey().toString();
            ClassVTable classVTable = classesTables.get(key);
            System.out.println("=====Class " + className + "=====");
            System.out.println("---Fields---");
            for (Map.Entry classVTableEntryFields : classVTable.fieldsTable.entrySet()) {
                String fieldName = classVTableEntryFields.getKey().toString();
                Integer offset = Integer.parseInt(classVTableEntryFields.getValue().toString());
                System.out.println(fieldName + " " + offset);
            }
            System.out.println("---Methods---");
            for (Map.Entry classVTableEntryMethods : classVTable.methodsTable.entrySet()) {
                String methodName = classVTableEntryMethods.getKey().toString();
                Integer offset = Integer.parseInt(classVTableEntryMethods.getValue().toString());
                System.out.println(methodName + " " + offset);
            }
            System.out.println();
        }
    }
}
