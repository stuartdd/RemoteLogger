/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package expectations;

import common.Util;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author 802996013
 */
public class MultiStringMatch {

    private final List<StringMatcher> list = new ArrayList<>();
    private final char delim;
    
    public MultiStringMatch(String in, char delim) {
        this.delim = delim;
        List<String> strings = Util.split(in, delim);
        for (String st : strings) {
            list.add(new StringMatcher(st));
        }
    }

    boolean match(Object with) {
        if (with == null) {
            return false;
        }
        List<String> strings = Util.split(with.toString(), delim);
        if (strings.size()!=list.size()) {
            return false;
        }       
        for (int i=0; i< strings.size();i++) {
            if (!list.get(i).match(strings.get(i))) {
                return false;
            }
        }
        return true;
    }

}
