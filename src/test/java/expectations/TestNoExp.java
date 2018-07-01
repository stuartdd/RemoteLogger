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
public class TestNoExp {

    private static Client client = new Client(new ClientConfig("http://localhost:1999"));

    @BeforeClass
    public static void beforeClass() {
        Main.startHeadless(1999, "/config/testNoExp.json");
    }

    @AfterClass
    public static void afterClass() {
        client.send("control/stop", null, Client.Method.PUT);
    }

    @Test
    public void testGrb() {
        String e = "ClientResponse{status=404, body=Not Found}";
        String r = client.send("grb", null, Client.Method.GET).toString();
        System.out.println("["+r+"]");
        assertEquals(e, r);
        
    }
}
