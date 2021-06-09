package dev.kerbow.services;

import java.util.Scanner;

import dev.kerbow.models.Accounts;

public interface AccountServices {
	boolean withdraw(Scanner scanner);
	boolean deposit(Scanner scanner);
	boolean transfer(Scanner scanner);
	void apply(Scanner scanner);
	void update(Accounts account, boolean updateCurrentCustomer);

}
