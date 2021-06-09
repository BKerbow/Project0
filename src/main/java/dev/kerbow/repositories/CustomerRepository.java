package dev.kerbow.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.kerbow.models.Customers;
import dev.kerbow.utils.JDBCConnection;

public class CustomerRepository implements GenericRepository<Customers> {
	private static CustomerRepository instance;
	private Connection conn = JDBCConnection.getConnection();
	
	private CustomerRepository() {}
	
	public static CustomerRepository getInstance() {
		if(instance == null) instance = new CustomerRepository();
		return instance;
	}
	
	@Override
	public Customers add(Customers c) {
		
		String sql = "insert into customers values (default, ?, ?, ?) returning *;";
		
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			
			ps.setString(1, c.getUsername());
			ps.setString(2, c.getPassword());
			ps.setBoolean(3, c.isEmployee());
			
			ResultSet rs = ps.executeQuery();
			
			if(rs.next()) {
				c.setId(rs.getInt("id"));
				return c;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public Customers getById(Integer id) {
		
		String sql = "select * from customers where id = ?;";
		
		try {
			
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			
			if (rs.next()) {
				Customers c = new Customers();
				c.setId(rs.getInt("id"));
				c.setUsername(rs.getString("username"));
				c.setPassword(rs.getString("password"));
				c.setEmployee(rs.getBoolean("employee"));
				c.setAccounts(AccountRepository.getInstance().getAllByCustomerId(c.getId()));
				return c;
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public Customers getByUsernameAndPassword(String username, String password) {
		String sql = "select * from customers where username = ? and \"password\" = ?;";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1,  username);
			ps.setString(2, password);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				Customers c = new Customers();
				c.setId(rs.getInt("id"));
				c.setUsername(rs.getString("username"));
				c.setPassword(rs.getString("password"));
				c.setEmployee(rs.getBoolean("employee"));
				c.setAccounts(AccountRepository.getInstance().getAllByCustomerId(c.getId()));
				return c;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	
		return null;
	}
	
	public List<Customers> getEmployees(){
		List<Customers> list = new ArrayList<Customers>();
		String sql = "select * from customers where employee = true;";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Customers c = new Customers();
				c.setId(rs.getInt("id"));
				c.setUsername(rs.getString("username"));
				c.setPassword(rs.getString("password"));
				c.setEmployee(rs.getBoolean("employee"));
				c.setAccounts(AccountRepository.getInstance().getAllByCustomerId(c.getId()));
				list.add(c);
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public Map<Integer, Customers> getAll() {
		Map<Integer, Customers> map = new HashMap<Integer, Customers>();
		String sql = "select * from customers;";
		
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				Customers c = new Customers();
				c.setId(rs.getInt("id"));
				c.setUsername(rs.getString("username"));
				c.setPassword(rs.getString("password"));
				c.setEmployee(rs.getBoolean("employee"));
				c.setAccounts(AccountRepository.getInstance().getAllByCustomerId(c.getId()));
				map.put(c.getId(), c);
			}
			return map;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean update(Customers c) {
		String sql = "update customers set username = ?, password = ?, employee = ? returning *;";
		
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, c.getUsername());
			ps.setString(2, c.getPassword());
			ps.setBoolean(3,  c.isEmployee());
			return ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public boolean delete(Customers c) {
		String sql = "delete from customers where id = ?;";
		
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, c.getId());
			return ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
}
