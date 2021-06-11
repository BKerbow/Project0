package dev.kerbow.services;

import java.text.NumberFormat;
import java.util.List;
import java.util.Scanner;

import dev.kerbow.app.Driver;
import dev.kerbow.models.Accounts;
import dev.kerbow.models.Customers;
import dev.kerbow.models.Transactions;
import dev.kerbow.repositories.AccountRepository;

public class AccountServicesImpl implements AccountServices {
	private static AccountServicesImpl instance;

	private AccountServicesImpl() {}

	public static AccountServicesImpl getInstance() {
		if(instance == null) instance = new AccountServicesImpl();
		return instance;
	}

	public List<Accounts> getPendingAccounts(){
		return AccountRepository.getInstance().getPendingAccounts();
	}

	private static String[] parseWithdrawOrDepositInfo(Scanner scanner, boolean withdraw) {
		String[] info = new String[2];
		Driver.printAccounts(CustomerServicesImpl.getInstance().getCustomer());
		Driver.printMessage("Please enter the account you wish to %s: ", withdraw ? "withdraw from" : "deposit into");
		info[0] = scanner.next();
		scanner.nextLine();
		Driver.printMessage("Amount to " + (withdraw ? "withdrarw: " : "deposit: "), false);
		info[1] = scanner.next();
		scanner.nextLine();
		return info;
	}

	private static String[] parseTransferInfo(Scanner scanner) {
		String[] info = new String[3];
		Driver.printAccounts(CustomerServicesImpl.getInstance().getCustomer());
		Driver.printMessage("Please enter the account you wish to transfer from: ", false);
		info[0] = scanner.next();
		scanner.nextLine();
		Driver.printMessage("Please enter the account you wish to transfer to: ", false);
		info[1] = scanner.next();
		scanner.nextLine();
		Driver.printMessage("Amount to Transfer: ", false);
		info[2] = scanner.next();
		scanner.nextLine();
		return info;
	}

	@Override
	public boolean withdraw(Scanner scanner) {
		String[] command = parseWithdrawOrDepositInfo(scanner, true);
		Integer account_id = Integer.parseInt(command[0]);
		Float amount = Float.parseFloat(command[1]);

		if(!CustomerServicesImpl.getInstance().getCustomer().getAccounts().containsKey(account_id)) {
			Driver.printMessage("You are not authorized to withdraw from account %d.%n", account_id);
			return false;
		}
		
		Accounts account = AccountRepository.getInstance().getById(account_id);
		if(account != null) {
			if(amount < 0) {
				Driver.printMessage("You cannot withdarw a negative amount.");
				return false;
			} else if (amount > account.getBalance()) {
				Driver.printMessage("You cannot withdraw $%.2f from %d because its balance is only $%.2f%n.", 
						amount, account_id, account.getBalance());
				return false;
			} else if(account.isPending()) {
				Driver.printMessage("You cannot withdraw money from a pending account. Please wait until an employee approves the account.");
				return false;
			} else {
				boolean confirmation = Driver.getConfirmation("Are you sure you wish to withdraw $%.2f from account %d?",
						amount, account_id);
				if(confirmation) {
					account.setBalance(account.getBalance() - amount);
					update(account, true);
					TransactionServicesImpl.getInstance().add(new Transactions(account, "withdrawal", amount));
					Driver.printMessage("You have withdrawn $%.2f from acount %d.%n%n", amount, account_id);
					Driver.logger.info(String.format("Customer %s withdrew $%.2f from account %d.", CustomerServicesImpl.getInstance().getCustomer().getUsername(), amount, account_id));
					return true;
				}
			}
		} else {
			Driver.printMessage("You do not have na account with the id of " + account_id);
		}
		return false;
	}

	@Override
	public boolean deposit(Scanner scanner) {
		String[] info = parseWithdrawOrDepositInfo(scanner, false);
		Integer account_id = Integer.parseInt(info[0]);
		Float amount = Float.parseFloat(info[1]);

		if(!CustomerServicesImpl.getInstance().getCustomer().getAccounts().containsKey(account_id)){
			Driver.printMessage("You are not authorized to deposit money into account %d.%n", account_id);
			return false;
		}
		
		Accounts account = AccountRepository.getInstance().getById(account_id);
		if(account != null) {
			if(amount < 0) {
				Driver.printMessage("You cannot deposit a negative ammount.");
				return false;
			}else if(account.isPending()) {
				Driver.printMessage("You cannot deposit money into a pending account. Please wait until an employee approves the account.");
				return false;
			} else {
				account.setBalance(account.getBalance() + amount);
				update(account, true);
				TransactionServicesImpl.getInstance().add(new Transactions(account, "deposit", amount));
				Driver.printMessage("You have deposited $%.2f into account %d.%n%n.", amount, account_id);
				Driver.logger.info(String.format("Customer %s depositted $%.2f into account %d.", CustomerServicesImpl.getInstance().getCustomer().getUsername(), amount, account_id));
				return true;
			}
		} else {
			Driver.printMessage("You do not have an account with the id of " + account_id);
		}
		return false;
	}

	@Override
	public boolean transfer(Scanner scanner) {
		String[] info = parseTransferInfo(scanner);
		Integer from_id = Integer.parseInt(info[0]);
		Integer to_id = Integer.parseInt(info[1]);
		Float amount = Float.parseFloat(info[2]);
		
		if(!CustomerServicesImpl.getInstance().getCustomer().getAccounts().containsKey(from_id)) {
			Driver.printMessage("You are not authorized to transfer money out of account %d.%n", from_id);
			return false;
		} else if (!CustomerServicesImpl.getInstance().getCustomer().getAccounts().containsKey(to_id)) {
			Driver.printMessage("You are not authorized to transfer money into acccount %d.%n", to_id);
			return false;
		}

		Accounts from = CustomerServicesImpl.getInstance().getCustomer().getAccounts().get(from_id);
		Accounts to = CustomerServicesImpl.getInstance().getCustomer().getAccounts().get(to_id);
		if (from == null) {
			Driver.printMessage("You do not have an account with the id of " + from_id);
			return false;
		}else if ( to == null) {
			Driver.printMessage("You do not have an account with the id of " + to_id);
			return false;
		}else {
			if (amount < 0) {
				Driver.printMessage("You cannot transfer a negative amount.");
				return false;
			} else if (amount > from.getBalance()) {
				Driver.printMessage("You cannot transfer $%.2f from %d because its balance is only $%.2f", amount, from.getId(), from.getBalance());
				return false;
			} else if (from.isPending() || to.isPending()) {
				Driver.printMessage("One of the acounts you are attempting the transfer with has not been approved. Please wait for an employee to approve the account.");
				return false;
			} else {
				boolean confirmation = Driver.getConfirmation("Are you sure you want to transfer $%.2f from account %d to account %d? Y/N", amount, from.getId(), to.getId());
				
				if(confirmation) {
					from.setBalance(from.getBalance() - amount);
					to.setBalance(to.getBalance() + amount);
					update(from, true);
					update(to, true);
					TransactionServicesImpl.getInstance().add(new Transactions(from, "transfer", amount, to));
					Driver.printMessage("Transfer confirmed.");
					Driver.logger.info(String.format("Customer %s transferred $%.2f from account %d to account %d.", CustomerServicesImpl.getInstance().getCustomer().getUsername(), amount, from_id, to_id));
					return true;
				} else {
					Driver.printMessage("Transfer cancelled.");
					return false;
				}
			}
		}
	}

	@Override
	public void apply(Scanner scanner) {
		Driver.printMessage("Please enter a starting balance: ");
		Float amount = scanner.nextFloat();
		scanner.nextLine();
		
		if (amount < 0) {
			Driver.printMessage("You cannot create an account with a negative balance");
			return;
		}
		
		Accounts a = new Accounts(amount);
		a.setCustomer_id(CustomerServicesImpl.getInstance().getCustomer().getId());
		a.setPending(!CustomerServicesImpl.getInstance().getCustomer().isEmployee());
		AccountRepository.getInstance().add(a);
		CustomerServicesImpl.getInstance().getCustomer().addAccount(a);
		if (CustomerServicesImpl.getInstance().getCustomer().isEmployee()) {
			Driver.printMessage("Your new account with balance %s has been created.", NumberFormat.getCurrencyInstance().format(amount));
		} else {
			Driver.printMessage("Your new account with balance %s has been created and is pending approval. It must be approved before it can be used.%n", NumberFormat.getCurrencyInstance().format(amount));
		}
	}
	
	@Override
	public void update(Accounts account, boolean updateCurrentCustomer) {
		if (updateCurrentCustomer) CustomerServicesImpl.getInstance().getCustomer().addAccount(account);
		AccountRepository.getInstance().update(account);
	}	
}
