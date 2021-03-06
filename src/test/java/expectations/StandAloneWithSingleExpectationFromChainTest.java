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
import java.util.Map;
import mockServer.MockRequest;
import mockServer.MockResponse;
import mockServer.MockServer;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import server.ResponseHandler;

/**
 *
 * @author stuart
 */
public class StandAloneWithSingleExpectationFromChainTest implements ResponseHandler {

    private static final int PORT = 1999;
    private static final Client CLIENT = new Client(new ClientConfig("http://localhost:" + PORT), new ClientNotifier(true));
    private static MockServer mockServer;
    private static int eventCount = 0;

    @Before
    public void before() {
        if (mockServer == null) {
            mockServer = MockServer.add(
                    Exp.withGetMethod().
                            withPath("/pre").
                            withResponse(
                                    Res.withStatus(201).
                                            withHeader("MyHeader", "HEAD").
                                            withBody("Status 201"))
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
        testMatch();
        testMissMatchMethod();
        testMissMatchPath();
    }

    public void testMatch() {
        ClientResponse r = CLIENT.send("pre", null, Client.Method.GET);
        assertEquals("HEAD", r.getHeader("Myheader"));
        assertEquals(201, r.getStatus());
        assertEquals("Status 201", r.getBody());
        assertEquals("ServerStatistics{request=1, response=1, missmatch=0, match=1}", mockServer.getServerStatistics().toString());
        assertEquals(1, eventCount);
    }

    public void testMissMatchMethod() {
        ClientResponse r = CLIENT.send("pre", null, Client.Method.PUT);
        assertEquals(404, r.getStatus());
        assertEquals("ServerStatistics{request=2, response=2, missmatch=1, match=1}", mockServer.getServerStatistics().toString());
        assertEquals(1, eventCount);
    }

    public void testMissMatchPath() {
        ClientResponse r = CLIENT.send("pres", null, Client.Method.GET);
        assertEquals(404, r.getStatus());
        assertEquals("ServerStatistics{request=3, response=3, missmatch=2, match=1}", mockServer.getServerStatistics().toString());
        assertEquals(1, eventCount);
    }

    @Override
    public MockResponse handle(MockRequest mockRequest, Map<String, Object> map) {
        eventCount++;
        return mockRequest.createResponse(map);
    }
}
