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

    private PackagedRequests packageRequests;
    private int selectedIndex;

    public PackagedRequestWrapperList(PackagedRequests packageRequests, String currentPackagedRequestName) {
        this.packageRequests = packageRequests;
        if (currentPackagedRequestName == null) {
            selectFirst();
        } else {
            select(currentPackagedRequestName);
        }
    }

    public void select(String currentPackagedRequestName) {
        for (int i = 0; i < packageRequests.size(); i++) {
            if (packageRequests.getPackagedRequests().get(i).getName().equals(currentPackagedRequestName)) {
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
        return ((selectedIndex >= 0) && (selectedIndex < packageRequests.size()));
    }

    public List<PackagedRequestWrapper> getWrappedPackagedRequests() {
        List<PackagedRequestWrapper> list = new ArrayList<>();
        for (PackagedRequest pr:packageRequests.getPackagedRequests()) {
            list.add(new PackagedRequestWrapper(pr));
        }
        return list;
    }

    public PackagedRequestWrapper getSelectedPackagedRequestWrapper() {
        if (isSelected()) {
            return getWrappedPackagedRequests().get(selectedIndex);
        }
        return null;
    }

    public PackagedRequest getSelectedPackagedRequest() {
        if (isSelected()) {
            return getWrappedPackagedRequests().get(selectedIndex).getPackagedRequest();
        }
        return null;
    }

    String getJson() {
        return getSelectedPackagedRequestWrapper().getJson();
    }

    boolean canNotDelete() {
        return packageRequests.size() < 2;
    }

    void check() {
        Map<String, String> names = new HashMap<>();
        for (PackagedRequest pr : packageRequests.getPackagedRequests()) {
            if (names.containsKey(pr.getName())) {
                throw new ConfigDataException("Duplicate name for Packaged Request:" + pr.getName());
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
        for (PackagedRequestWrapper wr : getWrappedPackagedRequests()) {
            sb.append(wr.toString());
        }
        return "selectedIndex=" + selectedIndex + "\n" + sb;
    }

}
