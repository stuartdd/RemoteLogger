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
import mockServer.MockServerBuilder;
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
public class StandAloneWithConfigAndCallbackReloadedTest {

    private static final int PORT = 1999;
    private static final Client CLIENT = new Client(new ClientConfig("http://localhost:" + PORT), new ClientNotifier(false));
    private static MockServer mockServer;

    @BeforeClass
    public static void beforeClass() {
        ResponseHandler handler = new ResponseHandler() {
            @Override
            public MockResponse handle(MockRequest mockRequest, Map<String, Object> map) {
                /*
                Add missing data.
                */
                map.put("PATH[3]", "PATH[3]");
                map.put("QUERY.q3", "QUERY.q3");
                /*
                Defer to the loaded expectations
                */
                return mockRequest.getResponseData(map);
            }
        };
        mockServer = MockServer.fromfile("/config/expectationsResource.json").start(PORT, handler, true);
    }   

    @AfterClass
    public static void afterClass() {
        mockServer.stop();
    }

    @Test
    public void test() {
        assertTrue(mockServer.isRunning());
        ClientResponse r = CLIENT.send("test/get/parts?q1=ONE&q2=TWO", null, Client.Method.GET);
        assertEquals("PATH[0]=test PATH[1]=get PATH[2]=parts [PATH[3]] QUERY.q1=ONE QUERY.q2=TWO [QUERY.q3]", r.getBody());
        assertEquals(200, r.getStatus());
    }

}
