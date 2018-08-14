/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

/**
 *
 * @author stuart
 */
public class ServerStatistics {

    int requestCount = 0;
    int responseCount = 0;
    int notMatchedCount = 0;
    int notFoundCount = 0;
    int matchedCount = 0;

    public void incRequestCount() {
        requestCount++;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void incResponseCount() {
        responseCount++;
    }

    public int getResponseCount() {
        return responseCount;
    }

    public void incNotMatchedCount() {
        notMatchedCount++;
    }

    public int getNotMatchedCount() {
        return notMatchedCount;
    }
    
    public void incMatchedCount() {
        matchedCount++;
    }

    public int getMatchedCount() {
        return matchedCount;
    }

    public void incNotFoundCount() {
        notFoundCount++;
    }

    public int getNotFoundCount() {
        return notFoundCount;
    }

    @Override
    public String toString() {
        return "ServerStatistics{" + "requestCount=" + requestCount + ", responseCount=" + responseCount + ", notMatchedCount=" + notMatchedCount + ", notFoundCount=" + notFoundCount + ", matchedCount=" + matchedCount + '}';
    }

}
