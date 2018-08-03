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
public class StandAloneWithSingleExpectationFromStringTest {

    private static final int PORT = 1999;
    private static final Client CLIENT = new Client(new ClientConfig("http://localhost:" + PORT), new ClientNotifier(false));
    private static MockServer mockServer;

    private static Expectations expectations = Expectations.newExpectation("{\n"
            + "            \"name\": \"Test Get Response Body\",\n"
            + "            \"method\": \"get\",\n"
            + "            \"path\": \"/pre\",\n"
            + "            \"response\": {\n"
            + "                \"status\": 201,\n"
            + "                \"body\": \"Method %{METHOD}.URL:'%{PATH}'.HOST:%{HEAD.Host}.Accept:%{HEAD.Accept}.\",\n"
            + "                \"headers\": {\n"
            + "                    \"Accept\": \"%{HEAD.Accept}\",\n"
            + "                    \"Connection\": \"%{HEAD.Connection}\"\n"
            + "                }\n"
            + "            }\n"
            + "        }");

    @BeforeClass
    public static void beforeClass() {
        mockServer = (new MockServer(PORT, null, expectations.withListMap(true), true)).start();
    }

    @AfterClass
    public static void afterClass() {
        mockServer.stop();
    }

    @Test
    public void test() {
        assertTrue(mockServer.isRunning());
        ClientResponse r = CLIENT.send("pre", null, Client.Method.GET);
        assertEquals("Method GET.URL:'/pre'.HOST:localhost:1999.Accept:text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2.", r.getBody());
        assertEquals("keep-alive", r.getHeader("Connection"));
    }
}
