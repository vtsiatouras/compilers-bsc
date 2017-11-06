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

    private void parser_token_consumer() throws ParseError {
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

    private int parser_evaluator(int digit) {
        return digit - '0';
    }

    private void parser_goal() throws ParseError {
        parser_token_consumer();
        int resultValue = parser_expr(0);
        // Check for EOF
        if (this.token != '\0') {
            throw new ParseError();
        }
        System.out.println("parse successful");
        System.out.println(resultValue);
    }

    private int parser_expr(int result) throws ParseError {
        result = parser_term(result);
        result = parser_expr2(result);
        return result;
    }


    private int parser_expr2(int result) throws ParseError {
        // Check for EOF
        if (this.token == '\0') {
            return result;
        }
        if (this.token == '+' ) {
            parser_token_consumer();
            result += parser_term(result);
            result = parser_expr2(result);
        }
        else if(this.token == '-'){
            parser_token_consumer();
            result -= parser_term(result);
            result = parser_expr2(result);
        }
        return result;
    }

    private int parser_term(int result) throws ParseError {
        result = parser_factor(result);
        result = parser_term2(result);
        return result;
    }

    private int parser_term2(int result) throws ParseError {
        // Check for EOF
        if (this.token == '\0') {
            return result;
        }
        // Check for '*' and '/' literals
        if (this.token == '*') {
            parser_token_consumer();
            result *= parser_factor(result);
            result = parser_term2(result);

        } else if(this.token == '/') {
            parser_token_consumer();
            result = result /parser_factor(result);
            result = parser_term2(result);
        }
        return result;
    }

    private int parser_factor(int result) throws ParseError {
        // Check for EOF
        if (this.token == '\0') {
            throw new ParseError();
        }
        // Check for '(' literal
        if (this.token == '(') {
            parser_token_consumer();
            result = parser_expr(result);
            // Check if parenthesis is closing with ')' literal
            if (this.token != ')') {
                throw new ParseError();
            }
            parser_token_consumer();
            return result;
        }
        else {
            // Check if the token is number between 0 and 9
            if (this.token < '0' || this.token > '9') {
                throw new ParseError();
            }
            result = parser_evaluator(this.token);
            parser_token_consumer();
            return result;
        }
    }
}
