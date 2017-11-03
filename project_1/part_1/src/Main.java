import java.io.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Parser parser = new Parser();
        Scanner scanner = new Scanner(System.in);
        String input;
        // Read lines
        // TODO add exception to handle ctrl+D to terminate gracefully
        while ((input= scanner.nextLine()) != null) {
            System.out.println(input);
//            parser.parser(input);
        }
    }
}
