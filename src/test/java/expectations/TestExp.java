/*
 * Copyright (C) 2018 stuartdd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package expectations;

import client.Client;
import client.ClientConfig;
import client.ClientNotifier;
import client.ClientResponse;
import common.Util;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import server.ServerManager;
import server.ServerConfig;

/**
 *
 * @author 802996013
 */
public class TestExp {

    private static final String POST_RESPONSE_JSON = Util.cleanString("{\n"
            + "  \"showTime\" : \"/bea\",\n"
            + "  \"x\" : 788.0,\n"
            + "  \"y\" : 228.0,\n"
            + "  \"width\" : 897.0,\n"
            + "  \"height\" : 863.0\n"
            + "}");

    private static final String POST_RESPONSE_XML = Util.cleanString("<bottom>\n"
            + "    <FlowPane prefHeight=\"25.0\" prefWidth=\"600.0\" BorderPane.alignment=\"CENTER\">\n"
            + "        <children>\n"
            + "            <Separator valueFromRequestXml=\"UNAVAILABLE\" prefHeight=\"31.0\" prefWidth=\"15.0\" />\n"
            + "            <Label prefHeight=\"22.0\" prefWidth=\"539.0\" text=\"Label\" />\n"
            + "        </children>\n"
            + "    </FlowPane>\n"
            + "</bottom>");

    private static final String GET_RESPONSE = Util.cleanString("Method GET.\n"
            + "URL:'/grb'.\n"
            + "HOST:localhost:1999.\n"
            + "Accept:text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2.\n"
            + "xxx:%{xxx}");

    private static final int PORT = 1999;
    private static final Client CLIENT = new Client(new ClientConfig("http://localhost:" + PORT), new ClientNotifier(false));

    @BeforeClass
    public static void beforeClass() {
        ServerManager.addServer("" + PORT, new ServerConfig("/config/expectationsResource.json", true), new TestNotifier());
        ServerManager.startServer(PORT);
        Util.sleep(200);
    }

    @AfterClass
    public static void afterClass() {
        CLIENT.send("control/stop", null, Client.Method.PUT);
    }

    @Test
    public void testPostJson() {
        ClientResponse r = CLIENT.send("test/post/json", Util.readResource("config/testPostData.json"), Client.Method.POST);
        assertEquals(POST_RESPONSE_JSON, Util.cleanString(r.getBody()));
        assertEquals(200, r.getStatus());
    }

    @Test
    public void testPostSubBody() {
        ClientResponse r = CLIENT.send("test/post/sub/body", Util.readResource("config/testPostData.json"), Client.Method.POST);
        assertEquals(POST_RESPONSE_JSON, Util.cleanString(r.getBody()));
        assertEquals(200, r.getStatus());
    }

    @Test
    public void testPostXml() {
        ClientResponse r = CLIENT.send("test/post/xml", Util.readResource("config/testPostData.xml"), Client.Method.POST);
        assertEquals(POST_RESPONSE_XML, Util.cleanString(r.getBody()));
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
        assertEquals(GET_RESPONSE, Util.cleanString(r.getBody()));
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
