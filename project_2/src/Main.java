import syntaxtree.*;

import java.io.*;

class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java Main [file1] [file2] ... [fileN]");
            System.exit(1);
        }
        FileInputStream fis = null;
//        for (int i = 0; i < args.length; i++) {
        for (String inputFile : args) {
            try {
                fis = new FileInputStream(inputFile);
                MiniJavaParser parser = new MiniJavaParser(fis);
                System.err.println("\nParsing '" + inputFile + "'");
                System.out.flush();
                SymbolTable symbolTable = new SymbolTable();
                FirstVisitor firstVisitor = new FirstVisitor();
                SecondVisitor secondVisitor = new SecondVisitor();
                Goal root = parser.Goal();
                try {
                    // Store all the critical information (i.e. class names, fields, methods, params, variables)
                    // in the symbol table.
                    root.accept(firstVisitor, symbolTable);
//                    symbolTable.print_symbol_table();
                    symbolTable.type_check_symbol_table();
                    // Typecheck the given program
                    root.accept(secondVisitor, symbolTable);
                    System.err.println("Parse Successful");
//                    symbolTable.calculate_offsets();
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
