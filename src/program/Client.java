package program;

public class Client {
    public static void main(String[] args) {
        try {
            // NOTE: You can either point to a new file location or modify the existing txt file
            Tokenizer tokenizer = new Tokenizer("src/resources/input.txt");
            while (!tokenizer.kind().equals("end-of-text")) {
                tokenizer.next();
                System.out.println("\n" + tokenizer.position());
                System.out.println("< " + tokenizer.kind() + ", " + tokenizer.value() + " >");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}