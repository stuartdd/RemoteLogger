/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package expectations;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author stuart
 */
public class MatcherStringTest {

    private TestData td;
    @Before
    public void before() {
        td = TestData.load("/config/testData.json");
    }
    
    @Test
    public void testConstructor() {
        assertEquals("exact[*|null]", (new MatcherString("\\*")).toString());
        assertEquals("exact[ \\*\\a \\n|null]", (new MatcherString(" \\*\\a \\n")).toString());
    }

    @Test
    public void testConstructorsAny() {
        assertEquals("any[null|null]", (new MatcherString(null)).toString());
        assertEquals("any[null|null]", (new MatcherString("*")).toString());
    }

    @Test
    public void testConstructorsExact() {
        assertEquals("exact[ \\*\\a|null]", (new MatcherString(" \\*\\a")).toString());
        assertEquals("exact[ \\\\ a|null]", (new MatcherString(" \\\\ a")).toString());
        assertEquals("exact[  |null]", (new MatcherString("  ")).toString());
        assertEquals("exact[ |null]", (new MatcherString(" ")).toString());
        assertEquals("exact[|null]", (new MatcherString("")).toString());
        assertEquals("exact[a  |null]", (new MatcherString("a  ")).toString());
        assertEquals("exact[a |null]", (new MatcherString("a ")).toString());
        assertEquals("exact[a|null]", (new MatcherString("a")).toString());
        assertEquals("exact[  a|null]", (new MatcherString("  a")).toString());
        assertEquals("exact[ a|null]", (new MatcherString(" a")).toString());
    }

    @Test
    public void testConstructorsStart() {
        assertEquals("MatcherString{type=start, match=a}", (new MatcherString("a*")).toString());
        assertEquals("MatcherString{type=start, match= This }", (new MatcherString(" This *")).toString());
        assertEquals("MatcherString{type=start, match= This}", (new MatcherString(" This*")).toString());
        assertEquals("MatcherString{type=start, match=This}", (new MatcherString("This*")).toString());
        assertEquals("MatcherString{type=start, match=Th\nis}", (new MatcherString("Th\nis*")).toString());
    }

    @Test
    public void testConstructorsEnd() {
        assertEquals("MatcherString{type=end, match=a}", (new MatcherString("*a")).toString());
        assertEquals("MatcherString{type=end, match= This }", (new MatcherString("* This ")).toString());
        assertEquals("MatcherString{type=end, match= This}", (new MatcherString("* This")).toString());
        assertEquals("MatcherString{type=end, match=This}", (new MatcherString("*This")).toString());
        assertEquals("MatcherString{type=end, match=Th\nis}", (new MatcherString("*Th\nis")).toString());
    }

    @Test
    public void testConstructorsMid() {
        assertEquals("MatcherString{type=mid, match=a}", (new MatcherString(" *a")).toString());
        assertEquals("MatcherString{type=mid, match= This }", (new MatcherString(" * This ")).toString());
        assertEquals("MatcherString{type=mid, match= This }", (new MatcherString(" This * ")).toString());
        assertEquals("MatcherString{type=mid, match= This}", (new MatcherString("This * This")).toString());
        assertEquals("MatcherString{type=mid, match=This}", (new MatcherString("This*This")).toString());
        assertEquals("MatcherString{type=mid, match=Th\nis}", (new MatcherString("This*Th\nis")).toString());
    }

}
