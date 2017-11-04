import java.io.IOException;

public class Parser {
    private String input;
    private char token;
    private int inputLength;
    private int tokenNumber;

    public void parser(String userInput) {
//        System.out.println(userInput);
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
        if (this.token == '\0') {
            return;
        }
        if (this.token == '*' || this.token == '/') {
            parser_token_consumer(this.token);
            parser_factor();
            parser_term2();
        }
    }

    private void parser_factor() throws ParseError {
//        if (this.token == '(') {
//            parser_token_consumer();
//            if (this.token == ')')
//                parser_expr();
//        }
        if (this.token == '\0') {
            throw new ParseError();
        }
        if (this.token < '0' || this.token > '9') {
            throw new ParseError();
        }
        parser_token_consumer(this.token);
    }
}
