import syntaxtree.*;

import java.io.*;

class Main {
    public static void main(String[] args) {
        // todo na to kanw na pairnei polla arxeia
        if (args.length != 1) {
            System.err.println("Usage: java Main [file1] [file2] ... [fileN]");
            System.exit(1);
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(args[0]);
            MiniJavaParser parser = new MiniJavaParser(fis);
            SymbolTable symbolTable = new SymbolTable();
            FirstVisitor first_visit = new FirstVisitor();
            Goal root = parser.Goal();
            try {
                root.accept(first_visit, symbolTable);
                symbolTable.PrintSymbolTable();
            }
            catch(Exception ex) {
                System.err.println(ex.getMessage());
            }
            finally {
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
