/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package expectations;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author stuart
 */
public class MatcherStringTest {

    private TestData tdConstr;
    private TestData tdMatch;

    @Before
    public void before() {
        tdConstr = TestData.load("/config/testDataForMatcherConstructor.json");
        tdMatch = TestData.load("/config/testDataForMatch.json");
    }

    private void testConstruct(String data) {
        List<String> testData = tdConstr.map(data);
        String actual;
        if (testData.size() == 1) {
            actual = (new MatcherString(null)).toString();
            System.out.println("Test:" + data + " Expect[" + testData.get(0) + " for[null] actual[" + actual + "]");
        } else {
            actual = (new MatcherString(testData.get(1))).toString();
            System.out.println("Test:" + data + " Expect[" + testData.get(0) + " for[" + testData.get(1) + "] actual[" + actual + "]");
        }
        assertEquals(testData.get(0), actual);
    }

    private void testMatch(String data) {
        List<String> testData = tdMatch.map(data);
        String matchString = testData.get(0);
        if (matchString.equals("null")) {
            matchString = null;
        }
        MatcherString ms = new MatcherString(matchString);
        for (int i = 1; i < testData.size(); i++) {
            String matchValue = testData.get(i);
            if (matchValue.equals("null")) {
                matchValue = null;
            }
            if (data.startsWith("miss")) {
                assertFalse("[" + matchString + "] Should NOT match[" + matchValue + "]", ms.match(matchValue));
            } else {
                assertTrue("[" + matchString + "] Should match[" + matchValue + "]", ms.match(matchValue));
            }
        }
    }

    @Test
    public void testConstructor() {
        for (String testKey : tdConstr.keys()) {
            testConstruct(testKey);
        }
    }

    @Test
    public void testMatcher() {
        for (String testKey : tdMatch.keys()) {
            testMatch(testKey);
        }
    }

}
