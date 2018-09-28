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
public class StandAloneWithSingleExpectationFromStringTest {

    private static final int PORT = 1999;
    private static final Client CLIENT = new Client(new ClientConfig("http://localhost:" + PORT), new ClientNotifier(true));
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
        mockServer = MockServer.list(expectations).start(PORT,true);
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
