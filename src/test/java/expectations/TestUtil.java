/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package expectations;

import common.Util;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author 802996013
 */
public class TestUtil {

    @Test
    public void testUtilSplit() {
        List<String> l = Util.split("A/b/c", '/');
        assertEquals(3, l.size());
        assertEquals("A", l.get(0));
        assertEquals("b", l.get(1));
        assertEquals("c", l.get(2));
    }
    @Test
    public void testUtilSplitWithEmpty() {
        List<String> l = Util.split("A//c", '/');
        assertEquals(3, l.size());
        assertEquals("A", l.get(0));
        assertEquals("", l.get(1));
        assertEquals("c", l.get(2));
    }
    @Test
    public void testUtilSplitWithEmptyEnd() {
        List<String> l = Util.split("A/b/c/", '/');
        assertEquals(3, l.size());
        assertEquals("A", l.get(0));
        assertEquals("b", l.get(1));
        assertEquals("c", l.get(2));
    }
    @Test
    public void testUtilSplitWithEmptyStart() {
        List<String> l = Util.split("/A/b/c/", '/');
        assertEquals(4, l.size());
        assertEquals("", l.get(0));
        assertEquals("A", l.get(1));
        assertEquals("b", l.get(2));
        assertEquals("c", l.get(3));
    }
}
