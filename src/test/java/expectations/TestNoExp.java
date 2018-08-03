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
import mockServer.MockServer;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestNoExp {

    private static MockServer mockServer;
    private static final int PORT = 1889;
    private static final Client CLIENT = new Client(new ClientConfig("http://localhost:" + PORT), new ClientNotifier(true));

    @BeforeClass
    public static void beforeClass() {
        mockServer = (new MockServer(PORT, null, "/config/testNoExp.json", true)).start();
    }

    @AfterClass
    public static void afterClass() {
        mockServer.stop();
    }

    @Test
    public void testNoExpGet() {
        String e = "ClientResponse{status=404, body=No Expectation defined}";
        String r = CLIENT.send("gettest", null, Client.Method.GET).toString();
        assertEquals(e, r);
    }

    @Test
    public void testNoExpPost() {
        String e = "ClientResponse{status=404, body=No Expectation defined}";
        String r = CLIENT.send("posttest", "<XML/>", Client.Method.POST).toString();
        assertEquals(e, r);
    }
}
