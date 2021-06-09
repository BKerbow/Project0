package dev.kerbow.app;

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

	private enum Menus{
		MAIN,
		CUSTOMER_MAIN
	}

	private static Menus currentMenu = Menus.MAIN;

	public static final Logger logger = LogManager.getLogger(Driver.class);

	private static Scanner scanner;
	private static String[] main_menu = { "1. Login", "2. Sign Up" };
	private static String[] customer_menu = { 
			"1. Apply for a new account",
			"2. Make a Withdrawal",
			"3. Make a Deposit",
			"4. Transfer Betweeen Accounts",
			"5. Accept a Transfer" 
	};
	private static String[] employee_options = { 
			"1. Approve or Reject an Account",
			"2. View a Customer's Accounts",
			"3. View Transaction Log"
	};


	public static void printAccounts(Customers customer) {
		if (customer == null) {
			logger.error("printAccounts: customer null");
			return;
		}

		boolean printingCurrentCustomer = customer.equals(CustomerServicesImpl.getInstance().getCustomer());

		if(printingCurrentCustomer) {
			printMessage("\n\nYou are logged in as a user \"" + customer.getUsername()+ "\"\n");
		} else printMessage("\nAccounts for customer \"" + customer.getUsername() + "\":\n");

		if(customer.getAccounts() == null || customer.getAccounts().size() == 0) {
			if (printingCurrentCustomer) {
				printMessage("You do not have any accounts currently open.");
			} else printMessage("Customer \"%s\" does not have any accounts.%n", customer.getUsername());
		} else {
			if (printingCurrentCustomer) printMessage("Your accounts:");
			customer.getAccounts().entrySet().forEach((e) -> {
				printMessage("    %d: $%.2f    %s%n", e.getKey(), e.getValue().getBalance(), e.getValue().isPending() ? "Pending" : "");
			});
			printMessage("");
		}
	}

	private static void printMenu(String[] menu, String extraOption, boolean printCarrot) {
		printMessage("Please select an option:\n");
		for (String option : menu) System.out.println(option);
		if (extraOption != null && !extraOption.isEmpty()) System.out.println("" + (menu.length + 1) + ". " + extraOption);
		if (printCarrot) printCarrot();
	}

	private static void printMenu(String[] menu, String extraOption, boolean printCarrot, boolean printEmployeeOptions) {
		printMenu(menu, null, false);
		if(printEmployeeOptions) {
			for(int i = 0; i < employee_options.length; i++) {
				printMessage("%d. %s%n", i + menu.length + 1, employee_options[i]);
			}
		}

		if(extraOption != null && !extraOption.isEmpty()) {
			int optionNum = menu.length + 1;
			if(printEmployeeOptions) optionNum += employee_options.length;
			printMessage("%d. %s%n", optionNum, extraOption);
		}
		if(printCarrot) printCarrot();
	}

	private static void printCarrot() {
		System.out.print("> ");
	}

	public static void printMessage(String message) {
		System.out.println(message);
	}

	public static void printMessage(String message, boolean addNewLine) {
		if(!addNewLine) System.out.print(message);
		else System.out.println(message);
	}

	public static void printMessage(String message, Object...args) {
		System.out.printf(message, args);
	}

	public static boolean getConfirmation(String message, Object...args) {
		System.out.printf(message + "Y/N: ", args);
		boolean confirmation = scanner.nextLine().equalsIgnoreCase("y");
		return confirmation;
	}

	private static boolean handleMainMenu() {
		String username;
		switch (scanner.nextInt()) {
			case 1:		//login
				if (CustomerServicesImpl.getInstance().login(scanner)) {
					currentMenu = Menus.CUSTOMER_MAIN;
					username = CustomerServicesImpl.getInstance().getCustomer().getUsername();
					logger.info("Customer " + username + " logged in.");
					return false;
				} else {
					printMenu(main_menu, "Quit", true);
					return false;
				} 
			case 2:		//sign up
				if(CustomerServicesImpl.getInstance().signUp(scanner)) {
					currentMenu = Menus.CUSTOMER_MAIN;
					username = CustomerServicesImpl.getInstance().getCustomer().getUsername();
					logger.info("Customer " + username + " has signed up.");
					return false;
				} else {
					printMenu(main_menu, "Quit", true);
					return false;
				}
			case 3: printMessage("Goodbye!"); return true;
			default: printMessage("Please enter a valid option."); return false;
		}
	}

	private static void handleCustomerMenu() {
		String[] command;
		Customers customer = CustomerServicesImpl.getInstance().getCustomer();
		if(customer == null) {
			logger.error("handleCustomerMenu: custmer null");
		}

		command = scanner.nextLine().split(" ");
		switch(command[0]) {
			case "1":
					//Apply for a new Account
					AccountServicesImpl.getInstance().apply(scanner);
					break;
			case "2":
					//Withdraw Money
					AccountServicesImpl.getInstance().withdraw(scanner);	
					break;
			case "3":
					//deposit money
					AccountServicesImpl.getInstance().deposit(scanner);
					break;
			case "4":
					//Transfer Money between Accounts
					AccountServicesImpl.getInstance().transfer(scanner);
					break;
			case "5":
			case "6":
			case "7":
			case "8":
					//Logout (if customer only or if case "8") / Approve or reject an account
					if(command[0].equals("8") || (command[0].equals("5") && !customer.isEmployee())){
						String username = CustomerServicesImpl.getInstance().getCustomer().getUsername();
						logger.info("Customer " + username + " logged out.");
						CustomerServicesImpl.getInstance().logout();
						currentMenu = Menus.MAIN;
						return;
					} else handleEmployeeOptions(command);
					break;
			default: printMessage("Please enter a valid option.\n"); return;
		}
	}

	private static void handleEmployeeOptions(String[] command) {

		switch (command[0]) {
			case "5":
				// Approve or reject an account
				List<Accounts> pending = AccountServicesImpl.getInstance().getPendingAccounts();
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
				Customers c = CustomerServicesImpl.getInstance().getCustomer(customer_id);
				printAccounts(c);
				break;
			case "7":
				// View transaction log
				Map<Integer, Transactions> transactions = TransactionServicesImpl.getInstance().getAll();
				printMessage("\n\n\nTransaction Log:");
				transactions.values().stream().forEach((t) -> printMessage("	id: %d, source: %s, type: %s, amount: $%.2f, receiver: %s%n", t.getId(), t.getSource(), t.getType(), t.getAmount(), t.getReceiver()));
				printMessage("\n\n");
				break;
			default: break;
		}
	}

	public static void main(String[] args) {
		scanner = new Scanner(System.in);
		boolean quit = false;

		System.out.println("Welcome to Lodsofemone Automated Banking System!\n");
		do {
			switch (currentMenu) {
				case MAIN:
					printMenu(main_menu, "Quit", true);
					quit = handleMainMenu();
					break;
				case CUSTOMER_MAIN:
					printAccounts(CustomerServicesImpl.getInstance().getCustomer());
					printMenu(customer_menu, "logout", true, CustomerServicesImpl.getInstance().getCustomer().isEmployee());
					handleCustomerMenu();
					break;
			}
		} while (!quit);
		scanner.close();
	}
}
