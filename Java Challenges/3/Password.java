import java.util.Scanner;
import java.util.List;

public class Password {
    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);

        System.out.println("Enter a password: ");
        String password = scan.nextLine();

        List<String> validationErrors = isValid.main(password);
        if(validationErrors.isEmpty()) {
            System.out.println("Password accepted!");
        } else {
            System.out.println("Password rejected:");
            for(String error : validationErrors) {
                System.out.println(error);
            }
        }

    }
}
