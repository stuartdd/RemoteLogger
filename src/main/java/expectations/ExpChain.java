/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package expectations;

import java.util.HashMap;

/**
 *
 * @author 802996013
 */
public class ExpChain {
    Expectation expectation = new Expectation();
    private static int nameCounter = 0;
    
    public ExpChain() {
        expectation.setName("Exp:"+nameCounter++);
        expectation.setMethod(null);
        expectation.setBodyType(null);
        expectation.setForward(null);
        expectation.setPath(null);
        expectation.setResponse(null);
        expectation.setAsserts(new HashMap<>());
    }


    public Expectation getExpectation() {
        return expectation;
    }
    
    public ExpChain withPath(String path) {
        expectation.setPath(path);
        return this;
    }
    
    public ExpChain withResponse(ResChain res) {
        expectation.setResponse(res.responseContent);
        return this;
    }
    
    public ExpChain withGetMethod() {
        expectation.setMethod("GET");
        return this;
    }
    
    public ExpChain withPostMethod() {
        expectation.setMethod("POST");
        return this;
    }
    
    public ExpChain withPutMethod() {
        expectation.setMethod("PUT");
        return this;
    }
    
    public ExpChain withPatchMethod() {
        expectation.setMethod("PATCH");
        return this;
    }

    public ExpChain withDeleteMethod() {
        expectation.setMethod("DELETE");
        return this;
    }

    public ExpChain withEmptyBody() {
        expectation.setBodyType("EMPTY");
        return this;
    }
    public ExpChain withAnyBody() {
        expectation.setBodyType(null);
        return this;
    }

    public ExpChain withJsonBody() {
        expectation.setBodyType("JSON");
        return this;
    }

    public ExpChain withXmlBody() {
        expectation.setBodyType("XML");
        return this;
    }

    public ExpChain withAnyMethod() {
        expectation.setBodyType(null);
        return this;       
    }

    ExpChain withName(String name) {
        expectation.setName(name);
        return this;   
    }

    ExpChain withProperty(String key, String value) {
        expectation.getAsserts().put(key, value);
        return this;
    }
}
