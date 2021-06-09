package dev.kerbow.services;

import java.util.Map;

import dev.kerbow.models.Transactions;
import dev.kerbow.repositories.TransactionRepository;

public class TransactionServicesImpl implements TransactionServices{
	private static TransactionServicesImpl instance;
	
	private TransactionServicesImpl() {}
	
	public static TransactionServicesImpl getInstance() {
		if(instance == null) instance = new TransactionServicesImpl();
		return instance;
	}

	@Override
	public void add(Transactions t) {
		TransactionRepository.getInstance().add(t);
		
	}

	@Override
	public Map<Integer, Transactions> getAll() {
		return TransactionRepository.getInstance().getAll();
	}

}
