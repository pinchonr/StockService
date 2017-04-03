package com.example;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
	public Response getStockResp(@QueryParam("isbn") String isbn) {
		if(createTableIfNotExists()){
			return Response.status(200).entity(getStockByISBN(isbn)).build();
		}
		else{
			return Response.status(500).entity("An error occured when verifying if the table exits").build();
		}

	}

	private boolean createTableIfNotExists(){
		try {
			Connection connection = getConnection();
			Statement stmt = connection.createStatement();
			stmt.execute("CREATE TABLE IF NOT EXISTS LIBRARY(ID INT PRIMARY KEY NOT NULL, ISBN TEXT NOT NULL, TITLE TEXT NOT NULL, AUTHOR TEXT NOT NULL, STOCK INT NOT NULL);");
			stmt.executeUpdate("INSERT INTO LIBRARY (ISBN,TITLE,AUTHOR,STOCK) VALUES (1234567890111,TEST,TEST,20)");
			return true;
		} catch (Exception e) {
			return false;
		}
		
	}
	
	private String getStockByISBN(String isbn){
		try {
			Connection connection = getConnection();
			Statement stmt = connection.createStatement();
			ResultSet result= stmt.executeQuery("SELECT STOCK FROM LIBRARY WHERE ISBN like "+isbn+";");
			return result.getString("STOCK");
			
		} catch (Exception e) {
			return e.getMessage();
		}
		
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
		System.out.println(dbUri.toString());
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
			ResultSet result = stmt.executeQuery("SELECT * FROM library");

			String out = "";
			while (result.next()) {
				out += "Read from DB: " + result.getRow() + "\n";
			}

			return out;
		} catch (Exception e) {
			return "There was an error: " + e.getMessage();
		}
	}

}
