package dev.kerbow.models;

import java.text.NumberFormat;

import dev.kerbow.services.CustomerServicesImpl;

public class Transactions {
	private Integer id;
	private Accounts source;
	private String type;
	private Float amount;
	private Accounts receiver;
	
	public Transactions() {}

	public Transactions(Accounts source, String type, Float amount) {
		this.source = source;
		this.type = type;
		this.amount = amount;
	}

	public Transactions(Accounts source, String type, Float amount, Accounts receiver) {
		super();
		this.source = source;
		this.type = type;
		this.amount = amount;
		this.receiver = receiver;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Accounts getSource() {
		return source;
	}

	public void setSource(Accounts source) {
		this.source = source;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Float getAmount() {
		return amount;
	}

	public void setAmount(Float amount) {
		this.amount = amount;
	}

	public Accounts getReceiver() {
		return receiver;
	}

	public void setReceiver(Accounts receiver) {
		this.receiver = receiver;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((amount == null) ? 0 : amount.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((receiver == null) ? 0 : receiver.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Transactions other = (Transactions) obj;
		if (amount == null) {
			if (other.amount != null)
				return false;
		} else if (!amount.equals(other.amount))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (receiver == null) {
			if (other.receiver != null)
				return false;
		} else if (!receiver.equals(other.receiver))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Transactions [id=" + id + ", source=" + source + ", type=" + type + ", amount=" + amount + ", receiver="
				+ receiver + "]";
	}
	
	public String toPrettyString() {
		int source_id = this.source.getId();
		String source_customer_name = CustomerServicesImpl.getInstance().getCustomer(this.source.getCustomer_id()).getUsername();
		String source_balance = NumberFormat.getCurrencyInstance().format(this.source.getBalance());
		String amount_format = NumberFormat.getCurrencyInstance().format(this.amount);
		
		String str = String.format("Transaction %3d -> (9%d) %s: %10s | %-10s of %10s", this.id, source_id, source_customer_name, source_balance, this.type, amount_format);
		
		StringBuilder builder = new StringBuilder(str);
		
		if (this.type.equalsIgnoreCase("transfer")) {
			int receiver_id = this.receiver.getId();
			String receiver_customer_name = CustomerServicesImpl.getInstance().getCustomer(this.receiver.getCustomer_id()).getUsername();
			float receiver_balance = this.receiver.getBalance();
			
			str = String.format(" to (%d) %s: $%.2f", receiver_id, receiver_customer_name, receiver_balance);
			builder.append(str);
		}
		return builder.toString();
	}
	
}
