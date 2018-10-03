package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.ThrowStatement;

import com.mysql.cj.jdbc.Driver;

import db.DBConnection;
import entity.AdItem;
import entity.AdItem.AdItemBuilder;

public class MySQLConnection implements DBConnection{
	private Connection conn;
	public MySQLConnection() {
		// TODO Auto-generated constructor stub
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);	
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		if(conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	@Override
	public List<AdItem> searchAdItems() {
		// TODO Auto-generated method stub
		if(conn == null) {
			System.out.println("DB connection failed!");
			return new ArrayList<>();
		}
		List<AdItem> adItems = new ArrayList<>();
		String sql = "SELECT * FROM ad LEFT JOIN advertiser on ad.advertiser_id = advertiser.advertiser_id WHERE ad.bid > 0 AND advertiser.budget > 0";
		try {
			PreparedStatement stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			AdItemBuilder builder = new AdItemBuilder();
			
			while(rs.next()) {
				builder.setAd_id(rs.getInt("ad_id"));
				builder.setBid(rs.getFloat("bid"));
				builder.setImage_url(rs.getString("image_url"));
				builder.setAdvertiser_id(rs.getInt("advertiser_id"));
				builder.setAd_score(rs.getFloat("ad_score"));
				adItems.add(builder.build());
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return adItems;
	}

	@Override
	public float getBudget(int advertiser_id) {
		// TODO Auto-generated method stub
		if(conn == null) {
			System.err.println("DB connection failed!");
		}
		float curBudget = -1;
		String sql = "SELECT * FROM advertiser WHERE advertiser_id = (?)";
		try {
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, advertiser_id);
			ResultSet rs = stmt.executeQuery();
			
			while(rs.next()) {
				curBudget = rs.getFloat("budget");
			}
			System.out.println("curBudget" + curBudget);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return curBudget;
	}
	@Override
	public void updateBudget(int advertiser_id, double budget) {
		// TODO Auto-generated method stub
		if (conn == null) {
			System.err.println("DB connection failed!");
		}
		try {
			String sql = "UPDATE advertiser SET budget=(?) WHERE advertiser_id=(?)";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setDouble(1, budget);
			stmt.setInt(2, advertiser_id);
			stmt.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void updateBid(int ad_id, double bid) {
		// TODO Auto-generated method stub
		if(conn == null) {
			System.err.println("DB connection failed!");
			try {
				String sql = "UPDATE ad SET bid=(?) WHERE ad_id=(?)";
				PreparedStatement stmt = conn.prepareStatement(sql);
				stmt.setDouble(1, bid);
				stmt.setInt(2, ad_id);
				System.out.println(stmt.toString());
				
			} catch (SQLException e) {
				// TODO: handle exception
			}finally {
				System.out.println("update bid done");
			}
		}
		
	}

	@Override
	public long createAdvertiser(String advertiser_name, double budget) {
		// TODO Auto-generated method stub
		if(conn == null) {
			System.out.println("DB connection failed!");
		}
		String sql = "INSERT INTO advertiser (name,budget) VALUES ((?), (?))";
		try {
			PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, advertiser_name);
			stmt.setDouble(2, budget);
			System.out.println(stmt.toString());
			
			int affectedRows = stmt.executeUpdate();
			if(affectedRows == 0) {
				throw new SQLException("Creating advertiser failed, no rows affected.");
			}
	        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
	            if (generatedKeys.next()) {
	            	return generatedKeys.getLong(1);
	            }
	            else {
	                throw new SQLException("Creating advertiser failed, no ID obtained.");
	            }
	        }
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("insert advertiser done");
		return -1;
	}

	@Override
	public long createAd(double bid, String image_url, int advertiser_id, double ad_score) {
		// TODO Auto-generated method stub
		if(conn == null) {
			System.out.println("DB connection failed!");
		}
		String sql = "INSERT INTO ad (bid, image_url, advertiser_id, ad_score) VALUES ((?), (?), (?), (?))";
		try {
			PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			stmt.setDouble(1, bid);
			stmt.setString(2, image_url);
			stmt.setInt(3, advertiser_id);
			stmt.setDouble(4, ad_score);
			System.out.println(stmt.toString());
			
			int affectedRows = stmt.executeUpdate();
			if(affectedRows == 0) {
				throw new SQLException("Creating ad failed, no rows affected.");
			}
			try {
				ResultSet generatedKeys = stmt.getGeneratedKeys();
				if(generatedKeys.next()) {
					return generatedKeys.getLong(1);
				}else {
					throw new SQLException("Creating ad failed, no ID obtained.");
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("insert ad done");
		return -1;
	}

}