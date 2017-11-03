public class Parser {
    private String input;
    private int inputLength;
    private int tokenNumber;

    public void parser(String userInput) {
        System.out.println(userInput);
        input = userInput;
        inputLength = userInput.length();
        tokenNumber = 0;
        for(int i = 0; i < inputLength; i++) {
            System.out.println(parser_token_consumer());
        }
        // Call parser_goal and implement recursion to below functions
    }
    public char parser_token_consumer () {
        char token = input.charAt(tokenNumber);
        tokenNumber++;
        return token;
    }

    public void parser_goal() {

    }

    public void parser_expr() {

    }

    public void parser_expr2() {

    }

    public void parser_factor() {

    }
}
