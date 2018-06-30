/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package expectations;

import client.Client;
import client.ClientConfig;
import main.Main;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author 802996013
 */
public class TestExp {

    private static Client client = new Client(new ClientConfig("http://localhost:1999"));

    @BeforeClass
    public static void beforeClass() {
        Main.startHeadless(1999, "/config/test001.json");
    }

    @AfterClass
    public static void afterClass() {
        client.send("control/stop", null, Client.Method.PUT);
    }

   @Test
    public void testPre() {
        String e = "ClientResponse{status=200, body=Not Found}";
        assertEquals(e, client.send("pre", null, Client.Method.POST).toString());
    }
    
    @Test
    public void testGre() {
        String e = "ClientResponse{status=200, body=Not Found}";
        assertEquals(e, client.send("gre", null, Client.Method.GET).toString());
    }
    
    @Test
    public void testGrb() {
        String e = "ClientResponse{status=200, body=Method GET.URL:'/grb'.HOST:localhost:1999.Accept:text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2.xxx:%{xxx}}";
        assertEquals(e, client.send("grb", null, Client.Method.GET).toString());
        
    }
}
