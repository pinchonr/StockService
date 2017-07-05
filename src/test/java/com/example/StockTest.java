package com.example;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import com.example.Stock;

public class StockTest extends JerseyTest {

    @Override
    protected Application configure() {
    	enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        return new ResourceConfig(Stock.class);
    }

    /**
     * Test to see that the message "Stock service" is sent in the response.
     */
    @Test
    public void testGetIt() {
        final String responseMsg = target("/StockService").request().get(String.class);
        assertEquals("Stock service", responseMsg);
    }
    
    /**
     * Test to see that the GUARD work correctly
     */
    @Test
    public void testGetStockRespWithNoQueryParams() {
        final Response response = target("/StockService/stocks/1234567891011").request().get(Response.class);
        assertEquals(500, response.getStatus());
    }
    
    /**
     * Test to see that the response is {"status":"400", "error":"Invalid isbn 13!"}
     */
    @Test
    public void testGetStockRespWithInvalidIsbn() {
        final Response response = target("/StockService/stocks/1234567891011").queryParam("from", "Shop").queryParam("to", "Stock").request().get(Response.class);
        assertEquals(400, response.getStatus());
        assertEquals("{\"status\":\"400\", \"error\":\"Invalid isbn 13!\"}", response.readEntity(String.class));
    }
    
    /**
     * Test to see that the response is {"isbn":"978-0-3213-5668-0", "stock":"20"}
     */
    @Test
    public void testGetStockRespWithValidIsbn() {
        final Response response = target("/StockService/stocks/978-0-3213-5668-0").queryParam("from", "Shop").queryParam("to", "Stock").request().get(Response.class);
        assertEquals(200, response.getStatus());
        assertEquals("{\"isbn\":\"978-0-3213-5668-0\", \"stock\":20}",response.readEntity(String.class));
    }
    
    /**
     * Test to see that a list of json objects is sent in response
     */
    @Test
    public void testShowAllProducts() {
        final Response response = target("/StockService/stocks").queryParam("from", "Shop").queryParam("to", "Stock").request().get(Response.class);
        assertEquals(200, response.getStatus());
        assertEquals("[{\"id\":1,\"isbn\":\"978-0-3213-5668-0\",\"title\":\"Effective Java\",\"author\":\"Joshua Bloch\",\"stock\":20}]", response.readEntity(String.class));
    }
}
