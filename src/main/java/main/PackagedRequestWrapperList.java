/*
 * Copyright (C) 2018 stuartdd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PackagedRequestWrapperList {

    private final List<PackagedRequestWrapper> wrappedPackagedRequests;
    private int selectedIndex;

    public PackagedRequestWrapperList(PackagedRequests packageRequests, String currentPackagedRequestName) {
        wrappedPackagedRequests = new ArrayList<>();
        for (PackagedRequest par : packageRequests.getPackagedRequests()) {
            wrappedPackagedRequests.add(new PackagedRequestWrapper(par));
        }
        if (currentPackagedRequestName == null) {
            selectFirst();
        } else {
            select(currentPackagedRequestName);
        }
    }

    public void select(String currentPackagedRequestName) {
        for (int i = 0; i < wrappedPackagedRequests.size(); i++) {
            if (wrappedPackagedRequests.get(i).getName().equals(currentPackagedRequestName)) {
                selectedIndex = i;
                return;
            }
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

    String getJson() {
        return getSelectedPackagedRequestWrapper().getJson();
    }

    boolean canNotDelete() {
        return wrappedPackagedRequests.size() < 2;
    }

    void check() {
        Map<String, String> names = new HashMap<>();
        for (PackagedRequestWrapper pr:wrappedPackagedRequests) {
            if (names.containsKey(pr.getName())) {
                throw new ConfigDataException("Duplicate name for Packaged Request:"+pr.getName());
            }
            names.put(pr.getName(), "");
        }
    }

    public String getSelectedPackagedName() {
        if (getSelectedPackagedRequest() == null) {
            return null;
        }
        return getSelectedPackagedRequest().getName();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (PackagedRequestWrapper wr:wrappedPackagedRequests) {
            sb.append(wr.toString());
        }
        return "selectedIndex=" + selectedIndex + "\n"+sb;
    }


}
