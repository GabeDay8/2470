import java.util.ArrayList;
import java.util.List;

public class isValid {
    public static List<String> main(String password) {

        List<String> errors = new ArrayList<>();

        if (password == null || password.length() < 8) {
            errors.add("Must be at least 8 characters");
        }
        if (!password.matches(".*[A-Z].*")) {
            errors.add("Must contain an uppercase letter");
        }
        if (!password.matches(".*[a-z].*")) {
            errors.add("Must contain a lowercase letter");
        }
        if (!password.matches(".*[0-9].*")) {
            errors.add("Must contain a number");
        }

        return errors;

    }
}
