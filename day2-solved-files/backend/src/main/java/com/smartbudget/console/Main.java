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
//       Run this class directly in your IDE: right-click -> Run 'Main.main()'
//
// WHY:  Before building REST APIs (Day 5-6), you practice Java basics:
//       ArrayList, Scanner input, loops, String.format, if-else validation.
//       This is your first runnable code in the SmartBudget project.
//
// ============================================================
public class Main {

    // -------------------------------------------------------
    // TICKET-F017: Sample data
    // -------------------------------------------------------
    // WHAT: An ArrayList is a resizable list that can grow/shrink dynamically.
    //       Unlike arrays (fixed size), ArrayList lets you add/remove items anytime.
    //
    // HOW:  Declared as a static field so every helper method below can see it.
    //       Populated by seed() which is called once at the top of main().
    //
    // WHY:  This gives you data to display and filter before the database is
    //       connected. Day 4 will swap seed() for a JDBC DAO call.
    //
    // OBSERVE: Startup prints "Seeded 10 transactions" and option 1 shows 10 rows.
    private static final List<Transaction> TXNS = new ArrayList<>();

    public static void main(String[] args) {

        // Populate the in-memory list before the menu appears (F017).
        seed();

        // -------------------------------------------------------
        // TICKET-F016: Menu loop
        // -------------------------------------------------------
        // WHAT: A while loop with a Scanner creates a text-based menu.
        //       Scanner reads keyboard input from the user.
        //       The loop runs until the user chooses "Exit".
        //
        // HOW:  Scanner over System.in + boolean flag + switch on the choice.
        //       After nextInt() we call nextLine() to consume the leftover newline —
        //       classic Java gotcha; without it the next nextLine() returns "".
        //       Wrap nextInt() in try/catch for InputMismatchException so typing
        //       a letter doesn't crash the app.
        //
        // WHY:  This teaches control flow (while, switch) and user input handling.
        //
        // OBSERVE: Menu prints, options 1/2/3 do distinct things, option 4 exits.
        Scanner sc = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("=== SmartBudget Console ===");
            System.out.println("1) List Transactions");
            System.out.println("2) Add Transaction");
            System.out.println("3) Summary");
            System.out.println("4) Exit");
            System.out.print("Choice: ");

            int choice;
            try {
                choice = sc.nextInt();
                sc.nextLine();                   // discard trailing newline
            } catch (InputMismatchException e) {
                System.out.println("Please enter a number 1-4.");
                sc.nextLine();                   // discard the bad token
                continue;
            }

            switch (choice) {
                case 1 -> listTransactions();    // F018
                case 2 -> addTransaction(sc);    // F019
                case 3 -> showSummary();         // F020
                case 4 -> running = false;
                default -> System.out.println("Unknown option: " + choice);
            }
        }

        sc.close();
        System.out.println("Goodbye!");
    }

    // -------------------------------------------------------
    // TICKET-F017 — seed the in-memory transaction list
    // -------------------------------------------------------
    private static void seed() {
        TXNS.add(t(1,  1, 1, "3500.00", "2026-01-01", "January salary",   "INCOME"));
        TXNS.add(t(2,  1, 3,   "45.20", "2026-01-08", "Groceries",        "EXPENSE"));
        TXNS.add(t(3,  1, 4,   "25.00", "2026-01-15", "Bus pass",         "EXPENSE"));
        TXNS.add(t(4,  2, 1, "4200.00", "2026-01-01", "January salary",   "INCOME"));
        TXNS.add(t(5,  2, 5,  "120.00", "2026-01-20", "Electricity bill", "EXPENSE"));
        TXNS.add(t(6,  3, 2,  "800.00", "2026-02-05", "Freelance gig",    "INCOME"));
        TXNS.add(t(7,  3, 3,   "60.00", "2026-02-10", "Restaurant",       "EXPENSE"));
        TXNS.add(t(8,  1, 1, "3500.00", "2026-02-01", "February salary",  "INCOME"));
        TXNS.add(t(9,  4, 1, "2800.00", "2026-02-01", "February salary",  "INCOME"));
        TXNS.add(t(10, 5, 3,   "52.00", "2026-03-05", "Groceries",        "EXPENSE"));
        System.out.println("Seeded " + TXNS.size() + " transactions");
    }

    /** Tiny factory to keep seed() readable — parses the strings once. */
    private static Transaction t(int id, int uid, int cid, String amt,
                                 String date, String desc, String type) {
        return new Transaction(id, uid, cid,
                new BigDecimal(amt),
                LocalDate.parse(date),
                desc, type);
    }

    // -------------------------------------------------------
    // TICKET-F018 — Formatted output with printf
    // -------------------------------------------------------
    // WHAT: System.out.printf() lets you format output in aligned columns.
    //       Format specifiers control width and alignment:
    //         %-5d   = integer, left-aligned, 5 wide
    //         %-25s  = string, left-aligned, 25 wide
    //         %10.2f = float, right-aligned, 10 wide, 2 decimal places
    //         %n     = platform-correct newline
    //
    // WHY:  Aligned, two-decimal output makes the console app feel like a real
    //       tool. Day 9's React table mirrors these same columns.
    //
    // OBSERVE: Amounts show exactly two decimal places (45.20, not 45.2) and
    //          the columns line up vertically.
    private static void listTransactions() {
        if (TXNS.isEmpty()) {
            System.out.println("(no transactions)");
            return;
        }

        String headerFmt = "%-5s %-25s %-8s %10s %-12s%n";
        String rowFmt    = "%-5d %-25s %-8s %10.2f %-12s%n";

        System.out.printf(headerFmt, "ID", "Description", "Type", "Amount", "Date");
        System.out.println("-".repeat(65));

        for (Transaction tx : TXNS) {
            System.out.printf(rowFmt,
                    tx.getTxnId(),
                    tx.getDescription(),
                    tx.getType(),
                    tx.getAmount(),
                    tx.getTxnDate());
        }

        System.out.println("-".repeat(65));
        System.out.println("Total rows: " + TXNS.size());
    }

    // -------------------------------------------------------
    // TICKET-F019 — Input validation
    // -------------------------------------------------------
    // WHAT: Validation means checking user input BEFORE using it.
    //       Invalid input should produce a clear error message, not crash.
    //
    // HOW:  Read each field with sc.nextLine(), parse it, validate, then
    //       build a Transaction. Wrap the parsing calls in try/catch —
    //       new BigDecimal("abc") throws NumberFormatException and
    //       LocalDate.parse("nope") throws DateTimeParseException.
    //       On any validation failure: print a clear message and return
    //       (don't add the row).
    //
    // WHY:  Real applications never trust user input. This is the application-
    //       layer companion to the POJO-level validation from F014.
    //       Day 6's REST controller layers @Valid + @ControllerAdvice on top
    //       of the same idea — same rules, different transport.
    //
    // OBSERVE:
    //   amount "-50"       -> "Amount must be positive. Transaction not added."
    //   date   "2030-01-01" -> "Date cannot be in the future. Transaction not added."
    //   amount "abc"       -> "Invalid number. Transaction not added."
    //   valid input        -> "Added transaction #11" and option 1 shows 11 rows.
    private static void addTransaction(Scanner sc) {
        System.out.print("Amount: ");
        BigDecimal amount;
        try {
            amount = new BigDecimal(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number. Transaction not added.");
            return;
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Amount must be positive. Transaction not added.");
            return;
        }

        System.out.print("Date (yyyy-mm-dd): ");
        LocalDate date;
        try {
            date = LocalDate.parse(sc.nextLine().trim());
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Transaction not added.");
            return;
        }
        if (date.isAfter(LocalDate.now())) {
            System.out.println("Date cannot be in the future. Transaction not added.");
            return;
        }

        System.out.print("Description: ");
        String desc = sc.nextLine().trim();
        if (desc.isEmpty()) {
            System.out.println("Description required. Transaction not added.");
            return;
        }

        System.out.print("Type (INCOME/EXPENSE): ");
        String type = sc.nextLine().trim().toUpperCase();
        if (!"INCOME".equals(type) && !"EXPENSE".equals(type)) {
            System.out.println("Type must be INCOME or EXPENSE. Transaction not added.");
            return;
        }

        int nextId = TXNS.size() + 1;
        TXNS.add(new Transaction(nextId, 1, 1, amount, date, desc, type));
        System.out.println("Added transaction #" + nextId);
    }

    // -------------------------------------------------------
    // TICKET-F020 — Summary view
    // -------------------------------------------------------
    // WHAT: Walk TXNS, accumulate two BigDecimal totals (one per type),
    //       compute net = income - expense, and print all three with %12.2f.
    //
    // WHY:  This is the Java mirror of Day 1's CTE-based net-balance query.
    //       Day 6 will expose the same calculation as /api/summary and Day 9
    //       will render it in a card — same math, three layers.
    //
    // OBSERVE: With the F017 seed:
    //   Total Income:    14800.00
    //   Total Expenses:    302.20
    //   Net Balance:     14497.80
    private static void showSummary() {
        BigDecimal income  = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;

        for (Transaction tx : TXNS) {
            if ("INCOME".equals(tx.getType())) {
                income = income.add(tx.getAmount());
            } else if ("EXPENSE".equals(tx.getType())) {
                expense = expense.add(tx.getAmount());
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
