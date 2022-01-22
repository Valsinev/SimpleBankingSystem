package banking;

public class Printer {

    public static void mainMenuPrinter() {
        System.out.println("1. Create an account");
        System.out.println("2. Log into account");
        System.out.println("0. Exit");
    }
    public static void accountMenuPrinter() {
        System.out.println("1. Balance");
        System.out.println("2. Add income");
        System.out.println("3. Do transfer");
        System.out.println("4. Close account");
        System.out.println("5. Log out");
        System.out.println("0. Exit");
    }

    public static void logOutMessage() {
        System.out.println("You have successfully logged out!");
    }

    public static void goodbye() {
        System.out.println("Bye!");
    }

    public static void cardCreatedMessage() {
        System.out.println("Your card has been created");
    }

    public static void wrongNumberOrCardMessage() {
        System.out.println("Wrong card number or PIN!");
    }
}
