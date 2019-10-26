package program;

public class Client {
    public static void main(String[] args) {
        try {
            Parser parser = new Parser();
            parser.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}