package program;

import java.util.List;

public class Parser {

    Tokenizer tokenizer;

    public Parser() throws Exception {
        tokenizer = new Tokenizer("src/resources/input.txt");
    }

    public Parser(String file_loc) throws Exception {
        tokenizer = new Tokenizer(file_loc);
    }

    public void run() throws Exception {
        nextSymbol();
        expression();

        if (!tokenizer.kind().equals("end-of-text")) {
            // TODO use report error function
            throw new Exception("Found symbol " + tokenizer.kind() + " at line " + tokenizer.position() + " expected end-of-text");
        }
    }

    public void expression() throws Exception {
        booleanExpression();
    }

    public void booleanExpression() throws Exception {
        booleanTerm();

        while (isIn(new String[]{"or"})) {
            nextSymbol();
            booleanTerm();
        }
    }

    public void booleanTerm() throws Exception {
        booleanFactor();

        while (isIn(new String[]{"and"})) {
            nextSymbol();
            booleanFactor();
        }
    }

    public void booleanFactor() throws Exception {
        if (isIn(new String[]{"not"})) {
            nextSymbol();
        }

        arithmeticExpression();

        if (isIn(new String[]{"=", "<"})) {
            nextSymbol();
            arithmeticExpression();
        }
    }

    public void booleanLiteral() throws Exception {
        if (isIn(new String[]{"true", "false"})) {
            nextSymbol();
        } else {
            throw new Exception("Expected: true, false");
        }
    }

    public void arithmeticExpression() throws Exception{
        term();

        while (isIn(new String[]{"+", "-"})) {
            nextSymbol();
            term();
        }
    }

    public void term() throws Exception {
        factor();

        while (isIn(new String[]{"*", "/"})) {
            nextSymbol();
            term();
        }
    }

    public void factor() throws Exception {
        if (isIn(new String[]{"true", "false", "NUM"})) {
            literal();
        } else if (isIn(new String[]{"ID"})) {
            nextSymbol();
        } else if (isIn(new String[]{"("})) {
            nextSymbol();
            expression();
            accept(")");
        } else {
            // TODO move this call to it's own method
            throw new Exception("Expected: true, false, NUM, ID, or ( instead got " + tokenizer.kind());
        }
    }

    public void literal() throws Exception {
        if (isIn(new String[]{"true", "false", "NUM"})) {
            if (isIn(new String[]{"NUM"})) {
                nextSymbol();
            } else {
                booleanLiteral();
            }
        } else {
            throw new Exception("Expected: true, false, NUM");
        }
    }

    public void accept(String symbol) throws Exception {
        if (tokenizer.kind().equals(symbol)) {
            nextSymbol();
        } else {
            // TODO move this call to it's own method
            throw new Exception("Expected " + symbol + " seeing " + tokenizer.kind());
        }
    }

    public void expected(List<String> symbols) throws Exception {
        if (!isIn((String[]) symbols.toArray())) {
            throw new Exception("Expected " + symbols + " seeing " + tokenizer.kind());
        }
    }

    public void nextSymbol() throws Exception{
        if (!isIn(new String[]{"end-of-text"})) {
            tokenizer.next();
        }
    }

    public boolean isIn(String[] symbols) throws Exception {
        for (String symbol: symbols) {
            if (symbol.equals(tokenizer.kind()))
                return true;
        }
        return false;
    }
}
