package com.example;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.example.model.Book;
import com.example.model.JsonError;
import com.google.gson.Gson;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.net.URI;



/**
 * Root resource (exposed at "StockService" path)
 */
@Path("StockService")
public class Stock {

	/**
	 * Simply display a text
	 */
	@Path("/")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getStockServiceHome(){
		return "Stock service";
	}

	/**
	 *Return all rows in the Library table
	 */
	@Path("/stocks")    
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response showAllProducts()
	{
		String answer=createTableIfNotExists();
		if(answer =="Table exists"){
			Statement stmt = null;
			ResultSet result= null;
			Connection connection = null;
			try {
				connection = getConnection();
				stmt = connection.createStatement();
				result = stmt.executeQuery("SELECT * FROM library");

				List<Book> bookList= new ArrayList<>();
				while (result.next()) {
					bookList.add(new Book(+result.getRow(),result.getString("isbn"),result.getString("title"),result.getString("author"),result.getInt("stock")));
				}
				String jsonList=new Gson().toJson(bookList);
				return Response.status(200).entity(jsonList).type(MediaType.APPLICATION_JSON).build();
			} 
			catch (Exception e) {
				return createJsonError(500, "StockService: There was an error while listing all books in db: " + e.getMessage());
			}
			finally{
				try { if (stmt != null) stmt.close(); } catch (Exception e) {};
				try { if (result != null) result.close(); } catch (Exception e) {};
				try { if (connection != null) connection.close(); } catch (Exception e) {};
			}
		}
		else{
			return createJsonError(500,"An error occured when verifying if the table exits: "+ answer);
		}

	}

	/**
	 * Ask for stock in database after having verified the sender and the destination (and the isbn once again!)
	 * @param isbn
	 * @param from
	 * @param to
	 * @return Response
	 */
	@Path("/stocks/{isbn}")    
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStockResp(@PathParam("isbn") String isbn, @QueryParam("from") String from, @QueryParam("to") String to) {

		//Example of valid isbn13: 978-0-596-52068-7 matched by this regex
		String regex="^(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$";

		//GUARD
		if(!from.equals("Shop")||!to.equals("Stock")){
			return createJsonError(500, "Invalid sender or wrong destination");
		}
		//Double check for isbn
		if(!isbn.matches(regex)){
			return createJsonError(400,"Invalid isbn 13!");
		}

		String answer=createTableIfNotExists();
		if(answer =="Table exists"){
			return getStockByISBN(isbn);
		}
		else{
			return createJsonError(500, "An error occured when verifying if the table exits: "+ answer);
		}
	}


	/**
	 * Update the given isbn stock with the given stock value
	 * @param isbn
	 * @param stock
	 * @param from
	 * @param to
	 * @return
	 */
	@Path("/stocks/{isbn}")    
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	public Response putStockReq(@PathParam("isbn") String isbn, @QueryParam("newStock") int nouveauStock, @QueryParam("from") String from, @QueryParam("to") String to) {

		//Example of valid isbn13: 978-0-596-52068-7 matched by this regex
		String regex="^(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$";

		//GUARD
		if(!from.equals("Shop")||!to.equals("Stock")){
			return createJsonError(400,"Invalid sender or wrong destination");
		}
		//Double check for isbn
		if(!isbn.matches(regex)){
			return createJsonError(400,"Invalid isbn 13!");
		}
		if(nouveauStock<0){
			return createJsonError(400,"Stock can't be less than 0!");
		}

		String answer=createTableIfNotExists();
		if(answer =="Table exists"){
			return setStockForIsbn(isbn, nouveauStock);
			/*if(setStockForIsbn(isbn, nouveauStock)){
				return getStockByISBN(isbn);		
			}
			else{
				return createJsonError(500, "An error occured while updating stock");
			}*/
		}
		else{
			return createJsonError(500,"An error occured when verifying if the table exits: "+ answer);
		}
	}


	/**
	 * Open a connection with the database
	 */
	private Connection getConnection() throws URISyntaxException, SQLException {
		// Class.forName("org.postgresql.Driver");
		URI dbUri = new URI(System.getenv("DATABASE_URL"));
		String username = dbUri.getUserInfo().split(":")[0];
		String password = dbUri.getUserInfo().split(":")[1];
		String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath();

		return DriverManager.getConnection(dbUrl, username, password);
	}

	/**
	 *Creates the Library table in database if it doesn't exist and insert a default value.
	 */
	private String createTableIfNotExists(){
		Connection connection=null;
		Statement stmt = null;
		try {
			connection = getConnection();
			stmt = connection.createStatement();
			stmt.execute("CREATE TABLE IF NOT EXISTS LIBRARY(ID SERIAL PRIMARY KEY NOT NULL, ISBN TEXT NOT NULL UNIQUE, TITLE TEXT NOT NULL, AUTHOR TEXT NOT NULL, STOCK INT NOT NULL);");
			stmt.executeUpdate("INSERT INTO LIBRARY (ISBN,TITLE,AUTHOR,STOCK) VALUES ('978-0-3213-5668-0','Effective Java','Joshua Bloch',20) ON CONFLICT DO NOTHING;");
			return "Table exists";
		} catch (Exception e) {
			return e.getMessage();
		}
		finally{
			try { if (stmt != null) stmt.close(); } catch (Exception e) {};
			try { if (connection != null) connection.close(); } catch (Exception e) {};	
		}
	}

	/**
	 * Get the stock of the book for the given ISBN
	 * @param isbn
	 * @return Response
	 */
	private Response getStockByISBN(String isbn){
		Statement stmt = null;
		ResultSet result = null;
		Connection connection = null;
		try {
			connection = getConnection();
			stmt = connection.createStatement();
			result= stmt.executeQuery("SELECT STOCK FROM LIBRARY WHERE ISBN LIKE '"+isbn+"';" );
			if(result.next()) {
				String stock = String.valueOf(result.getInt("STOCK"));
				String json ="{\"isbn\":\""+isbn+"\", \"stock\":"+stock+"}";
				return Response.status(200).entity(json).type(MediaType.APPLICATION_JSON).build();
			}
			return createJsonError(404,"can't find the stock for the given isbn "+isbn+" maybe this book is not in our database... yet!");

		} catch (Exception e) {
			return createJsonError(500,"Stock Service: an error occured while getting the stock: "+e.getMessage());
		}
		finally{
			try { if (stmt != null) stmt.close(); } catch (Exception e) {};
			try { if (result != null) result.close(); } catch (Exception e) {};
			try { if (connection != null) connection.close(); } catch (Exception e) {};
		}
	}

	/**
	 * Set the stock of the given isbn with given stock value
	 * @param isbn
	 * @param stock 
	 * @return boolean
	 */
	private Response setStockForIsbn(String isbn, int stock){
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = getConnection();
			stmt = connection.createStatement();
			int result= stmt.executeUpdate("UPDATE LIBRARY SET STOCK=" + stock + "WHERE ISBN LIKE'"+isbn+"';" );
			return Response.status(200).entity(result).build();
		} catch (Exception e) {
			return Response.status(200).entity(e.getMessage()).build();
		}
		finally{
			try { if (stmt != null) stmt.close(); } catch (Exception e) {};
			try { if (connection != null) connection.close(); } catch (Exception e) {};
		}

	}
	/**
	 * Create a response containing status and error message in JSON
	 * @param status
	 * @param message
	 * @return Response
	 */
	public Response createJsonError(int status, String message){
		return Response.status(status).type(MediaType.APPLICATION_JSON).entity(new JsonError(status,message).toJson()).build();
	}



}
