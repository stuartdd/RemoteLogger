/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author 802996013
 */
public class PackagedRequestWrapperList {

    private final List<PackagedRequestWrapper> wrappedPackagedRequests;
    private int selectedIndex;

    public PackagedRequestWrapperList(List<PackagedRequest> packageRequests) {
        wrappedPackagedRequests = new ArrayList<>();
        for (PackagedRequest par : packageRequests) {
            wrappedPackagedRequests.add(new PackagedRequestWrapper(par));
        }
        selectFirst();
    }

    public void selectFirst() {
        selectedIndex = 0;
    }
    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }
    
    public boolean isSelected() {
        return ((selectedIndex >= 0) && (selectedIndex < wrappedPackagedRequests.size()));
    }

    public List<PackagedRequestWrapper> getWrappedPackagedRequests() {
        return wrappedPackagedRequests;
    }
    
   public PackagedRequestWrapper getSelectedPackagedRequestWrapper() {
        if (isSelected()) {
            return wrappedPackagedRequests.get(selectedIndex);
        }
        return null;
    }
   
    public PackagedRequest getSelectedPackagedRequest() {
        if (isSelected()) {
            return wrappedPackagedRequests.get(selectedIndex).getPackagedRequest();
        }
        return null;
    }
    
    private int indexOfPackagedRequestWrapper(PackagedRequestWrapper packagedRequestWrapper) {
        return indexOfPackagedRequest(packagedRequestWrapper.getPackagedRequest());
    }

    private int indexOfPackagedRequest(PackagedRequest packagedRequest) {
        for (int i = 0; i < wrappedPackagedRequests.size(); i++) {
            if (packagedRequest.getName().equals(wrappedPackagedRequests.get(i).getName())) {
                return i;
            }
        }
        return -1;
    }
    
    String getJson() {
        return getSelectedPackagedRequestWrapper().getJson();
    }

}
