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
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import server.ResponseHandler;

/**
 *
 * @author stuart
 */
public class StandAloneWithSingleExpectationFromChainWithTemplateTest2 implements ResponseHandler {

    private static final int PORT = 1999;
    private static final Client CLIENT = new Client(new ClientConfig("http://localhost:" + PORT), new ClientNotifier(true));
    private static MockServer mockServer;
    private static int eventCount = 0;

    @Before
    public void before() {
        if (mockServer == null) {
            mockServer = MockServer.add(
                    Exp.withName("Post Test").
                            withPostMethod().
                            withXmlBody().
                            withProperty("A", "B").
                            withResponse(
                                    Res.withStatus(202).
                                            withHeader("MyHeader", "HEAD").
                                            withHeader("MyHeader2", "HEAD2").
                                            withTemplate("config/Test001.json"))
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
//        testMissMatchMethod();
//        testMissMatchPath();
    }

    public void testMatch() {
        ClientResponse r = CLIENT.send("pre", Util.readResource("config/testPostData.xml"), Client.Method.POST);
        assertEquals(202, r.getStatus());
        assertEquals("HEAD", r.getHeader("Myheader"));
        assertEquals("HEAD2", r.getHeader("Myheader2"));
        assertEquals(202, r.getStatus());
        assertTrue(r.getBody().contains("logDateFormat"));
    }

    @Override
    public MockResponse handle(MockRequest mockRequest, Map<String, Object> map) {
        eventCount++;
        return mockRequest.createResponse(map);
    }
}
