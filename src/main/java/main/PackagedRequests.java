/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.ArrayList;
import java.util.List;
import json.JsonUtils;

/**
 *
 * @author stuart
 */
public class PackagedRequests {
    private List<PackagedRequest> packagedRequests = new ArrayList<>();
    private String[] paths;
    private boolean verbose;

    public List<PackagedRequest> getPackagedRequests() {
        return packagedRequests;
    }

    public void setPackagedRequests(List<PackagedRequest> packagedRequests) {
        this.packagedRequests = packagedRequests;
    }

    public String[] getPaths() {
        return paths;
    }

    public void setPaths(String[] paths) {
        this.paths = paths;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public String tojSON() {
        return JsonUtils.toJsonFormatted(this);
    }

    public boolean canNotDelete() {
        return packagedRequests.size() < 2;
    }

    public int size() {
        return packagedRequests.size();
    }
    
    
    
}
