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
        MatcherString ms = new MatcherString(testData.get(0));
        for (int i=1; i<testData.size(); i++) {
            if (data.startsWith("miss")) {
                assertFalse("["+testData.get(0) + "] Should NOT match["+testData.get(i)+"]", ms.match(testData.get(i)));
            } else {
                assertTrue("["+testData.get(0) + "] Should match["+testData.get(i)+"]", ms.match(testData.get(i)));
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
