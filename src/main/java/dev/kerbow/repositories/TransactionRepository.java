package dev.kerbow.repositories;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Map;
import java.util.HashMap;

import dev.kerbow.models.Transactions;
import dev.kerbow.utils.JDBCConnection;

public class TransactionRepository implements GenericRepository<Transactions> {

	private static TransactionRepository instance;
	private Connection conn = JDBCConnection.getConnection();
	
	private TransactionRepository() {}
	
	public static TransactionRepository getInstance() {
		if(instance == null) instance = new TransactionRepository();
		return instance;
	}
	
	@Override
	public Transactions add(Transactions t) {
		String sql = "Insert into transactions values (default, ?, ?, ?, ?) returning *;";
		
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, t.getSource().getId());
			ps.setString(2, t.getType());
			ps.setFloat(3, t.getAmount());
			
			if(t.getReceiver() != null && t.getType().equalsIgnoreCase("transfer")){
				ps.setInt(4, t.getReceiver().getId());
			} else ps.setInt(4, t.getSource().getId());
			
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				t.setId(rs.getInt("id"));
				return t;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Transactions getById(Integer id) {
		String sql = "select * from transactions where id = ?;";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			
			if(rs.next()) {
				Transactions t = new Transactions();
				t.setId(rs.getInt("id"));
				t.setSource(AccountRepository.getInstance().getById(rs.getInt("source")));
				t.setType(rs.getString("type"));
				t.setAmount(rs.getFloat("amout"));
				
				if(t.getType().equalsIgnoreCase("transfer")) {
					t.setReceiver(AccountRepository.getInstance().getById(rs.getInt("receiver")));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Map<Integer, Transactions> getAll() {
		Map<Integer, Transactions> map = new HashMap<Integer, Transactions>();
		String sql = "select * from transactions;";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			
			while(rs.next()) {
				Transactions t = new Transactions();
				t.setId(rs.getInt("id"));
				t.setSource(AccountRepository.getInstance().getById(rs.getInt("source")));
				t.setType(rs.getString("type"));
				t.setAmount(rs.getFloat("amount"));
				
				if (t.getType().equalsIgnoreCase("transfer")) {
					t.setReceiver(AccountRepository.getInstance().getById(rs.getInt("receiver")));
				}
				map.put(t.getId(), t);
			}
			return map;
			
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean update(Transactions t) {
		String sql = "call \"Project0\".update_transaction(?, ?, ?)";
		try {
			CallableStatement cs = conn.prepareCall(sql);
			cs.setInt(1, t.getId());
			cs.setString(2, t.getType());
			cs.setFloat(3, t.getAmount());
			return cs.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public boolean delete(Transactions t) {
		String sql = "delete from transactions where id = ?;";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, t.getId());
			return ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
}