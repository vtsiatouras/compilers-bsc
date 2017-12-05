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
                    root.accept(firstVisitor, symbolTable);
                    symbolTable.PrintSymbolTable();
//                    root.accept(secondVisitor, symbolTable);
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                } finally {
                    symbolTable = null;
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
