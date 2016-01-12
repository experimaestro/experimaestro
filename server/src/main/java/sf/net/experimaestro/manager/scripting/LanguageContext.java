package sf.net.experimaestro.manager.scripting;

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

import sf.net.experimaestro.manager.QName;
import sf.net.experimaestro.manager.json.Json;

import javax.xml.namespace.NamespaceContext;
import java.nio.file.Path;

/**
 * Language specific context when executing a script
 */
abstract public class LanguageContext {
    /**
     * Returns a JSON object from an objct
     */
    abstract public Json toJSON(Object object);

    /**
     * Get the context
     */
    public abstract NamespaceContext getNamespaceContext();

    /**
     * Create an exception for the specific language
     * @param e The wrapped exception
     * @param format The message
     * @param objects The formatting objects
     * @return A runtime exception
     */
    public abstract RuntimeException runtimeException(Exception e, String format, Object... objects);

    /**
     * Returns a QName from an object
     * @param value
     * @return
     */
    public abstract QName qname(Object value);

    /**
     * Wrap values so that they can be accessed as a java object.
     *
     * This is usefull for collections
     * @param value The value to wrap
     * @return The wrapped value
     */
    public abstract Object toJava(Object value);

    abstract public ScriptLocation getScriptLocation();
}
