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
import mockServer.MockServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author stuart
 */
public class StandAloneWithExpectationAddTest {

    private static final int PORT = 1999;
    private static final Client CLIENT = new Client(new ClientConfig("http://localhost:" + PORT), new ClientNotifier(false));
    private static MockServer mockServer;

    private static String addExpc = "{\n"
            + "            \"name\": \"Test Add\",\n"
            + "            \"method\": \"get\",\n"
            + "            \"path\": \"/test/add\",\n"
            + "            \"response\": {\n"
            + "                \"status\": 200,\n"
            + "                \"body\": \"{\\\"msg\\\":\\\"ADD\\\"}\"\n"
            + "            }\n"
            + "        }";

    @BeforeClass
    public static void beforeClass() {
        mockServer = MockServer.fromfile("/config/expectationsResource.json").add(addExpc).start(PORT, true);
    }

    @AfterClass
    public static void afterClass() {
        mockServer.stop();
    }

    @Test
    public void test1() {
        assertTrue(mockServer.isRunning());
        ClientResponse r = CLIENT.send("test/get/parts?q1=ONE&q2=TWO", null, Client.Method.GET);
        assertTrue(r.getBody().contains("PATH[0]=test PATH[1]=get PATH[2]=parts"));
        assertTrue(r.getBody().contains("QUERY.q1=ONE QUERY.q2=TWO"));
        assertEquals(200, r.getStatus());
    }
    @Test
    public void test2() {
        assertTrue(mockServer.isRunning());
        ClientResponse r = CLIENT.send("test/add", null, Client.Method.GET);
        assertTrue(r.getBody().contains("QUERY.q1=ONE QUERY.q2=TWO"));
        assertEquals(200, r.getStatus());
    }
}
