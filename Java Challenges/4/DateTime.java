import java.time.LocalDate;
import java.time.Period;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Scanner;

public class DateTime {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        LocalDate today = LocalDate.now();

        System.out.println("Date: " + today);
        System.out.println("Year: " + today.getYear());
        System.out.println("Month: " + today.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase());
        System.out.println("Day: " + today.getDayOfMonth());

        System.out.print("\nEnter your birth date (YYYY-MM-DD): ");
        LocalDate birthDate = LocalDate.parse(scanner.nextLine().trim());

        Period agePeriod = Period.between(birthDate, today);
        System.out.println("You are " + agePeriod.getYears() + " years old.");

        LocalDate nextBirthday = birthDate.withYear(today.getYear());
        if (nextBirthday.isBefore(today) || nextBirthday.isEqual(today)) {
            nextBirthday = nextBirthday.plusYears(1);
        }
        long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, nextBirthday);
        System.out.println("Days until your next birthday: " + daysUntil);

        scanner.close();
    }
}