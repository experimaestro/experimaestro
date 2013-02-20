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

import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Grouping by
 * @author B. Piwowarski <benjamin@bpiwowar.net>
 * @date 20/2/13
 */
public class GroupBy extends SimpleOperator {
    List<Operator> joins = new ArrayList<>();

    public void add(Operator operator) {
        joins.add(operator);
    }

    @Override
    public List<Plan> plans() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected OperatorIterator _iterator() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected Operator init(HashSet<Operator> processed) throws XPathExpressionException {

        return super.init(processed);
    }

}
