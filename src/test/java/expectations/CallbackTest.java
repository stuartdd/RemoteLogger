/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package expectations;

import client.Client;
import client.ClientConfig;
import client.ClientNotifier;
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
public class CallbackTest {
    private static final int PORT = 1999;
    private static final Client CLIENT = new Client(new ClientConfig("http://localhost:" + PORT), new ClientNotifier(false));
    private static MockServer mockServer;
    
    @BeforeClass
    public static void beforeClass() {
        mockServer = (new MockServer(PORT, null, "/config/expectationsResource.json", true)).start();
        Util.sleep(200);
    }

    @AfterClass
    public static void afterClass() {
        mockServer.stop();
    }

    @Test
    public void testCallback() {
        assertTrue(mockServer.isRunning());
    }
}
