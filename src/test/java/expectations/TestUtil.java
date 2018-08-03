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

import common.Util;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

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
