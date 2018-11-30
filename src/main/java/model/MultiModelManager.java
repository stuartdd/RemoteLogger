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
package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import json.JsonUtils;

public class MultiModelManager {

    /*
    Many instances of the managers
     */
    private static Map<Class, MultiModelManager> instances = new ConcurrentHashMap<>();

    private ModelProvider models;
    private final Class type;
    private boolean updated = false;
    private Model selectedModel;
    private List<SelectedModelChangeListener> changeListeners = new ArrayList<>();

    private MultiModelManager(Class type) {
        this.type = type;
    }

    public boolean isUpdated() {
        return updated;
    }

    public boolean isEmpty() {
        return models.size() == 0;
    }

    public Model[] list() {
        if (isEmpty()) {
            return new Model[0];
        }
        Model[] mod = new Model[models.size()];
        for (int i = 0; i < mod.length; i++) {
            mod[i] = models.getModel(i);
        }
        return mod;
    }

    public boolean replace(String json) {
        return models.replaceModel(json);
    }

    public boolean remove(Model model) {
        return remove(model.getName());
    }

    public boolean remove(String name) {
        return models.deleteModel(name);
    }

    public Model get(String name) {
        return models.getModel(name);
    }

    public String getJson(String name) {
        Model m = get(name);
        if (m == null) {
            return null;
        }
        return JsonUtils.toJsonFormatted(m);
    }

    public MultiModelManager clearUpdated() {
        updated = false;
        return this;
    }

    public int size() {
        return models.size();
    }

    public MultiModelManager add(String json) {
        return add((Model) JsonUtils.beanFromJson(type, json));
    }

    public MultiModelManager add(Model model) {
        if (models.getModel(model.getName()) != null) {
            throw new DuplicateDataException("Data for item with name[" + model.getName() + "] already exists");
        }
        models.addModel(model);
        updated = true;
        return this;
    }

    public Model getSelectedModel() {
        return selectedModel;
    }

    public void setSelectedModel(Model selectedModel) {
        Model m = get(selectedModel.getName());
        if (m == null) {
            return;
        }
        changeSelectedModel(m);
    }

    public void setSelectedModel(String selectedModelName) {
        setSelectedModel(get(selectedModelName));
    }

    public static MultiModelManager instance(Class type) {
        try {
            type.getMethod("getName");
        } catch (NoSuchMethodException ex) {
            throw new ModelTypeException("Type perameter [" + type.getName() + "] must be of type [" + type.getName() + "]");
        }
        MultiModelManager mmm = instances.get(type);
        if (mmm == null) {
            mmm = new MultiModelManager(type);
            instances.put(type, mmm);
        }
        return mmm;
    }

    public void addChangeListener(SelectedModelChangeListener changeListener) {
        changeListeners.add(changeListener);
    }

    private void changeSelectedModel(Model m) {
        if (selectedModel != m) {
            selectedModel = m;
            notifySelectedModelChange(selectedModel);
        }
    }

    private void notifySelectedModelChange(Model selectedModel) {
        for (SelectedModelChangeListener cl : changeListeners) {
            if (cl != null) {
                cl.notify(selectedModel);
            }
        }
    }

}
