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

import common.Util;
import expectations.*;
import json.JsonUtils;
import main.PackagedRequest;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;

public class ModelTester {

    private static final String BB = clean("{\n"
            + "  \"name\" : \"B\",\n"
            + "  \"id\" : \"B\"\n"
            + "}");
    private static final String BX = clean("{\n"
            + "  \"name\" : \"B\",\n"
            + "  \"id\" : \"X\"\n"
            + "}");

    MultiModelManager mmmExp = MultiModelManager.instance(Expectation.class);
    MultiModelManager mmmPac = MultiModelManager.instance(PackagedRequest.class);
    MultiModelManager mmmMT = MultiModelManager.instance(ModelType.class);

    @Before
    public void before() {
        JsonUtils.beanFromJson(ModelType.class, BB);
    }

    @Test
    public void testManagerInstances() {
        assertNotNull(mmmExp);
        assertNotNull(mmmPac);
        assertEquals(mmmExp, MultiModelManager.instance(Expectation.class));
        assertEquals(mmmPac, MultiModelManager.instance(PackagedRequest.class));
        assertNotSame(mmmExp, mmmPac);
    }

    @Test(expected = ModelTypeException.class)
    public void testManagerGetStringType() {
        MultiModelManager.instance(String.class);
    }

    @Test(expected = ModelTypeException.class)
    public void testManagerAddWrongType() {
        mmmExp.add(model("10", "A"));
    }

    @Ignore
    @Test()
    public void testManagerAddDuplicate() {
        mmmMT.removeAll();
        assertFalse(mmmMT.isUpdated());
        mmmMT.add(model("10", "A"));
        assertTrue(mmmMT.isUpdated());
        try {
            mmmMT.add(model("10", "B"));
            fail("Should throw Exception");
        } catch (DuplicateDataException dde) {
            return;
        }
        fail("Should throw Exception");
    }

    @Test
    public void testManagerRemove() {
        mmmMT.removeAll();
        mmmMT.clearUpdated();

        assertFalse(mmmMT.isUpdated());
        assertEquals(0, mmmMT.size());
        assertTrue(mmmMT.isEmpty());
        mmmMT.add(model("A", "A"));
        assertEquals(1, mmmMT.size());
        assertFalse(mmmMT.isEmpty());
        assertTrue(mmmMT.isUpdated());
        mmmMT.add(BB);
        assertEquals(2, mmmMT.size());
        assertTrue(mmmMT.isUpdated());
        mmmMT.add(model("C", "C"));
        assertEquals(3, mmmMT.size());
        assertTrue(mmmMT.isUpdated());

        mmmMT.clearUpdated();
        assertFalse(mmmMT.isUpdated());
        mmmMT.remove("B");
        assertTrue(mmmMT.isUpdated());

        assertEquals(2, mmmMT.size());
        assertNotNull(mmmMT.get("A"));
        assertNotNull(mmmMT.get("C"));
        assertNull(mmmMT.get("B"));
        assertEquals("A:A|C:C|", list(mmmMT.list()));
        mmmMT.clearUpdated();
        mmmMT.removeAll();
        assertEquals(0, mmmMT.size());
        assertTrue(mmmMT.isEmpty());
        assertTrue(mmmMT.isUpdated());
    }

    @Test
    public void testManagerReplace() {
        mmmMT.removeAll();
        mmmMT.clearUpdated();
        mmmMT.add(model("A", "A"));
        mmmMT.add(BB);
        mmmMT.add(model("C", "C"));
        assertTrue(mmmMT.isUpdated());

        mmmMT.clearUpdated();
        assertEquals("A:A|B:B|C:C|", list(mmmMT.list()));
        mmmMT.replace(model("B", "X"));
        assertTrue(mmmMT.isUpdated());
        assertEquals("A:A|B:X|C:C|", list(mmmMT.list()));

        mmmMT.clearUpdated();
        mmmMT.replace(model("Z", "X"));
        assertFalse(mmmMT.isUpdated());
        assertEquals("A:A|B:X|C:C|", list(mmmMT.list()));
        mmmMT.removeAll();
    }

    @Test
    public void testManagerReplaceJson() {
        mmmMT.removeAll();
        mmmMT.add(model("A", "A"));
        mmmMT.add(model("B", "Y"));
        mmmMT.add(model("C", "C"));
        assertEquals("A:A|B:Y|C:C|", list(mmmMT.list()));
        mmmMT.clearUpdated();
        mmmMT.replace(BB);
        assertTrue(mmmMT.isUpdated());
        assertEquals("A:A|B:B|C:C|", list(mmmMT.list()));
        mmmMT.removeAll();
    }

    @Test
    public void testManagerJson() {
        mmmMT.removeAll();
        mmmMT.add(model("A", "A"));
        mmmMT.add(model("B", "B"));
        mmmMT.add(model("C", "C"));
        assertEquals(BB, clean(mmmMT.getJson("B")));
        mmmMT.replace(BX);
        assertEquals(BX, clean(mmmMT.getJson("B")));
    }

    private String list(Object[] a) {
        StringBuilder sb = new StringBuilder();
        for (Object o : a) {
            sb.append(o == null ? "" : o.toString()).append('|');
        }
        return sb.toString();
    }

    private ModelType model(String name, String id) {
        ModelType mt = new ModelType();
        mt.setName(name);
        mt.setId(id);
        return mt;
    }

    private static String clean(String s) {
        return Util.cleanString(s);
    }

}
