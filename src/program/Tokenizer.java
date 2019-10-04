package program;

import java.io.File;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {
    // The keywords of the mini-language
    private static final String[] KEYWORDS =
            {"or", "and", "not", "=", "<", "+", "-", "*", "/", "(", ")", "false", "true", "begin", "end", "bool", "int",
                    ";", ":=", "if", "then", "else", "fi", "while", "do", "od", "print", "<=", ">=", ">", "_"};

    // ([A-z]+[_0-9A-z]*)                                               Identifier and text pattern
    // ([0-9]+)                                                         Digit pattern
    // (:=)|(<=)|(>=)|(=)|(<)|(>)|(;)|(\+)|(-)|(\/)|(\*)|(\()|(\))|(_)  Operator pattern
    // [^A-z0-9_ \t\n+-=\*\/<>();:]|[.]                                     Invalid character pattern
    // //.*                                                             Comment pattern
    // |                                                                Or

    // Regex that breaks it up into IDs, Numbers, or Keywords
    private final Pattern tokenizer = Pattern.compile("(_)|([A-z]+[_0-9A-z]*)|([0-9]+)|(:=)|(<=)|(>=)|(=)|(<)|(>)|(;)|(\\+)|(-)|(\\/)|(\\*)|(\\()|(\\))");
    // Regex that checks if the input contains any invalid characters
    private final Pattern linter = Pattern.compile("[^A-z0-9_ \\t\\n+-=\\*\\/<>();:]|[.]");

    private Scanner scanner;
    private Token currentToken;

    private int line = 0;
    private int position = 0;
    private String currLine;
    private boolean error = false;

    private String input;

    /**
     * Checks if the input has any characters it should not have, sets the scanner equal to the input, and creates a
     * new blank token to read from
     * @param file_loc string location of the file to tokenize
     * @throws Exception if exception is thrown in delinting process
     */
    public Tokenizer(String file_loc) throws Exception {
        try {
            readInputFile(file_loc);
            decommentText();
            scanner = new Scanner(this.input);
            currentToken = new Token();
        } catch (Exception e) {
            error = true;
            throw e;
        }
    }

    /**
     * Reads the input file and parses it into a string format that the program can read
     * @param file_loc string location of the file
     * @throws Exception when it cannot find the file location
     */
    private void readInputFile(String file_loc) throws Exception {
        Scanner scanner = new Scanner(new File(file_loc));
        input = "";
        while (scanner.hasNextLine()) {
            String str = scanner.nextLine();
            if (scanner.hasNextLine())
                input = input + str + "\n";
            else
                input = input + str;
        }
        scanner.close();
    }

    /**
     * Goes through the input string and replaces comments with empty characters
     * @throws Exception when the matcher finds an invalid character
     */
    private void decommentText() throws Exception {
        // Remove comments from the input and replace it with spaces
        input = input.replaceAll("//.*", "");
    }

    /**
     * Checks to see if the next input is a invalid character
     * @param line the next line to read
     * @throws Exception when it finds a illegal character (i.e. $, #, &)
     */
    private void detectIllegalCharacter(String line) throws Exception {
        Matcher matcher = linter.matcher(line);

        if (matcher.find(position)) {
            String character = matcher.group();
            // Get a string that can be checked against the character found given the current position
            String str = line.substring(position).replaceAll(" ", "").substring(0, 1);
            if (str.equals(character)) {
                error = true;
                throw new Exception("Invalid character " + character + " at line " + this.line + " position "
                        + (matcher.start() + 1));
            }
        }
    }

    /**
     * Reads the next value to be tokenized and tokenizes it according to the value's type
     * @throws Exception if there was an error in initializing and delinting the input
     */
    public void next() throws Exception {
        if (error) {
            throw new Exception("Error linting input, cannot get next token");
        }

        String lint = currLine == null ? "" : currLine;
        detectIllegalCharacter(lint);
        Matcher matcher = tokenizer.matcher(lint);

        // If matcher cannot find another value in current line, jump to next
        if (!matcher.find(position) && scanner.hasNextLine()) {
            currLine = scanner.nextLine();
            lint = currLine;
            matcher = tokenizer.matcher(currLine);
            line++;
            position = 0;
        }

        detectIllegalCharacter(lint);

        // Find the next position that matches the regex, if it doesn't exist you are at the end of the line
        if (matcher.find(position)) {
            String str = matcher.group();

            if (isKeyword(str)) {
                currentToken = new Token(str, null, line, matcher.start() + 1);
            } else if(isNumber(str)) {
                currentToken = new Token("NUM", str, line, matcher.start() + 1);
            } else {
                currentToken = new Token("ID", str, line, matcher.start() + 1);
            }

            position = matcher.end();
        } else {
            if (!scanner.hasNextLine()) {
                line++;
                position = 0;
                currentToken = new Token("end-of-text", null, line, 0);
            } else {
                next();
            }
        }
    }

    /**
     * Returns the kind of the recently read token
     * @throws Exception if there was an error in initializing and delinting the input
     * @return the kind of the token
     */
    public String kind() throws Exception {
        if (error) {
            throw new Exception("Error linting input, cannot get kind of token");
        }
        return currentToken.kind;
    }

    /**
     * Returns the value of the recently read token
     * @throws Exception if there was an error in initializing and delinting the input
     * @return the value of the token
     */
    public String value() throws Exception {
        if (error) {
            throw new Exception("Error linting input, cannot get value of token");
        }
        return currentToken.value;
    }

    /**
     * Returns the position, in String form, of the recently read token
     * @throws Exception if there was an error in initializing and delinting the input
     * @return the position of the token
     */
    public String position() throws Exception {
        if (error) {
            throw new Exception("Error linting input, cannot get position of token");
        }
        return "Line: " + currentToken.line + " Position: " + currentToken.position;
    }

    /**
     * Checks if a given string, read from the input, is a keyword or not
     * @param str the string to check
     * @return true if a keyword, false otherwise
     */
    private boolean isKeyword(String str) {
        boolean flag = false;
        for (String keyword: KEYWORDS) {
            if (keyword.equals(str)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     * Checks if a given string, read from the input, is a number or not
     * @param str the string to check
     * @return true if a number, false otherwise
     */
    private boolean isNumber(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Inner class used to easily structure, create, and read tokens
     */
    private class Token {
        // https://stackoverflow.com/questions/34806693/java-regex-get-line-number-from-matching-text
        int line;
        int position;
        String kind;
        String value;

        public Token() {
            this.position = -1;
            this.kind = "";
            this.value = "";
        }

        public Token(String kind, String value, int line, int position) {
            this.line = line;
            this.position = position;
            this.kind = kind;
            this.value = value;
        }

        @Override
        public String toString() {
            return "< " + kind + ", " + value + " >";
        }
    }
}
