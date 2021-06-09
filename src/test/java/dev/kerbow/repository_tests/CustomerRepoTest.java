package dev.kerbow.repository_tests;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import dev.kerbow.models.Accounts;
import dev.kerbow.models.Customers;
import dev.kerbow.repositories.CustomerRepository;
import dev.kerbow.utils.JDBCConnection;

public class CustomerRepoTest {
	private static Savepoint sp;
	private static Connection conn;
	private Integer expectedId = 4;
	
	@BeforeClass
	public static void beforeClass() {
		conn = JDBCConnection.getConnection();
	}
	
	@Before
	public void before() {
		try {
			conn.setAutoCommit(false);
			sp = conn.setSavepoint();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void addTest() {
		Customers expected = new Customers(expectedId, "test", "test", true);
		Customers c = new Customers("test", "test", true);
		Customers result = CustomerRepository.getInstance().add(c);
		
		Assert.assertEquals(expected, result);
	}
	
	@Test
	public void getByIdTest() {
		Accounts a1 = new Accounts(1, 500000.5f);
		a1.setCustomer_id(2);
		a1.setPending(false);
		Accounts a2 = new Accounts(2, 30.25f);
		a2.setCustomer_id(2);
		a2.setPending(false);
		Customers expected = new Customers("joseph", "suits", a1, a2);
		expected.setId(2);
		Customers result = CustomerRepository.getInstance().getById(2);
		Assert.assertEquals(expected, result);
	}
	
	@Test
	public void getByUAndPTest() {
		Accounts a1 = new Accounts(1, 50000.f);
		a1.setCustomer_id(2);
		a1.setPending(false);
		Accounts a2 = new Accounts(2, 30.25f);
		a2.setCustomer_id(2);
		a2.setPending(false);
		Customers expected = new Customers("hussar", "cavalry", a1, a2);
		expected.setId(2);
		Customers result = CustomerRepository.getInstance().getByUsernameAndPassword("hussar", "cavalry");
		Assert.assertEquals(expected, result);
	}
	
	@After
	public void after() {
		try {
			conn.rollback(sp);
			conn.setAutoCommit(true);
			String sql = String.format("alter sequence customers_ud_seq restart with %d;", expectedId);
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.executeUpdate();
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
