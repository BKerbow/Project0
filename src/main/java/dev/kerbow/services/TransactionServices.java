package dev.kerbow.services;

import java.util.Map;

import dev.kerbow.models.Transactions;

public interface TransactionServices {
	void add(Transactions t);
	Map<Integer, Transactions> getAll();

}
