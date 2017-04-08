package com.example;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import com.example.Stock;

public class MyResourceTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(Stock.class);
    }

    /**
     * Test to see that the message "Stock service" is sent in the response.
     */
    @Test
    public void testGetIt() {
        final String responseMsg = target().path("StockService/").request().get(String.class);
        assertEquals("Stock service", responseMsg);
    }
}
