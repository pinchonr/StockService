package com.example;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.net.URI;



/**
 * Root resource
 */
@Path("StockService")
public class Stock {
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
		String answer=createTableIfNotExists();
		if(answer =="Table exists"){
			return Response.status(200).entity(getStockByISBN(isbn)).build();
		}
		else{
			return Response.status(500).entity("An error occured when verifying if the table exits: "+ answer).build();
		}

	}

	private String createTableIfNotExists(){
		Connection connection=null;
		try {
			 connection = getConnection();
			Statement stmt = connection.createStatement();
			stmt.execute("CREATE TABLE IF NOT EXISTS LIBRARY(ID SERIAL PRIMARY KEY NOT NULL, ISBN TEXT NOT NULL, TITLE TEXT NOT NULL, AUTHOR TEXT NOT NULL, STOCK INT NOT NULL);");
			stmt.executeUpdate("INSERT INTO LIBRARY (ISBN,TITLE,AUTHOR,STOCK) VALUES ('1234567890111','TEST','TEST','20')");
			return "Table exists";
		} catch (Exception e) {
			return e.getMessage();
		}
		finally{
			if(connection!=null){
				try{
					connection.close();
				}
				catch(Exception e){
					//ignore exception
				}
			}
			
		}
		
	}
	
	private String getStockByISBN(String isbn){
		try {
			Connection connection = getConnection();
			Statement stmt = connection.createStatement();
			ResultSet result= stmt.executeQuery("SELECT STOCK FROM LIBRARY WHERE ISBN like '"+isbn+"';");
			if(result.next()) {

				String stock = result.getString("STOCK");
				System.out.println("Stock : " + stock);
				return stock;
			}
			return "An error occured while getting the stock";
			
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
