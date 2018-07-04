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
    private static final int PORT = 1889;
    private static final Client CLIENT = new Client(new ClientConfig("http://localhost:"+PORT));

    @BeforeClass
    public static void beforeClass() {
        Main.startHeadless(PORT, "/config/testNoExp.json");
    }

    @AfterClass
    public static void afterClass() {
        CLIENT.send("control/stop", null, Client.Method.PUT);
    }

    @Test
    public void testNoExpGet() {
        String e = "ClientResponse{status=404, body=Not Found}";
        String r = CLIENT.send("gettest", null, Client.Method.GET).toString();
        assertEquals(e, r);
        
    }
    @Test
    public void testNoExpPost() {
        String e = "ClientResponse{status=404, body=Not Found}";
        String r = CLIENT.send("posttest", "<XML/>", Client.Method.POST).toString();
        assertEquals(e, r);
    }
}
