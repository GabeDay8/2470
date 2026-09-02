import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class Main {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        List<Integer> numbers = new ArrayList<>();
        int total = 0;

        for (int i = 0; i < 5; i++) {
            System.out.println("Please enter number");
            numbers.add(scan.nextInt());

        }
        scan.close();

        int sum = numbers.stream().mapToInt(Integer::intValue).sum();
        System.out.println("Total: " + sum);
        double avg = sum / 5;
        System.out.println("Average: " + avg);
        int max = Collections.max(numbers);
        int min = Collections.min(numbers);
        System.out.println("Highest: " + max);
        System.out.println("Lowest: " + min);

        for (int num : numbers) {
            if (num >= 90) {
                System.out.println(num + " - A");
            } else if (num < 90 && num >= 80) {
                System.out.println(num + " - B");
            } else if (num < 80 && num >= 70) {
                System.out.println(num + " - C");
            } else if (num < 70 && num >= 60) {
                System.out.println(num + " - D");
            } else {
                System.out.println(num + " - F");
            }
        }

    }
}