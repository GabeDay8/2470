void main() {
    //Challenge 1
    int age = 23;
    double height = 5.9;
    String name = "Gabe";
    System.out.println(name + " is " + age + " years old and " + height + " feet tall.");

    //Challenge 2
    int a = 20;
    int b = 10;
    int add = a + b;
    int sub = a - b;
    int mul = a * b;
    int div = a / b;
    boolean c = a > b;
    boolean d = b > 0;
    System.out.println("Addition: "+add);
    System.out.println("Subtraction: "+sub);
    System.out.println("Multiplication: "+mul);
    System.out.println("Division: "+div);
    System.out.println("Is a > b? "+c);
    System.out.println("Is b > 0?"+d);

    //Challenge 3
    int grade = 75;
    String letterGrade = "B";
    if(grade > 50) {
        System.out.println("Passed");
        if(grade >= 90) {
            System.out.println("Grade: A");
        }
        else if(grade <= 89 && grade >= 75) {
            System.out.println("Grade: B");
        }
        else if(grade <= 74 && grade >= 60) {
            System.out.println("Grade: C");
        }
        else {
            System.out.println("Grade: D");
        }
    } else {
        System.out.println("Failed");
    }

    //Challenge 4
    System.out.print("For Loop: ");
    for(int i = 1; i <= 5; i++ ) {
            System.out.print(" "+i);
    }
    System.out.println();
    System.out.print("While Loop: ");
    int i =1;
    while(i <=5) {
        System.out.print(" "+i);
        i++;
    }
    System.out.println();
    System.out.print("Do-While Loop: ");
    int j=1;
    do {
        System.out.print(" "+j);
        j++;
    } while(j <= 5);
    System.out.println();

    //Challenge 5
    double num1 = 7;
    double num2 = 3;
    char operator = '+';
    String again = "y";

    while (again.equals("y")) {
        if (operator == '+') {
            double result = num1 + num2;
            System.out.println("Result: " + result);
        } else if (operator == '-') {
            double result = num1 - num2;
            System.out.println("Result: " + result);
        } else if (operator == '*') {
            double result = num1 * num2;
            System.out.println("Result: " + result);
        } else if (operator == '/') {
            if (num2 == 0) {
                System.out.println("Cannot divide by zero.");
            } else {
                double result = num1 / num2;
                System.out.println("Result: " + result);
            }
        }

        again = "n";
    }
    System.out.println("Thank you for using the calculator.");
}

