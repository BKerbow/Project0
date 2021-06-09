package dev.kerbow.models;

import java.util.HashMap;
import java.util.Map;

public class Customers {
	private Integer id;
	private String username, password;
	private boolean isEmployee;

	private Map<Integer, Accounts> accounts;

	public Customers() {}

	public Customers(Integer id, String username, String password) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.isEmployee = false;
		this.accounts = new HashMap<Integer, Accounts>();
	}

	public Customers(Integer id, String username, String password, boolean isEmployee) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.isEmployee = isEmployee;
		this.accounts = new HashMap<Integer, Accounts>();
	}

	public Customers(String username, String password, Accounts...accounts) {
		this.username = username;
		this.password = password;
		this.isEmployee = false;
		this.accounts = new HashMap<Integer, Accounts>();
		for (Accounts a : accounts) this.accounts.put(a.getId(), a);
	}

	public Customers(String username, String password, Boolean isEmployee, Accounts...accounts) {
		this.username = username;
		this.password = password;
		this.isEmployee = isEmployee;
		this.accounts = new HashMap<Integer, Accounts>();
		for (Accounts a : accounts) this.accounts.put(a.getId(), a);
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isEmployee() {
		return isEmployee;
	}

	public void setEmployee(boolean isEmployee) {
		this.isEmployee = isEmployee;
	}

	public Map<Integer, Accounts> getAccounts() {
		return accounts;
	}

	public void setAccounts(Map<Integer, Accounts> accounts) {
		this.accounts = accounts;
	}

	public void addAccount(Accounts accounts) {
		if (this.accounts == null) this.accounts = new HashMap<Integer, Accounts>();
		if (accounts != null && accounts.getId() != null && accounts.getId() >=1) {
			this.accounts.put(accounts.getId(), accounts);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accounts == null) ? 0 : accounts.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (isEmployee ? 1231 : 1237);
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		Customers other = (Customers) obj;
		if (accounts == null) {
			if (other.accounts != null) return false;
		} else if (!accounts.equals(other.accounts)) return false;

		if (id == null) {
			if (other.id != null) return false;
		} else if (!id.equals(other.id)) return false;

		if (isEmployee != other.isEmployee) return false;

		if (password == null) {
			if (other.password != null) return false;
		} else if (!password.equals(other.password)) return false;

		if (username == null) {
			if (other.username != null) return false;
		} else if (!username.equals(other.username)) return false;

		return true;
	}

	@Override
	public String toString() {
		return "Customers [id=" + id + ", username=" + username + ", password=" + password + ", isEmployee="
				+ isEmployee + ", accounts=" + accounts + "]";
	}
}