import syntaxtree.*;

import java.io.*;

class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java Main [file1] [file2] ... [fileN]");
            System.exit(1);
        }
        FileInputStream fis = null;
        for (String inputFile : args) {
            try {
                fis = new FileInputStream(inputFile);
                MiniJavaParser parser = new MiniJavaParser(fis);
                System.err.println("\nParsing '" + inputFile + "'");
                System.out.flush();
                SymbolTable symbolTable = new SymbolTable();
                VTables vTables = new VTables();
                TypeCheckFirstVisitor typeCheckFirstVisitor = new TypeCheckFirstVisitor();
                TypeCheckSecondVisitor typeCheckSecondVisitor = new TypeCheckSecondVisitor();
                Goal root = parser.Goal();
                try {
                    // Store all the critical information (i.e. class names, fields, methods, params, variables)
                    // in the symbol table.
                    root.accept(typeCheckFirstVisitor, symbolTable);
                    // Type check the given program
                    symbolTable.type_check_symbol_table();
                    root.accept(typeCheckSecondVisitor, symbolTable);
                    System.err.println("Parse Successful");
                    // Preparations to calculate V-Table
                    // Get file name without absolute path
                    File f = new File(inputFile);
                    String fileName = f.getName();
                    // Crop .java
                    fileName = fileName.substring(0, fileName.length() - 5);
                    // Calculate offsets
                    vTables = vTables.create_v_tables(symbolTable);
//                    vTables.print_v_tables();
                    LLVMGenerateVisitor llvmVisitor = new LLVMGenerateVisitor(fileName/*, vTables*/);
                    root.accept(llvmVisitor, vTables);
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                }
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            } catch (FileNotFoundException ex) {
                System.err.println(ex.getMessage());
            } finally {
                try {
                    if (fis != null) fis.close();
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                }
            }
        }
    }
}
