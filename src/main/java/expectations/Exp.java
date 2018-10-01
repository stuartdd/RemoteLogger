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
public class Exp {
    Expectation expectation = new Expectation();

    public Exp() {
        expectation.setMethod("GET");
        expectation.setBodyType("EMPTY");
        expectation.setForward(null);
        expectation.setPath(null);
        expectation.setResponse(null);
        expectation.setAsserts(new HashMap<>());
    }

    public Expectation getExpectation() {
        return expectation;
    }
    
    public Exp withPath(String path) {
        expectation.setPath(path);
        return this;
    }
    
    public Exp withGetMethod() {
        expectation.setMethod("GET");
        return this;
    }
    
    public Exp withPostMethod() {
        expectation.setMethod("POST");
        return this;
    }
    
    public Exp withPutMethod() {
        expectation.setMethod("PUT");
        return this;
    }
    
    public Exp withPatchMethod() {
        expectation.setMethod("PATCH");
        return this;
    }

    public Exp withDeleteMethod() {
        expectation.setMethod("DELETE");
        return this;
    }

    public Exp withEmptyBody() {
        expectation.setBodyType("EMPTY");
        return this;
    }

    public Exp withJsonBody() {
        expectation.setBodyType("JSON");
        return this;
    }

    public Exp withXmlBody() {
        expectation.setBodyType("JSON");
        return this;
    }
}
