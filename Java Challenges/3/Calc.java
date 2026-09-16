import java.util.Random;
public class Calc {

    Random random = new Random();

    public int add(int a, int b) {
        return a + b;
    }

    public int sub(int a, int b) {
        return a - b;
    }

    public int mul(int a, int b) {
        return a * b;
    }

    public int div(int a, int b) {
        return a / b;
    }

    public int rand(int a, int b) {
        return random.nextInt(a,b);
    }

    public String rev(String a) {
        String r = new StringBuilder(a).reverse().toString();
        return r;
    }
}
