import java.io.InputStream;
import java.io.IOException;

class TernaryOperatorParser {

    private int lookaheadToken;

    private InputStream in;

    public TernaryOperatorParser(InputStream in) throws IOException {
	this.in = in;
	lookaheadToken = in.read();
    }

    private void consume(int symbol) throws IOException, ParseError {
	if (lookaheadToken != symbol)
	    throw new ParseError();
	lookaheadToken = in.read();
    }

    private void Tern() throws IOException, ParseError {
	if(lookaheadToken < '0' || lookaheadToken > '9')
	    throw new ParseError();
	consume(lookaheadToken);
	TernTail();
    }

    private void TernTail() throws IOException, ParseError {
	if(lookaheadToken == ':' || lookaheadToken == '\n' || lookaheadToken == -1)
	    return;
	if(lookaheadToken != '?')
	    throw new ParseError();
	consume('?');
	Tern();
	consume(':');
	Tern();
    }

    public void parse() throws IOException, ParseError {
	Tern();
	if (lookaheadToken != '\n' && lookaheadToken != -1)
	    throw new ParseError();
    }

    public static void main(String[] args) {
	try {
	    TernaryOperatorParser parser = new TernaryOperatorParser(System.in);
	    parser.parse();
	}
	catch (IOException e) {
	    System.err.println(e.getMessage());
	}
	catch(ParseError err){
	    System.err.println(err.getMessage());
	}
    }
}

