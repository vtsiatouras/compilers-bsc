public class Parser {
    private String input;
    private char token;
    private int inputLength;
    private int tokenNumber;

    public void parser(String userInput) {
        this.input = userInput;
        this.inputLength = userInput.length();
        this.tokenNumber = 0;
        try {
            parser_goal();
        } catch (ParseError err) {
            System.err.println(err.getMessage());
        }
    }

    private void parser_token_consumer(int symbol) throws ParseError {
        if (this.token != symbol) {
            throw new ParseError();
        }
        // If we have read the whole input, assign '\0' to token
        // Else consume next character from input buffer
        if(this.tokenNumber == this.inputLength) {
            this.token = '\0';
        }
        else {
            this.token = this.input.charAt(this.tokenNumber);
            this.tokenNumber++;
        }
    }

    private void parser_goal() throws ParseError {
        parser_token_consumer(this.token);
        parser_expr();
        // Check for EOF
        if (this.token != '\0') {
            throw new ParseError();
        }
        System.out.println("parse successful");
    }

    private void parser_expr() throws ParseError {
        parser_term();
        parser_expr2();
    }


    private void parser_expr2() throws ParseError {
        // Check for EOF
        if (this.token == '\0') {
            return;
        }
        if (this.token == '+' || this.token == '-') {
            parser_token_consumer(this.token);
            parser_term();
            parser_expr2();
        }
    }

    private void parser_term() throws ParseError {
        parser_factor();
        parser_term2();
    }

    private void parser_term2() throws ParseError {
        // Check for EOF
        if (this.token == '\0') {
            return;
        }
        // Check for '*' and '/' literals
        if (this.token == '*' || this.token == '/') {
            parser_token_consumer(this.token);
            parser_factor();
            parser_term2();
        }
    }

    private void parser_factor() throws ParseError {
        // Check for EOF
        if (this.token == '\0') {
            throw new ParseError();
        }
        // Check for '(' literal
        if (this.token == '(') {
            parser_token_consumer(this.token);
            parser_expr();
            // Check if parenthesis is closing with ')' literal
            if (this.token != ')') {
                throw new ParseError();
            }
            parser_token_consumer(this.token);
        }
        else {
            // Check if the token is number between 0 and 9
            if (this.token < '0' || this.token > '9') {
                throw new ParseError();
            }
            parser_token_consumer(this.token);
        }
    }
}
