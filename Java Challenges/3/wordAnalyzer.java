import java.util.Scanner;

public class wordAnalyzer {
    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);

        System.out.println("Enter a word: ");
        String word = scan.nextLine();

        int charCount = word.length();
        System.out.println("Characters: "+charCount);

        int vowels = 0;
        int consonants = 0;
        int digits = 0;
        int spaces = 0;

        String lowerCase = word.toLowerCase();

        for(int i = 0; i < charCount; i++) {
            char ch = lowerCase.charAt(i);

            if(ch >= 'a' && ch <= 'z') {
                if(ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u') {
                    vowels++;
                } else {
                    consonants++;
                }
            }
            else if(ch >= '0' && ch <= '9') {
                digits++;
            }
            else if(ch == ' ') {
                spaces++;
            }
        }

        System.out.println("Vowels: "+vowels);
        System.out.println("Consonants: "+consonants);
        System.out.println("Digits: "+digits);
        System.out.println("Spaces: "+spaces);

    }
}
