import java.io.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Parser parser = new Parser();
        Scanner scanner = new Scanner(System.in);
        String input;
        // Read input
        while (scanner.hasNextLine()) {
            input = scanner.nextLine();
//            System.out.println(input);
            parser.parser(input);
        }
        scanner.close();
    }
}
