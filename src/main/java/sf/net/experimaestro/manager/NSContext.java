/*
 *
 *  This file is part of experimaestro.
 *  Copyright (c) 2011 B. Piwowarski <benjamin@bpiwowar.net>
 *
 *  experimaestro is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  experimaestro is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with experimaestro.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package sf.net.experimaestro.manager;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Node;

import sf.net.experimaestro.exceptions.ExperimaestroException;
import sf.net.experimaestro.utils.log.Logger;

public class NSContext implements NamespaceContext {
	final static private Logger LOGGER = Logger.getLogger();

	public NSContext(Node node) {
		// FIXME: would be good to take the map and leave the element
		this.node = node;
	}

	private Node node;

	@Override
	public String getNamespaceURI(String prefix) {
		String uri = node.lookupNamespaceURI(prefix);
		if (uri == null)
			uri = Manager.PREDEFINED_PREFIXES.get(prefix);
		if (uri == null)
			throw new ExperimaestroException("Prefix %s not bound", prefix);
		LOGGER.info("Prefix %s maps to %s", prefix, uri);
		return uri;
	}

	@Override
	public String getPrefix(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<?> getPrefixes(String arg0) {
		throw new UnsupportedOperationException();
	}
}