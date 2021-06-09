package dev.kerbow.services;

import java.util.Scanner;
import java.util.Map;

import dev.kerbow.models.Customers;

public interface CustomerServices {
	
	boolean login(Scanner scanner);
	boolean signUp(Scanner scanner);
	boolean logout();
	Map<Integer, Customers> getAllCustomers();

}
