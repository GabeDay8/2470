import java.util.Scanner;

public class Main2 {
    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);
        int balance = 0;
        int choice = 0;

        while(choice != 4) {

            System.out.println("1. Check Balance" +
                    "2. Deposit" +
                    "3. Withdraw" +
                    "4. Exit");

            choice = scan.nextInt();

            if(choice == 1) {
                System.out.println("Balance: "+balance);
            }
            else if(choice == 2) {
                System.out.println("How much would you like to deposit: ");
                int dep = scan.nextInt();
                balance = balance + dep;
                System.out.println("New balance: "+balance);
            }
            else if(choice == 3) {
                System.out.println("How much would you like to withdraw: ");
                int draw = scan.nextInt();
                int testDraw = balance - draw;
                if (testDraw > 0) {
                    balance = balance - draw;
                    System.out.println("New balance: " + balance);
                } else {
                    System.out.println("Can't withdraw more than balance amount");
                }
            }
            else if(choice == 4) {
                System.out.println("Goodbye!");
                } else {
                System.out.println("Invalid input");
            }
        }
    }
}
