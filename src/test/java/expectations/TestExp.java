/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package expectations;

import client.Client;
import client.ClientConfig;
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

   private static int PORT = 1999;
    private static Client client = new Client(new ClientConfig("http://localhost:"+PORT));

    @BeforeClass
    public static void beforeClass() {
//        Main.startHeadless(PORT, "/config/test001.json");
    }

    @AfterClass
    public static void afterClass() {
 //       client.send("control/stop", null, Client.Method.PUT);
    }

   @Test
    public void testPost() {
        String e = "ClientResponse{status=201, body=Response is undefined}";
        String r = client.send("test/post/xml", Util.readResource("config/testPostData.xml"), Client.Method.POST).toString();
        assertEquals(e, r);
    }
    
    
   @Test
    public void testPre() {
        String e = "ClientResponse{status=201, body=Response is undefined}";
        String r = client.send("pre", null, Client.Method.POST).toString();
        assertEquals(e, r);
    }
    
    @Test
    public void testGre() {
        String e = "ClientResponse{status=200, body=Response is undefined}";
        String r = client.send("gre", null, Client.Method.GET).toString();
        assertEquals(e, r);
    }
    
    @Test
    public void testGrb() {
        String e = "ClientResponse{status=200, body=Method GET.URL:'/grb'.HOST:localhost:1999.Accept:text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2.xxx:%{xxx}}";
        String r = client.send("grb", null, Client.Method.GET).toString();
        System.out.println("["+r+"]");
        assertEquals(e, r);
        
    }
}
