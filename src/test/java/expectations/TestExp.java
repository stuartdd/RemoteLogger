/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package expectations;

import client.Client;
import client.ClientConfig;
import client.ClientResponse;
import main.Main;
import main.Util;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author 802996013
 */
public class TestExp {

    private static final String POST_RESPONSE_JSON = "{\n"
            + "  \"showTime\" : \"/bea\",\n"
            + "  \"x\" : 788.0,\n"
            + "  \"y\" : 228.0,\n"
            + "  \"width\" : 897.0,\n"
            + "  \"height\" : 863.0\n"
            + "}";

    private static final String POST_RESPONSE_XML = "<bottom>\n"
            + "    <FlowPane prefHeight=\"25.0\" prefWidth=\"600.0\" BorderPane.alignment=\"CENTER\">\n"
            + "        <children>\n"
            + "            <Separator valueFromRequestXml=\"UNAVAILABLE\" prefHeight=\"31.0\" prefWidth=\"15.0\" />\n"
            + "            <Label prefHeight=\"22.0\" prefWidth=\"539.0\" text=\"Label\" />\n"
            + "        </children>\n"
            + "    </FlowPane>\n"
            + "</bottom>";

    private static final String GET_RESPONSE = "Method GET.\n"
            + "URL:'/grb'.\n"
            + "HOST:localhost:1999.\n"
            + "Accept:text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2.\n"
            + "xxx:%{xxx}";

    private static final int PORT = 1999;
    private static final Client CLIENT = new Client(new ClientConfig("http://localhost:" + PORT));

    @BeforeClass
    public static void beforeClass() {
        Main.startHeadless(PORT, "/config/test001.json");
    }

    @AfterClass
    public static void afterClass() {
        CLIENT.send("control/stop", null, Client.Method.PUT);
    }

    @Test
    public void testPostJson() {
        ClientResponse r = CLIENT.send("test/post/json", Util.readResource("config/testPostData.json"), Client.Method.POST);
        assertEquals(POST_RESPONSE_JSON, r.getBody());
        assertEquals(200, r.getStatus());
    }

    @Test
    public void testPostXml() {
        ClientResponse r = CLIENT.send("test/post/xml", Util.readResource("config/testPostData.xml"), Client.Method.POST);
        assertEquals(POST_RESPONSE_XML, r.getBody());
        assertEquals(200, r.getStatus());
    }

    @Test
    public void testPre() {
        String e = "ClientResponse{status=201, body=Response is undefined}";
        String r = CLIENT.send("pre", null, Client.Method.POST).toString();
        assertEquals(e, r);
    }

    @Test
    public void testGre() {
        String e = "ClientResponse{status=200, body=Response is undefined}";
        String r = CLIENT.send("gre", null, Client.Method.GET).toString();
        assertEquals(e, r);
    }

    @Test
    public void testGrb() {
        ClientResponse r = CLIENT.send("grb", null, Client.Method.GET);
        assertEquals(GET_RESPONSE, r.getBody());
        assertEquals(200, r.getStatus());
    }

    @Test
    public void testGetParts() {
        ClientResponse r = CLIENT.send("test/get/parts?q1=ONE&q2=TWO", null, Client.Method.GET);
        assertEquals("PATH[0]=test PATH[1]=get PATH[2]=parts [%{PATH[3]}] QUERY.q1=ONE QUERY.q2=TWO [%{QUERY.q3}]", r.getBody());
        assertEquals(200, r.getStatus());
    }

    @Test
    public void testFileNotFound() {
        ClientResponse r = CLIENT.send("test/getNoFile", null, Client.Method.GET);
        assertEquals("Not Found", r.getBody());
        assertEquals(404, r.getStatus());
    }
}
