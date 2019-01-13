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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import json.JsonUtils;

public class MultiModelManager {

    private static Map<Class, MultiModelManager> instances = new ConcurrentHashMap<>();

    private final Class type;
    private boolean updated = false;

    private Map<String, Model> models = new HashMap<>();

    private MultiModelManager(Class type) {
        this.type = type;
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

    public boolean isUpdated() {
        return updated;
    }

    public MultiModelManager replace(String json) {
        return replace((Model) JsonUtils.beanFromJson(type, json));
    }

    public MultiModelManager replace(Model model) {
        Object o = models.replace(model.getName(), model);
        if (o != null) {
            updated = true;
        }
        return this;
    }

    public MultiModelManager remove(Model model) {
        return remove(model.getName());
    }

    public MultiModelManager remove(String name) {
        Object o = models.remove(name);
        if (o != null) {
            updated = true;
        }
        return this;
    }

    public Model get(String name) {
        return models.get(name);
    }

    public String getJson(String name) {
        Model m = models.get(name);
        if (m == null) {
            return null;
        }
        return JsonUtils.toJsonFormatted(m);
    }

    public synchronized MultiModelManager clear() {
        if (models.size() > 0) {
            models.clear();
            updated = true;
        }
        return this;
    }

    public MultiModelManager clearUpdated() {
        updated = false;
        return this;
    }

    public boolean isEmpty() {
        return models.isEmpty();
    }

    public int size() {
        return models.size();
    }

    public Model[] list() {
        Model[] list = new Model[models.size()];
        int i = 0;
        for (String m : models.keySet()) {
            list[i] = models.get(m);
            i++;
        }
        return list;
    }

    public MultiModelManager add(String json) {
        return add((Model) JsonUtils.beanFromJson(type, json));
    }

    public MultiModelManager add(Model model) {
        if (model.getClass() != type) {
            throw new ModelTypeException("Model [" + model.getClass().getName() + "] must be of type [" + type.getName() + "]");
        }
        if (models.containsKey(model.getName())) {
            throw new DuplicateDataException("Date for [" + model.getClass().getSimpleName() + "] has a duplicate id [" + model.getName() + "]");
        }
        models.put(model.getName(), model);
        updated = true;
        return this;
    }


}
