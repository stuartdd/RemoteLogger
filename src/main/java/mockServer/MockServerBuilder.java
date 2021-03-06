/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mockServer;

import expectations.ExpChain;
import expectations.Expectation;
import common.ExpectationException;
import expectations.Expectations;
import json.JsonUtils;
import server.ResponseHandler;

/**
 *
 * @author 802996013
 */
public class MockServerBuilder {

    Expectations expectationList = new Expectations();

    protected MockServerBuilder() {
    }

    protected MockServerBuilder(Expectations expectationList) {
        this.expectationList = expectationList;
    }

    protected MockServerBuilder(Expectation expectation) {
        addExpectation(-1, expectation);
    }

    protected MockServerBuilder(ExpChain expectation) {
        addExpectation(-1, expectation.getExpectation());
    }

    public Expectations getExpectations() {
        return expectationList;
    }

    public MockServerBuilder add(ExpChain exp) {
        addExpectation(0, exp.getExpectation());
        return this;
    }

    public MockServerBuilder add(Expectation exp) {
        addExpectation(0, exp);
        return this;
    }

    public MockServerBuilder add(Expectations expectations) {
        for (Expectation exp : expectations.getExpectations()) {
            addExpectation(0, exp);
        }
        return this;
    }

    public MockServerBuilder add(String json) {
        addExpectation(0, (Expectation) JsonUtils.beanFromJson(Expectation.class, json));
        return this;
    }

    public MockServer start(int port) {
        return (new MockServer(port, null, getExpectations(), true)).start();
    }

    public MockServer start(int port, boolean verbose) {
        return (new MockServer(port, null, getExpectations(), verbose)).start();
    }

    public MockServer start(int port, ResponseHandler handler) {
        return (new MockServer(port, handler, getExpectations(), true)).start();
    }

    public MockServer start(int port, ResponseHandler handler, boolean verbose) {
        return (new MockServer(port, handler, getExpectations(), verbose)).start();
    }

    private void addExpectation(int index, Expectation exp) {
        for (Expectation e : expectationList.getExpectations()) {
            if (e.getName().equals(exp.getName())) {
                throw new ExpectationException("Duplicate Expectation Name [" + exp.getName() + "] found.");
            }
        }
        expectationList.add(index, exp);
    }

}
