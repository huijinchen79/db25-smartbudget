package com.smartbudget.console;

import com.smartbudget.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

// ============================================================
// TICKET-F016 to F020 (Day 2, Sprint 1) — Console Menu Application
// ============================================================
//
// WHAT: This is a standalone Java program (NOT a Spring class).
//       It uses a while loop + Scanner to create an interactive text menu.
//       Run this class directly in your IDE: right-click → Run 'Main.main()'
//
// WHY:  Before building REST APIs (Day 5-6), you practice Java basics:
//       ArrayList, Scanner input, loops, String.format, if-else validation.
//       This is your first runnable code in the SmartBudget project.
//
// ============================================================
public class Main {

    /*
     * The list is static because all static menu methods need access to it.
     *
     * ArrayList is used because transactions can be added dynamically while
     * the application is running.
     */
    private static final List<Transaction> TXNS = new ArrayList<>();

    public static void main(String[] args) {

        // -------------------------------------------------------
        // TODO TICKET-F017: Step 1 — Create sample data
        // STATUS: Completed in seed().
        // -------------------------------------------------------
        //
        // WHAT: An ArrayList is a resizable list that can grow/shrink dynamically.
        //       Unlike arrays (fixed size), ArrayList lets you add/remove items anytime.
        //
        // HOW:  Create an ArrayList and add 10+ hardcoded "transactions" to it.
        //
        //       Original learning-task wording:
        //       Since you haven't built the Transaction class yet, use a simple approach:
        //       each transaction can be a String[] with fields:
        //       id, description, type, amount, date.
        //       Add items using the .add() method.
        //
        //       CURRENT IMPLEMENTATION:
        //       The Transaction class now exists, so this version uses:
        //       List<Transaction>
        //
        // WHY:  This gives you data to display and filter before the database is connected.
        //
        // OBSERVE: After creating the list, print its .size() to verify — should be 10+.

        seed(); // Seed the data on startup

        // -------------------------------------------------------
        // TODO TICKET-F016: Step 2 — Build the menu loop
        // STATUS: Completed below.
        // -------------------------------------------------------
        //
        // WHAT: A while loop with a Scanner creates a text-based menu.
        //       Scanner reads keyboard input from the user.
        //       The loop runs until the user chooses "Exit".
        //
        // HOW:  Create a Scanner object for System.in.
        //       Use a boolean variable (e.g., "running = true") to control the loop.
        //       Inside the loop:
        //         1. Print the menu options (1. List, 2. Add, 3. Summary, 4. Exit)
        //         2. Read the user's choice with scanner.nextInt()
        //         3. IMPORTANT: call scanner.nextLine() after nextInt()
        //            to consume the leftover newline
        //         4. Use a switch statement to handle each option
        //         5. Option 4 sets running = false to exit the loop
        //
        // WHY:  This teaches control flow (while, switch) and user input handling.
        //       The nextLine() trick after nextInt() is a common Java gotcha —
        //       without it, the next Scanner read skips input unexpectedly.
        //
        // OBSERVE: Run the program. You should see the menu. Type 1, 2, 3, 4.
        //          Each option should do something different.
        //          Typing 4 should exit.

        // -------------------------------------------------------
        // TODO TICKET-F018: Step 3 — Formatted output with printf
        // STATUS: Completed in listTransactions().
        // -------------------------------------------------------
        //
        // WHAT: System.out.printf() lets you format output in aligned columns.
        //       Format specifiers control width and alignment:
        //         %-15s = left-aligned string, 15 chars wide
        //         %10.2f = right-aligned decimal, 10 chars wide,
        //                  2 decimal places
        //         %n = newline (platform-independent)
        //
        // HOW:  When the user picks "1. List Transactions":
        //         Print a header row with column names
        //         (ID, Description, Type, Amount, Date)
        //         Print a separator line using "-".repeat(65)
        //         Loop through the ArrayList and print each transaction using printf
        //
        // WHY:  Formatted output makes data readable. Without alignment,
        //       columns don't line up and the output looks messy.
        //
        // OBSERVE: The output should look like a clean table with aligned columns.
        //          All amounts should have exactly 2 decimal places.

        // -------------------------------------------------------
        // TODO TICKET-F019: Step 4 — Input validation
        // STATUS: Completed in addTransaction() and its helper methods.
        // -------------------------------------------------------
        //
        // WHAT: Validation means checking user input BEFORE using it.
        //       Invalid input should produce a clear error message,
        //       not crash the program.
        //
        // HOW:  When the user picks "2. Add Transaction":
        //         Read amount and date from the Scanner.
        //
        //         Check Rule 1: Amount must be > 0.
        //           Parse the input to BigDecimal, then use
        //           compareTo(BigDecimal.ZERO) to check.
        //           If invalid, print an error message and ask again.
        //
        //         Check Rule 2: Date must not be in the future.
        //           Parse the input to LocalDate, then use
        //           isAfter(LocalDate.now()) to check.
        //           If invalid, print an error message and ask again.
        //
        // WHY:  Real applications never trust user input.
        //       A user might type "-500" as amount or "2030-01-01" as date.
        //       Your code must handle this gracefully.
        //
        //       Later (Day 3), you'll move this validation INTO the
        //       BaseTransaction constructor.
        //
        // OBSERVE: Try adding a transaction with amount = -10.
        //          It should print an error.
        //
        //          Try a future date.
        //          It should also print an error.
        //
        //          Try valid data.
        //          It should add successfully and appear in the list.

        Scanner sc = new Scanner(System.in);
        boolean running = true;

        while (running) {
            printMenu();

            int choice;

            try {
                choice = sc.nextInt();
                sc.nextLine(); // discard trailing newline
            } catch (InputMismatchException e) {
                System.out.println("Please enter a number from 1 to 4.");
                sc.nextLine(); // discard bad token
                continue;
            }

            switch (choice) {
                case 1 -> listTransactions(); // wired up in F018
                case 2 -> addTransaction(sc); // wired up in F019
                case 3 -> showSummary();      // wired up in F020
                case 4 -> running = false;
                default -> System.out.println(
                        "Unknown option: " + choice + ". Please choose 1-4."
                );
            }
        }

        sc.close();
        System.out.println("Goodbye!");
    }

    /**
     * Prints the main console menu.
     */
    private static void printMenu() {
        System.out.println();
        System.out.println("=== SmartBudget Console ===");
        System.out.println("1) List Transactions");
        System.out.println("2) Add Transaction");
        System.out.println("3) Summary");
        System.out.println("4) Exit");
        System.out.print("Choice: ");
    }

    // -------------------------------------------------------
    // TICKET-F017: Sample Data
    // -------------------------------------------------------

    /**
     * Adds sample transactions when the application starts.
     */
    private static void seed() {
        TXNS.add(t(1, 1, 1, "3500.00", "2026-01-01", "January salary",   "INCOME"));
        TXNS.add(t(2, 1, 3,   "45.20", "2026-01-08", "Groceries",        "EXPENSE"));
        TXNS.add(t(3, 1, 4,   "25.00", "2026-01-15", "Bus pass",         "EXPENSE"));
        TXNS.add(t(4, 2, 1, "4200.00", "2026-01-01", "January salary",   "INCOME"));
        TXNS.add(t(5, 2, 5,  "120.00", "2026-01-20", "Electricity bill", "EXPENSE"));
        TXNS.add(t(6, 3, 2,  "800.00", "2026-02-05", "Freelance gig",    "INCOME"));
        TXNS.add(t(7, 3, 3,   "60.00", "2026-02-10", "Restaurant",       "EXPENSE"));
        TXNS.add(t(8, 1, 1, "3500.00", "2026-02-01", "February salary",  "INCOME"));
        TXNS.add(t(9, 4, 1, "2800.00", "2026-02-01", "February salary",  "INCOME"));
        TXNS.add(t(10, 5, 3, "52.00",  "2026-03-05", "Groceries",        "EXPENSE"));
        System.out.println("Seeded " + TXNS.size() + " transactions");
    }

    // Tiny factory to keep seed() readable.
    private static Transaction t(
            int id,
            int uid,
            int cid,
            String amt,
            String date,
            String desc,
            String type
    ) {
        return new Transaction(
                id,
                uid,
                cid,
                new BigDecimal(amt),
                LocalDate.parse(date),
                desc,
                type
        );
    }

    // -------------------------------------------------------
    // TICKET-F018: Formatted Output
    // -------------------------------------------------------

    /**
     * Displays all transactions in an aligned table.
     */
    private static void listTransactions() {
        if (TXNS.isEmpty()) {
            System.out.println("(no transactions)");
            return;
        }

        String headerFormat = "%-5s %-25s %-10s %12s %-12s%n";
        String rowFormat = "%-5d %-25.25s %-10s %12.2f %-12s%n";

        System.out.println();
        System.out.println("=== Transactions ===");

        System.out.printf(
                headerFormat,
                "ID",
                "Description",
                "Type",
                "Amount",
                "Date"
        );

        System.out.println("-".repeat(70));

        for (Transaction transaction : TXNS) {
            System.out.printf(
                    rowFormat,
                    transaction.getTxnId(),
                    transaction.getDescription(),
                    transaction.getType(),
                    transaction.getAmount(),
                    transaction.getTxnDate()
            );
        }

        System.out.println("-".repeat(70));
        System.out.println("Total rows: " + TXNS.size());
    }

    // -------------------------------------------------------
    // TICKET-F019: Add Transaction and Input Validation
    // -------------------------------------------------------

    /**
     * Reads transaction information from the user, validates it,
     * creates a Transaction object and adds it to the list.
     */
    private static void addTransaction(Scanner sc) {
        System.out.println();
        System.out.println("=== Add Transaction ===");

        /*
         * The transaction ID is generated automatically.
         *
         * This avoids asking the user to manually choose an ID and prevents
         * accidental duplicate IDs inside the in-memory list.
         */
        int transactionId = generateNextTransactionId();

        int userId = readPositiveInteger(sc, "User ID: ");
        int categoryId = readPositiveInteger(sc, "Category ID: ");
        String description = readRequiredText(sc, "Description: ");
        String type = readTransactionType(sc);
        BigDecimal amount = readPositiveAmount(sc);
        LocalDate date = readValidDate(sc);

        Transaction transaction = new Transaction(
                transactionId,
                userId,
                categoryId,
                amount,
                date,
                description,
                type
        );

        TXNS.add(transaction);

        System.out.println();
        System.out.println("Transaction added successfully.");
        System.out.println("Transaction ID: " + transactionId);
    }

    /**
     * Reads a positive whole number.
     *
     * A valid value must:
     * 1. Be an integer
     * 2. Be greater than zero
     */
    private static int readPositiveInteger(
            Scanner sc,
            String prompt
    ) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();

            try {
                int value = Integer.parseInt(input);

                if (value <= 0) {
                    System.out.println(
                            "Value must be greater than zero."
                    );
                    continue;
                }

                return value;
            } catch (NumberFormatException e) {
                System.out.println(
                        "Please enter a valid whole number."
                );
            }
        }
    }

    /**
     * Reads text that cannot be empty.
     */
    private static String readRequiredText(
            Scanner sc,
            String prompt
    ) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println(
                        "This field cannot be empty."
                );
                continue;
            }

            return input;
        }
    }

    /**
     * Reads and validates the transaction type.
     *
     * Only INCOME and EXPENSE are accepted.
     */
    private static String readTransactionType(Scanner sc) {
        while (true) {
            System.out.print("Type (INCOME/EXPENSE): ");
            String type = sc.nextLine().trim().toUpperCase();

            if ("INCOME".equals(type) || "EXPENSE".equals(type)) {
                return type;
            }

            System.out.println(
                    "Type must be either INCOME or EXPENSE."
            );
        }
    }

    /**
     * Reads and validates a positive monetary amount.
     *
     * BigDecimal is used instead of double because BigDecimal is safer
     * and more precise for financial calculations.
     */
    private static BigDecimal readPositiveAmount(Scanner sc) {
        while (true) {
            System.out.print("Amount: ");
            String input = sc.nextLine().trim();

            try {
                BigDecimal amount = new BigDecimal(input);

                // compareTo() returns:
                //   -1 when amount is smaller than zero
                //    0 when amount equals zero
                //    1 when amount is greater than zero
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println(
                            "Amount must be greater than zero."
                    );
                    continue;
                }

                /*
                 * Restrict the value to two decimal places.
                 *
                 * Examples:
                 * 10       -> valid
                 * 10.5     -> valid
                 * 10.50    -> valid
                 * 10.999   -> invalid
                 */
                if (amount.scale() > 2) {
                    System.out.println(
                            "Amount cannot have more than 2 decimal places."
                    );
                    continue;
                }

                return amount;
            } catch (NumberFormatException e) {
                System.out.println(
                        "Please enter a valid amount, for example 25.50."
                );
            }
        }
    }

    /**
     * Reads and validates the transaction date.
     *
     * The required format is yyyy-MM-dd.
     * The date cannot be after today's date.
     */
    private static LocalDate readValidDate(Scanner sc) {
        while (true) {
            System.out.print("Date (yyyy-MM-dd): ");
            String input = sc.nextLine().trim();

            try {
                LocalDate date = LocalDate.parse(input);

                if (date.isAfter(LocalDate.now())) {
                    System.out.println(
                            "Transaction date cannot be in the future."
                    );
                    continue;
                }

                return date;
            } catch (DateTimeParseException e) {
                System.out.println(
                        "Invalid date. Use the format yyyy-MM-dd."
                );
            }
        }
    }

    /**
     * Generates the next transaction ID.
     *
     * It finds the largest current ID and adds one.
     */
    private static int generateNextTransactionId() {
        int highestId = 0;

        for (Transaction transaction : TXNS) {
            if (transaction.getTxnId() > highestId) {
                highestId = transaction.getTxnId();
            }
        }

        return highestId + 1;
    }

    // -------------------------------------------------------
    // TICKET-F020: Summary
    // -------------------------------------------------------

    /**
     * Calculates total income, total expenses and net balance.
     */
    private static void showSummary() {
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;

        for (Transaction transaction : TXNS) {
            if ("INCOME".equals(transaction.getType())) {
                income = income.add(transaction.getAmount());
            } else if ("EXPENSE".equals(transaction.getType())) {
                expense = expense.add(transaction.getAmount());
            }
        }

        BigDecimal net = income.subtract(expense);

        System.out.println();
        System.out.println("=== Summary ===");
        System.out.printf("Total Income:    %12.2f%n", income);
        System.out.printf("Total Expenses:  %12.2f%n", expense);
        System.out.println("-".repeat(28));
        System.out.printf("Net Balance:     %12.2f%n", net);
    }
}