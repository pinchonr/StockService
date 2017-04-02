package com.example;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.sql.*;
import java.net.URISyntaxException;
import java.net.URI;



/**
 * Root resource
 */
@Path("StockService")
public class Stock {
	private boolean hasTableBeenCreated=false;
	
	@Path("/")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getStockServiceHome(){
		return "Stock service";
		
	}

	/**
	 * Method handling HTTP GET requests. The returned object will be sent
	 * to the client as "text/plain" media type.
	 *
	 * @return String that will be returned as a text/plain response.
	 */
	@Path("/stock/{isbn}/")    
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public int getStockResp() {
		if(!hasTableBeenCreated){
			createTable();
		}
		
		return 0;
	}

	@Path("db")    
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getItbd() {
		return showDatabase();
	}

	private Connection getConnection() throws URISyntaxException, SQLException {
		// Class.forName("org.postgresql.Driver");
		URI dbUri = new URI(System.getenv("DATABASE_URL"));

		String username = dbUri.getUserInfo().split(":")[0];
		String password = dbUri.getUserInfo().split(":")[1];
		String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath();

		return DriverManager.getConnection(dbUrl, username, password);
	}

	private String showDatabase()
	{
		try {
			Connection connection = getConnection();

			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM library");

			String out = "";
			while (rs.next()) {
				out += "Read from DB: " + rs.getRow() + "\n";
			}

			return out;
		} catch (Exception e) {
			return "There was an error: " + e.getMessage();
		}
	}
	
	private boolean createTable(){
		try {
			Connection connection = getConnection();

			Statement stmt = connection.createStatement();
			stmt.executeUpdate("CREATE TABLE LIBRARY(ID INT PRIMARY KEY NOT NULL, ISBN BIGINT NOT NULL, TITLE TEXT NOT NULL, AUTHOR TEXT NOT NULL, STOCK INT NOT NULL);");
			hasTableBeenCreated=true;
			return true;
		} catch (Exception e) {
			hasTableBeenCreated=false;
			return false;
		}
		
	}

}
