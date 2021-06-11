package dev.kerbow.app;

import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.kerbow.models.Accounts;
import dev.kerbow.models.Customers;
import dev.kerbow.models.Transactions;
import dev.kerbow.services.AccountServicesImpl;
import dev.kerbow.services.CustomerServicesImpl;
import dev.kerbow.services.TransactionServicesImpl;

public class Driver {
	
	private enum Menus {
		MAIN,
		CUSTOMER_MAIN
	}
	
	private static Menus currentMenu = Menus.MAIN;
	
	public static final Logger logger = LogManager.getLogger(Driver.class);
	
	public static Scanner scanner;
	private static String[] main_menu = { "1. Login", "2. Sign Up" };
	private static String[] customer_menu = { "1. Apply for a new account", "2. Withdraw money", "3. Deposit money", "4. Transfer money"};
	private static String[] employee_options = { "Approve or reject accounts", "View a customer's accounts", "View transaction log" };
	
	public static void printAccounts(Customers customer) {
		if (customer == null) {
			logger.error("printAccounts: customer null");
			return;
		}
		
		boolean printingCurrentCustomer = customer.equals(CustomerServicesImpl.getInstance().getCustomer());
		
		if (printingCurrentCustomer) printMessage("\n\n\nYou are logged in as user \"" + customer.getUsername() + "\"\n");
		else printMessage("\nAccounts for customer \"" + customer.getUsername() + "\":\n");
		
		if (customer.getAccounts() == null || customer.getAccounts().size() == 0) {
			if (printingCurrentCustomer) printMessage("You do not have any accounts currently open.\n");
			else printMessage("Customer \"%s\" does not have any accounts.%n", customer.getUsername());
		} else {
			if (printingCurrentCustomer) printMessage("Your accounts:");
			customer.getAccounts().entrySet().forEach((e) -> {
				printMessage("    %d: %15s    %s%n", e.getKey(), NumberFormat.getCurrencyInstance().format(e.getValue().getBalance()), e.getValue().isPending() ? "Pending" : "");
			});
		}
	}
	
	private static void printMenu(String[] menu, String extraOption, boolean printCarrot) {
		printMessage("\nPlease select an option:\n");
		for (String option : menu) printMessage(option);
		if (extraOption != null && !extraOption.isEmpty()) printMessage("" + (menu.length + 1) + ". " + extraOption);
		if (printCarrot) printCarrot();
	}
	
	private static void printMenu(String[] menu, String extraOption, boolean printCarrot, boolean printEmployeeOptions) {
		printMenu(menu, null, false);
		if (printEmployeeOptions) { 
			for (int i = 0; i < employee_options.length; i++) {
				printMessage("%d. %s%n", i + menu.length + 1, employee_options[i]);
			}
		}
		
		if (extraOption != null && !extraOption.isEmpty()) {
			int optionNum = menu.length + 1;
			if (printEmployeeOptions) optionNum += employee_options.length;
			printMessage("%d. %s%n", optionNum, extraOption);
		}
		if (printCarrot) printCarrot();
	}
	
	private static void printCarrot() {
		System.out.print("> ");
	}
	
	public static void printMessage(String message) {
		System.out.println(message);
	}
	
	public static void printMessage(String message, boolean addNewline) {
		if (!addNewline) System.out.print(message);
		else System.out.println(message);
	}
	
	public static void printMessage(String message, Object... args) {
		System.out.printf(message, args);
	}
	
	public static boolean getConfirmation(String message, Object... args) {
		System.out.printf(message + " Y/N: ", args);
		boolean confirmation = scanner.nextLine().equalsIgnoreCase("y");
		return confirmation;
	}
	
	private static boolean handleMainMenu() {
		String username;
		switch (scanner.nextInt()) {
			case 1:		// login
				if (CustomerServicesImpl.getInstance().login(scanner)) {
					currentMenu = Menus.CUSTOMER_MAIN;
					username = CustomerServicesImpl.getInstance().getCustomer().getUsername();
					logger.info("Customer " + username + " logged in.");
					return false;
				} else {
					printMenu(main_menu, "Quit", true);
					return false;
				}			
			case 2:		// sign up
				if (CustomerServicesImpl.getInstance().signUp(scanner)) {
					currentMenu = Menus.CUSTOMER_MAIN;
					username = CustomerServicesImpl.getInstance().getCustomer().getUsername();
					logger.info("Customer " + username + " has signed up.");
					return false;
				} else {
					printMenu(main_menu, "Quit", true);
					return false;
				}				
			case 3: printMessage("\nGoodbye!"); return true;
			default: printMessage("Please enter a valid option."); return false;
		}
	}
	
	private static void handleCustomerMenu() {
		/*
		 * "1. Apply for a new account"
		 * "2. Withdraw money"
		 * "3. Deposit money"
		 * "4. Transfer money"
		 * "5. Logout (customer only) / Approve or reject an account" (Employee Only)
		 * "6. View a customer's accounts" (Employee Only)
		 * "7. View transaction log" (Employee Only)
		 * "8. Logout (Employee only)"
		 */
		String[] command;
		Customers customer = CustomerServicesImpl.getInstance().getCustomer();
		if (customer == null) {
			logger.error("handleCustomerMenu: customer null");
			return;
		}
		
		command = scanner.nextLine().split(" ");
		switch (command[0]) {
			case "1":
				// apply for a new account
				AccountServicesImpl.getInstance().apply(scanner);
				break;
			case "2":
				// withdraw money
				AccountServicesImpl.getInstance().withdraw(scanner);
				break;
			case "3":
				// deposit money
				AccountServicesImpl.getInstance().deposit(scanner);
				break;
			case "4":
				// post money transfer
				AccountServicesImpl.getInstance().transfer(scanner);
				break;
			case "5":
			case "6":
			case "7":
			case "8":
				// logout (if customer-only or if case "8") / Approve or reject an account
				if (command[0].equals("8") || (command[0].equals("5") && !customer.isEmployee())) {
					String username = CustomerServicesImpl.getInstance().getCustomer().getUsername();
					logger.info("Customer " + username + " logged out.");
					CustomerServicesImpl.getInstance().logout();
					currentMenu = Menus.MAIN;
					return;
				} else handleEmployeeOptions(command);
				break;
			default: printMessage("Please enter a valid option."); return;
		}
	}
	
	private static void handleEmployeeOptions(String[] command) {
		/*
		 * "5. Approve or reject an account"
		 * "6. View a customer's accounts"
		 * "7. View transaction log"
		 */
		
		switch (command[0]) {
			case "5":
				// Approve or reject an account
				List<Accounts> pending = AccountServicesImpl.getInstance().getPendingAccounts();
				
				if (pending.isEmpty()) {
					printMessage("\nThere are no pending accounts.");
					break;
				}
				
				printMessage("\nPending accounts:");
				for (Accounts a : pending) {
					Customers c = CustomerServicesImpl.getInstance().getCustomer(a.getCustomer_id());
					printMessage("    Customer: %s, Requested balance: $%.2f%n", c.getUsername(), a.getBalance());
					boolean approved = getConfirmation("    Approved? ");
					if (approved) {
						a.setPending(false);
						AccountServicesImpl.getInstance().update(a, false);
						printMessage("Account %d with balance $%.2f for Customer %s has been approved.%n", a.getId(), a.getBalance(), c.getUsername());
					}
				}
				break;
			case "6":
				// View a customer's accounts
				printMessage("Choose a Customer to view:");
				CustomerServicesImpl.getInstance().getAllCustomers().entrySet().forEach((e) -> {
					printMessage("    %d. %s%n", e.getKey(), e.getValue().getUsername());
				});
				printCarrot();
				int customer_id = scanner.nextInt();
				scanner.nextLine();
				Customers c = CustomerServicesImpl.getInstance().getCustomer(customer_id);
				printAccounts(c);
				break;
			case "7":
				// View transaction log
				Map<Integer, Transactions> transactions = TransactionServicesImpl.getInstance().getAll();
				printMessage("\n\n\nTransaction Log:");
				transactions.values().stream().forEach((t) -> printMessage("    " + t.toPrettyString()));
				break;
			default: break;
		}
	}
	
	public static void main(String[] args) {
		scanner = new Scanner(System.in);
		boolean quit = false;
		
		printMessage("Welcome to Lodsofemone United F.C.'s POILEZ Banking App!\n");
		do {
			switch (currentMenu) {
				case MAIN:
					printMenu(main_menu, "Quit", true);
					quit = handleMainMenu();
					break;
				case CUSTOMER_MAIN:
					printAccounts(CustomerServicesImpl.getInstance().getCustomer());
					printMenu(customer_menu, "Logout", true, CustomerServicesImpl.getInstance().getCustomer().isEmployee());
					handleCustomerMenu();
					break;
			}
		} while (!quit);
		
		scanner.close();
	}
}