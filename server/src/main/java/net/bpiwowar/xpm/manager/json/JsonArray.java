package net.bpiwowar.xpm.manager.json;

/*
 * This file is part of experimaestro.
 * Copyright (c) 2014 B. Piwowarski <benjamin@bpiwowar.net>
 *
 * experimaestro is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * experimaestro is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with experimaestro.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.google.common.base.Joiner;
import com.google.gson.stream.JsonWriter;
import net.bpiwowar.xpm.manager.Constants;
import net.bpiwowar.xpm.manager.TypeName;
import net.bpiwowar.xpm.manager.scripting.Expose;
import net.bpiwowar.xpm.manager.scripting.ExposeMode;
import net.bpiwowar.xpm.manager.scripting.Exposed;
import net.bpiwowar.xpm.utils.Output;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import static java.lang.String.format;

/**
 * @author B. Piwowarski <benjamin@bpiwowar.net>
 */
@Exposed
public class JsonArray extends Json implements Iterable<Json> {
    private boolean sealed = false;

    ArrayList<Json> array = new ArrayList<>();


    public JsonArray(Json... elements) {
        array.addAll(Arrays.asList(elements));
    }

    public JsonArray() {
    }

    @Override
    public String toString() {
        return format("[%s]", Output.toString(", ", this));
    }


    @Override
    public void write(JsonWriter out) throws IOException {
        out.beginArray();
        for (Json json : array) {
            json.write(out);
        }
        out.endArray();
    }

    @Override
    public Json copy(boolean full) {
        if (!full && sealed) {
            return this;
        }
        JsonArray newArray = new JsonArray();
        for (Json json : array) {
            newArray.add(json.copy(false));
        }
        return newArray;
    }

    @Override
    public Json seal() {
        if (!sealed) {
            sealed = true;
            this.array.forEach(x -> x.seal());
        }
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JsonArray)) return false;
        ArrayList<Json> objArray = ((JsonArray) obj).array;
        if (objArray.size() != array.size()) return false;
        for(int i = array.size(); --i >= 0; ) {
            if (!array.get(i).equals(objArray.get(i)))
                return false;
        }
        return true;
    }

    @Override
    public Json annotate(String key, Json value) {
        throw new IllegalArgumentException("Cannot annotate a JSON array");
    }


    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public Object get() {
        return this;
    }

    @Override
    public TypeName type() {
        return Constants.XP_ARRAY;
    }

    @Override
    public boolean canIgnore(JsonWriterOptions options) {
        return size() == 0;
    }

    @Override
    public void writeDescriptorString(JsonWriter out, JsonWriterOptions options) throws IOException {
        out.beginArray();
        for (Json json : this) {
            json.writeDescriptorString(out, options);
        }
        out.endArray();
    }

    @Expose
    public String join(String separator) {
        return Joiner.on(separator).join(this.array);
    }

    public void add(Json json) {
        if (sealed) {
            throw new UnsupportedOperationException("JSON array cannot be modified (it is sealed)");
        }
        array.add(json);
    }

    @Expose(value = "length", mode = ExposeMode.PROPERTY)
    public int size() {
        return array.size();
    }

    @Override
    @Expose(mode = ExposeMode.ITERATOR)
    public Iterator<Json> iterator() {
        return array.iterator();
    }

    @Expose(mode = ExposeMode.FIELDS)
    public Json get(int index) {
        return array.get(index);
    }

    @Expose(mode = ExposeMode.LENGTH)
    public int _size() {
        return array.size();
    }
}