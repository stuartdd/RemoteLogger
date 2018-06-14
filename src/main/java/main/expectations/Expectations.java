/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.expectations;

import java.util.List;

/**
 *
 * @author 802996013
 */
public class Expectations {

    private List<Expectation> expectations;
    private String[] paths;
    private boolean listMap;

    public List<Expectation> getExpectations() {
        return expectations;
    }

    public void setExpectations(List<Expectation> expectations) {
        this.expectations = expectations;
    }

    public String[] getPaths() {
        return paths;
    }

    public void setPaths(String[] paths) {
        this.paths = paths;
    }

    public boolean isListMap() {
        return listMap;
    }

    public void setListMap(boolean listMap) {
        this.listMap = listMap;
    }
   
}
