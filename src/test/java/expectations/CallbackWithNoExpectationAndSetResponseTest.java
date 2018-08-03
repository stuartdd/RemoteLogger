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
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import server.ResponseHandler;

/**
 *
 * @author stuart
 */
public class CallbackWithNoExpectationAndSetResponseTest {

    private static final int PORT = 1999;
    private static final Client CLIENT = new Client(new ClientConfig("http://localhost:" + PORT), new ClientNotifier(true));
    private static MockServer mockServer;

    private static String response = "{\"response\":\"This is the response\"}";
    @BeforeClass
    public static void beforeClass() {
        mockServer = (new MockServer(PORT, null, true)).start();
    }

    @AfterClass
    public static void afterClass() {
        mockServer.stop();
    }

    @Test
    public void testHandleResponse() {
        assertTrue(mockServer.isRunning());
        mockServer.setCallBackClass(new ResponseHandler() {
            @Override
            public MockResponse handle(MockRequest mockRequest, Map<String, Object> map) {
                return new MockResponse(response, 200, null);
            }
        });
        ClientResponse r = CLIENT.send("pre", null, Client.Method.GET);
        assertEquals(response, r.getBody());
    }
}
