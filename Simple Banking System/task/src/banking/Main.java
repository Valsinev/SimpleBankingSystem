package banking;

import org.sqlite.SQLiteDataSource;

import java.sql.*;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        //connecting to DB
        String url = "jdbc:sqlite:./" + args[1];
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        String insert = "INSERT INTO card (number, pin) VALUES (?, ?)";
        String selectCard = "SELECT * FROM card WHERE number = ? AND pin = ?";
        String updateBalance = "UPDATE card SET balance = ? WHERE number = ?";
        String deleteCard = "DELETE FROM card WHERE id = ?";
        String selectByNumber = "SELECT * FROM card WHERE number = ?";

        try (Connection con = dataSource.getConnection()) {
                Statement statement = con.createStatement();
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS card(" +
                        "id INTEGER PRIMARY KEY," +
                        "number TEXT," +
                        "pin TEXT," +
                        "balance INTEGER DEFAULT 0)");

                Scanner scanner = new Scanner(System.in);
                Random random = new Random();
                int userInput;
                int loggedInput;

                do {
                    Printer.mainMenuPrinter();

                    userInput = scanner.nextInt();

                    switch (userInput) {
                        case 1: {
                            int pinCreator = random.nextInt(10000 - 1000) + 1000;
                            long cardNcreator = cardNGenerator();

                            Printer.cardCreatedMessage();
                            System.out.println("Your card number:");
                            System.out.println(cardNcreator);
                            System.out.println("Your card PIN:");
                            System.out.println(pinCreator);

                            //add card to DB//
                            try (PreparedStatement addCardToDB = con.prepareStatement(insert)) {
                                addCardToDB.setLong(1, cardNcreator);
                                addCardToDB.setInt(2, pinCreator);
                                addCardToDB.executeUpdate();
                            }

                        }
                        break;
                        case 2: {
                            System.out.println("Enter your card number:");
                            long cardNumberInput = scanner.nextLong();
                            System.out.println("Enter your PIN:");
                            int PINInput = scanner.nextInt();

                            PreparedStatement accSelect = con.prepareStatement(selectCard);

                            accSelect.setLong(1, cardNumberInput);
                            accSelect.setInt(2, PINInput);

                            ResultSet accFind = accSelect.executeQuery();

                            if (accFind.next()) {

                                System.out.println("\nYou have successfully logged in!\n");

                                do {
                                    accFind = accSelect.executeQuery();
                                    Printer.accountMenuPrinter();
                                    loggedInput = scanner.nextInt();

                                    switch (loggedInput) {
                                        case 0:
                                            Printer.goodbye();
                                            return;
                                        case 1:
                                            System.out.printf("Balance: %2f", accFind.getDouble("balance"));
                                            System.out.println();
                                            break;
                                        case 2: {
                                            System.out.println("Enter income:");
                                            double income = scanner.nextDouble();
                                            PreparedStatement addIncome = con.prepareStatement(updateBalance);
                                            addIncome.setDouble(1, accFind.getDouble("balance") + income);
                                            addIncome.setString(2, accFind.getString("number"));
                                            addIncome.executeUpdate();
                                            System.out.println("Income was added!");
                                        }
                                        break;
                                        case 3: {
                                            System.out.println("Transfer");
                                            System.out.println("Enter card number:");
                                            String cardToDoTransferTo = scanner.next();
                                            PreparedStatement st = con.prepareStatement(selectByNumber);
                                            st.setString(1, cardToDoTransferTo);
                                            ResultSet cardToTransfer = st.executeQuery();
                                        if (!luhnAlgorithmChecker(Long.parseLong(cardToDoTransferTo))) {
                                            System.out.println("Probably you made a mistake in the card number. Please try again!");
                                        } else if (!cardToTransfer.next()) {
                                            System.out.println("Such a card does not exist.");
                                        } else {
                                                System.out.println("Enter how much money you want to transfer:");
                                                double moneyToTransfer = scanner.nextDouble();
                                                if (!balanceValidator(accFind.getLong("balance"), moneyToTransfer)) {
                                                    System.out.println("Not enough money!");
                                                } else {
                                                    PreparedStatement transfer = con.prepareStatement(updateBalance);
                                                    transfer.setDouble(1, (accFind.getDouble("balance") - moneyToTransfer));
                                                    transfer.setString(2, accFind.getString("number"));
                                                    transfer.executeUpdate();

                                                    transfer.setDouble(1, (cardToTransfer.getDouble("balance") + moneyToTransfer));
                                                    transfer.setString(2, cardToDoTransferTo);

                                                    transfer.executeUpdate();
                                                    System.out.println("Success!");

                                                }
                                            }
                                        }
                                        break;
                                        case 4: {
                                            try (PreparedStatement delete = con.prepareStatement(deleteCard)) {
                                                delete.setInt(1, accFind.getInt("id"));
                                                delete.executeUpdate();
                                                System.out.println("The account has been closed!");
                                                loggedInput = 5;
                                            }
                                        }
                                        break;
                                        default:
                                            break;
                                    }
                                } while (loggedInput != 5);
                                Printer.logOutMessage();
                            } else {
                                Printer.wrongNumberOrCardMessage();
                            }
                        }
                        break;
                    }
                } while (userInput != 0);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        Printer.goodbye();
    }

    private static boolean balanceValidator(double balance  , double ammount) {
        return !(ammount > balance);
    }


    private static boolean luhnAlgorithmChecker(long cardNumber) {
        int lastDigit = (int)(cardNumber % 10);
        long firstFifteenDigits = cardNumber / 10;
        return luhnAlgorithm(firstFifteenDigits) == lastDigit;
    }

    private static long cardNGenerator() {
        String accountIdentifier = "400000";
        String BIN = String.valueOf(new Random().nextInt(1000000000 - 100000000) + 100000000);
        int lastDigit = luhnAlgorithm(Long.parseLong(accountIdentifier + BIN));
        return Long.parseLong(accountIdentifier + BIN + lastDigit);
    }

    private static int luhnAlgorithm(long fifteenDigitNumber) {
        String numToString = Long.toString(fifteenDigitNumber);
        int[] digits = new int[numToString.length()];
        int sumOfDigits = 0;
        int result;
        for (int i = 0; i < digits.length; i++) {
            digits[i] = numToString.charAt(i) - '0';
        }
        for (int i = digits.length - 1; i >= 0 ; i -= 2) {
            digits[i] *= 2;
            if (digits[i] > 9) {
                digits[i] -= 9;
            }
        }
        for (int i = 0; i < digits.length; i++) {
            sumOfDigits += digits[i];
        }
        if (sumOfDigits % 10 == 0) {
            result = 0;
        } else {
            result = 10 - sumOfDigits % 10;
        }
        return result;
    }

}