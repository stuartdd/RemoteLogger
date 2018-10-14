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
import java.util.Map;
import mockServer.MockRequest;
import mockServer.MockResponse;
import mockServer.MockServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import server.ResponseHandler;

/**
 *
 * @author stuart
 */
public class StandAloneWithSingleExpectationFromJsonTest implements ResponseHandler {

    private static final int PORT = 1999;
    private static final Client CLIENT = new Client(new ClientConfig("http://localhost:" + PORT), new ClientNotifier(false));
    private static MockServer mockServer;

    @Before
    public void before() {
        if (mockServer == null) {
            mockServer = MockServer.add(
                    Exp.withName("Post XML").
                            withPostMethod().
                            withXmlBody().
                            withProperty("XML.BorderPane.top.ToolBar.(BorderPane.alignment)", "CENTER").
                            withResponse(
                                    Res.withStatus(202).
                                            withHeader("MyHeader", "HEAD").
                                            withHeader("MyHeader2", "HEAD2").
                                            withTemplate("config/test001.json"))
            ).add(Exp.withName("Post JSON").
                    withPostMethod().
                    withJsonBody().
                    withProperty("JSON.y", "228.0").
                    withResponse(
                            Res.withStatus(203).
                                    withHeader("MyHeader", "HEAD").
                                    withHeader("MyHeader3", "HEAD3").
                                    withTemplate("config/test001.json"))
            ).start(PORT, this, true);
        }
        assertTrue(mockServer.isRunning());
    }

    @AfterClass
    public static void afterClass() {
        mockServer.stop();
    }

    @Test
    public void testInOrder() {
        testMatchXml();
        testMatchJson();
//        testMissMatchMethod();
//        testMissMatchPath();
    }

    public void testMatchXml() {
        assertTrue(mockServer.isRunning());
        ClientResponse r = CLIENT.send("pre", Util.readResource("config/testPostData.xml").getContent(), Client.Method.POST);
        assertEquals(202, r.getStatus());
        assertEquals("HEAD2", r.getHeaderIgnoreCase("MyHeader2"));
        assertEquals("ServerStatistics{request=1, response=1, missmatch=0, match=1}", mockServer.getServerStatistics().toString());
    }

    public void testMatchJson() {
        assertTrue(mockServer.isRunning());
        ClientResponse r = CLIENT.send("pre", Util.readResource("config/post-200.json").getContent(), Client.Method.POST);
        assertEquals(203, r.getStatus());
        assertEquals("HEAD3", r.getHeaderIgnoreCase("MyHeader3"));
        assertEquals("ServerStatistics{request=2, response=2, missmatch=0, match=2}", mockServer.getServerStatistics().toString());
    }

    public void testMissMatchMethod() {
        assertTrue(mockServer.isRunning());
        ClientResponse r = CLIENT.send("pre", null, Client.Method.POST);
        assertEquals("Not Found", r.getBody());
        assertEquals(404, r.getStatus());
    }

    public void testMissMatchPath() {
        assertTrue(mockServer.isRunning());
        ClientResponse r = CLIENT.send("presss", null, Client.Method.GET);
        assertEquals("Not Found", r.getBody());
        assertEquals(404, r.getStatus());
    }

    @Override
    public MockResponse handle(MockRequest mockRequest, Map<String, Object> map) {
        return mockRequest.createResponse(map);
    }

}
