import java.util.Scanner;

public class REPL {
    public static void main(String[] args) {

    Calc c = new Calc();
    Scanner scan = new Scanner(System.in);

    int choice = 0;

    while(choice != 7) {
        System.out.println("1. Add\n2. Subtract\n3. Multiply\n4. Divide\n5. Random\n6. Reverse\n7. Exit");


        choice = scan.nextInt();

        if(choice == 1) {
            System.out.println("Enter the first number: ");
            int a = scan.nextInt();
            System.out.println("Enter the second number: ");
            int b = scan.nextInt();
            System.out.println("Add: " + c.add(a,b));
        }
        else if(choice == 2) {
            System.out.println("Enter the first number: ");
            int a = scan.nextInt();
            System.out.println("Enter the second number: ");
            int b = scan.nextInt();
            System.out.println("Subtract: " + c.sub(a,b));
        }
        else if(choice == 3) {
            System.out.println("Enter the first number: ");
            int a = scan.nextInt();
            System.out.println("Enter the second number: ");
            int b = scan.nextInt();
            System.out.println("Multiply: " + c.mul(a,b));
        }
        else if(choice == 4) {
            System.out.println("Enter the first number: ");
            int a = scan.nextInt();
            System.out.println("Enter the second number: ");
            int b = scan.nextInt();
            System.out.println("Divide: " + c.div(a,b));
        }
        else if(choice == 5) {
            System.out.println("Enter the first number: ");
            int a = scan.nextInt();
            System.out.println("Enter the second number: ");
            int b = scan.nextInt();
            System.out.println("Random Number: " + c.rand(a,b));
        }
        else if(choice == 6) {
            scan.nextLine();
            System.out.println("Enter a word: ");
            String a = scan.nextLine();
            System.out.println("Reverse: " + c.rev(a));
        }
        else if(choice == 7) {
            System.out.println("Goodbye!");
        }else {
            System.out.println("Invalid Input");
        }

    }

    }
}