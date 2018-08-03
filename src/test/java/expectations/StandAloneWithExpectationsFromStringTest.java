/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package expectations;

import client.Client;
import client.ClientConfig;
import client.ClientNotifier;
import client.ClientResponse;
import common.Util;
import mockCallBack.MockServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author stuart
 */
public class StandAloneWithExpectationsFromStringTest {

    private static final int PORT = 1999;
    private static final Client CLIENT = new Client(new ClientConfig("http://localhost:" + PORT), new ClientNotifier(false));
    private static MockServer mockServer;

    @BeforeClass
    public static void beforeClass() {
        mockServer = (new MockServer(PORT, null,Expectations.fromString(Util.readResource("/config/expectationsResource.json")) , true)).start();
    }

    @AfterClass
    public static void afterClass() {
        mockServer.stop();
    }

    @Test
    public void test() {
        assertTrue(mockServer.isRunning());
        ClientResponse r = CLIENT.send("test/get/parts?q1=ONE&q2=TWO", null, Client.Method.GET);
        assertTrue(r.getBody().contains("PATH[0]=test PATH[1]=get PATH[2]=parts"));
        assertTrue(r.getBody().contains("QUERY.q1=ONE QUERY.q2=TWO"));
        assertEquals(200, r.getStatus());
    }
}
