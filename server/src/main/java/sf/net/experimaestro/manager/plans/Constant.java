/*
 * This file is part of experimaestro.
 * Copyright (c) 2013 B. Piwowarski <benjamin@bpiwowar.net>
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

package sf.net.experimaestro.manager.plans;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import sf.net.experimaestro.annotations.Expose;
import sf.net.experimaestro.annotations.Exposed;
import sf.net.experimaestro.manager.json.Json;

import java.util.*;

/**
 * A constant in a plan just generate json values
 *
 * @author B. Piwowarski <benjamin@bpiwowar.net>
 * @date 21/2/13
 */
@Exposed
public class Constant extends Operator {
    List<Json> values = new ArrayList<>();

    @Expose
    public Constant(Json... values) {
        this(Arrays.asList(values));
    }

    public Constant(Iterable<Json> values) {
        for (Json json : values) {
            this.values.add(json);
        }
    }

    @Override
    public List<Operator> getParents() {
        return ImmutableList.of();
    }

    @Override
    protected Iterator<ReturnValue> _iterator(PlanContext planContext) {
        return Iterators.transform(values.iterator(), input -> new ReturnValue(null, input));
    }


    @Override
    public void addParent(Operator parent) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Operator doCopy(boolean deep, Map<Object, Object> map) {
        return new Constant(values);
    }

    @Override
    @Expose
    protected String getName() {
        return String.format("JSON (#=%d)", values.size());
    }

    public void add(Constant source) {
        values.addAll(source.values);
    }

    public void add(Json document) {
        values.add(document);
    }
}
