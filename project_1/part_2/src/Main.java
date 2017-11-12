import java_cup.runtime.*;
import java.io.*;

class Main {
    public static void main(String[] argv) throws Exception {
        // Create "out" directory to store generated Main.java
        File dir = new File("out");
        // If the directory does not exist, create it
        if (!dir.exists()) {
            boolean result = false;
            try{
                dir.mkdir();
                result = true;
            }
            catch(Exception ex){
                System.out.println("something went really bad...");
            }
        }
        Parser p = new Parser(new Scanner(new InputStreamReader(System.in)));
        p.parse();
    }
}
