package program;

public class Client {
    public static void main(String[] args) {
        try {
            Parser parser = new Parser();
            System.out.println(parser.run());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}