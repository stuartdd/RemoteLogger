/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

/**
 *
 * @author 802996013
 */
public class ClientResponse {
    private int status;
    private String body;

    public ClientResponse(int status, String body) {
        this.status = status;
        this.body = body;
    }

    public int getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "ClientResponse{" + "status=" + status + ", body=" + body + '}';
    }
    
}
