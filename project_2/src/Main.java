import syntaxtree.*;

import java.io.*;

class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java Main [file1] [file2] ... [fileN]");
            System.exit(1);
        }
        FileInputStream fis = null;
        for (int i = 0; i < args.length; i++) {
            try {
                fis = new FileInputStream(args[i]);
                MiniJavaParser parser = new MiniJavaParser(fis);
                System.out.println("Parsing '"+args[i]+"'");
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
                    System.out.println("Parse Successful\n");
//                    symbolTable = null;
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                } finally {
//                    symbolTable = null;
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
