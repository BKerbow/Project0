package dev.kerbow.services;

import java.util.Map;
import java.util.Scanner;

import dev.kerbow.app.Driver;
import dev.kerbow.models.Accounts;
import dev.kerbow.models.Customers;
import dev.kerbow.repositories.CustomerRepository;

public class CustomerServicesImpl implements CustomerServices {
	
	//create instance CustomerServiceImpl
	private static CustomerServicesImpl instance;
	//create Customers variable
	private Customers customer;
	
	//No Args Constructor
	private CustomerServicesImpl() {}

	//create instance of CSI if none exists
	public static CustomerServicesImpl getInstance() {
		if (instance == null) instance = new CustomerServicesImpl();
		return instance;
	}
	
	
	public Customers getCustomer() {
		return this.customer;
	}
	
	public Customers getCustomer(Integer id) {
		return CustomerRepository.getInstance().getById(id);
	}
	
	public Customers updateCustomer() {
		this.customer = CustomerRepository.getInstance().getById(this.customer.getId());
		return this.customer;
	}
	
	public void updateAccount(Accounts account) {
		this.customer.addAccount(account);
	}
	
	private static String[] parseInfo(Scanner scanner, boolean signingUp) {
		String[] info = new String[2];
		Driver.printMessage("Please enter %s login information:%n", signingUp ? "new" : "your");
		Driver.printMessage("Username: ", false);
		info[0] = scanner.next();
		scanner.nextLine();
		Driver.printMessage("Password: ", false);
		info[1] = scanner.next();
		scanner.nextLine();
		
		if(signingUp) {
			Driver.printMessage("Confirm Password: ");
			String confirmation = scanner.nextLine();
			if(!confirmation.equals(info[1])) {
				Driver.printMessage("Password does not match confirmation, please try again.\n\n");
				return parseInfo(scanner, signingUp);
			}
		}
		return info;
	}
	
	@Override
	public boolean login(Scanner scanner) {
		String[] info = parseInfo(scanner, false);
		customer = CustomerRepository.getInstance().getByUsernameAndPassword(info[0], info[1]);
		if(customer == null) {
			Driver.printMessage("No customer account was found with the provided login information.\n");
			return false;
		} else return true;
	}
	
	@Override
	public boolean signUp(Scanner scanner) {
		String[] info = parseInfo(scanner, true);
		customer = CustomerRepository.getInstance().getByUsernameAndPassword(info[0], info[1]);
		if (customer != null) {
			Driver.printMessage("An account with the provided login information already exists. Please try again.\n");
			return false;
		} else {
			customer = new Customers(info[0], info[1]);
			CustomerRepository.getInstance().add(customer);
			Driver.printMessage("Logged in with account: " + customer);
			return true;
		}
	}
	
	@Override
	public boolean logout() {
		this.customer = null;
		Driver.printMessage("\nYou have been logged out.\n");
		return true;
	}

	@Override
	public Map<Integer, Customers> getAllCustomers(){
		return CustomerRepository.getInstance().getAll();
	}
}
