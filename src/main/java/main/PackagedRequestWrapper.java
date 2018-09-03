/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import json.JsonUtils;

/**
 *
 * @author 802996013
 */
public class PackagedRequestWrapper {
    private PackagedRequest packagedRequest;

    public PackagedRequestWrapper(PackagedRequest packagedRequest) {
        this.packagedRequest = packagedRequest;
    }

    public PackagedRequest getPackagedRequest() {
        return packagedRequest;
    }
    
    public String getName() {
        return packagedRequest.getName();
    }

    @Override
    public String toString() {
        return packagedRequest.getName();
    }

    String getJson() {
        return JsonUtils.toJsonFormatted(packagedRequest);
    }
    
    
}
