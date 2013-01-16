/*
 * This file is part of experimaestro.
 * Copyright (c) 2012 B. Piwowarski <benjamin@bpiwowar.net>
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

package sf.net.experimaestro.utils;

import org.mozilla.javascript.*;
import org.mozilla.javascript.xml.XMLObject;
import org.mozilla.javascript.xmlimpl.XMLLibImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import sf.net.experimaestro.manager.QName;
import sf.net.experimaestro.utils.log.Logger;

import static java.lang.String.format;

public class JSUtils {
    final static private Logger LOGGER = Logger.getLogger();

    /**
     * Get an object from a scriptable
     *
     * @param object
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Scriptable scope, String name, NativeObject object, boolean allowNull) {
        final Object _value = object.get(name, scope);
        if (_value == UniqueTag.NOT_FOUND)
            if (allowNull) return null;
            else throw new RuntimeException(format("Could not find property '%s'",
                    name));
        return (T) unwrap(_value);
    }

    public static <T> T get(Scriptable scope, String name, NativeObject object) {
        return get(scope, name, object, false);
    }


    /**
     * Unwrap a JavaScript object (if necessary)
     *
     * @param object
     * @return
     */
    public static Object unwrap(Object object) {
        if (object instanceof Wrapper)
            object = ((Wrapper) object).unwrap();

        return object;
    }

    /**
     * Get an object from a scriptable
     *
     * @param object
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Scriptable scope, String name, NativeObject object,
                            T defaultValue) {
        final Object _value = object.get(name, scope);
        if (_value == UniqueTag.NOT_FOUND)
            return defaultValue;
        return (T) unwrap(_value);
    }

    /**
     * Transforms a DOM node to a E4X scriptable object
     *
     * @param node
     * @param cx
     * @param scope
     * @return
     */
    public static Object domToE4X(Node node, Context cx, Scriptable scope) {
        if (node == null) {
            LOGGER.info("XML is null");
            return Context.getUndefinedValue();
        }
        if (node instanceof Document)
            node = ((Document) node).getDocumentElement();


        LOGGER.debug("XML is of type %s [%s]; %s", node.getClass(),
                XMLUtils.toStringObject(node),
                node.getUserData("org.mozilla.javascript.xmlimpl.XmlNode"));
        return cx.newObject(scope, "XML", new Node[]{node});
    }

    /**
     * Transform objects into an XML node
     *
     * @param object
     * @return a {@linkplain Node} or a {@linkplain NodeList}
     */
    public static Object toDOM(Object object) {
        // Unwrap if needed
        if (object instanceof Wrapper)
            object = ((Wrapper) object).unwrap();

        // It is already a DOM node
        if (object instanceof Node)
            return (Node) object;

        if (object instanceof XMLObject) {
            final XMLObject xmlObject = (XMLObject) object;
            String className = xmlObject.getClassName();

            if (className.equals("XMLList")) {
                LOGGER.debug("Transforming from XMLList [%s]", object);
                final Object[] ids = xmlObject.getIds();
                if (ids.length == 1)
                    return toDOM(xmlObject.get((Integer) ids[0], xmlObject));

                NodeList list = new NodeList() {
                    final Node[] nodes = new Node[ids.length];

                    {
                        Document doc = XMLUtils.newDocument();

                        for (int i = 0; i < nodes.length; i++) {
                            nodes[i] = (Node) toDOM(xmlObject.get((Integer) ids[i], xmlObject));
                            doc.adoptNode(nodes[i]);

                        }
                    }

                    @Override
                    public Node item(int index) {
                        return nodes[index];
                    }

                    @Override
                    public int getLength() {
                        return nodes.length;
                    }
                };


                return list;
            }

            if (className.equals("XML")) {
                // FIXME: this strips all whitespaces!
                Node node = XMLLibImpl.toDomNode(object);
                LOGGER.debug("Got node from JavaScript [%s / %s] from [%s]",
                        node.getClass(), XMLUtils.toStringObject(node),
                        object.toString());
                return node;
            }


            throw new RuntimeException(format(
                    "Not implemented: convert %s to XML", className));

        }

        throw new RuntimeException("Class %s cannot be converted to XML");
    }

    /**
     * Returns true if the object is XML
     *
     * @param input
     * @return
     */
    public static boolean isXML(Object input) {
        return input instanceof XMLObject;
    }

    /**
     * Converts a JavaScript object into an XML document
     *
     * @param object
     * @param wrapName If the object is not already a document and has more than one
     *                 element child (or zero), use this to wrap the elements
     * @return
     */
    public static Document toDocument(Object object, QName wrapName) {
        Node dom = (Node) toDOM(object);

        if (dom instanceof Document)
            return (Document) dom;

        Document document = XMLUtils.newDocument();

        // Add a new root element if needed
        NodeList childNodes = dom.getChildNodes();
        int elementCount = 0;
        for (int i = 0; i < childNodes.getLength(); i++)
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE)
                elementCount++;

        Node root = document;
        if (elementCount != 1) {
            root = document.createElementNS(wrapName.getNamespaceURI(),
                    wrapName.getLocalPart());
            document.appendChild(root);
        }

        // Copy back in the DOM
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            node = node.cloneNode(true);
            document.adoptNode(node);
            root.appendChild(node);
        }

        return document;
    }

    /**
     * Add a new javascript function to the scope
     *
     * @param aClass    The class where the function should be searched
     * @param scope     The scope where the function should be defined
     * @param fname     The function name
     * @param prototype The prototype or null. If null, uses the standard Context, Scriptablem Object[], Function prototype
     *                  used by Rhino JS
     * @throws NoSuchMethodException If
     */
    public static void addFunction(Class<?> aClass, Scriptable scope, final String fname,
                                   Class<?>[] prototype) throws NoSuchMethodException {
        final FunctionObject f = new FunctionObject(fname,
                aClass.getMethod("js_" + fname, prototype), scope);
        ScriptableObject.putProperty(scope, fname, f);
    }

    public static String toString(Object object) {
        return Context.toString(unwrap(object));
    }

    public static int getInteger(Object object) {
        return (Integer) unwrap(object);
    }

    public static void addFunction(Scriptable scope, FunctionDefinition definition) throws NoSuchMethodException {
        addFunction(definition.clazz, scope, definition.name, definition.arguments);
    }

    /**
     * Defines a JavaScript function by refering a class, a name and its parameters
     */
    static public class FunctionDefinition {
        Class<?> clazz;
        String name;
        Class<?>[] arguments;

        public FunctionDefinition(Class<?> clazz, String name, Class<?>... arguments) {
            this.clazz = clazz;
            this.name = name;
            if (arguments == null)
                this.arguments = new Class[]{Context.class, Scriptable.class, Object[].class, Function.class};
            else
                this.arguments = arguments;
        }

        public Class<?> getClazz() {
            return clazz;
        }

        public String getName() {
            return name;
        }

        public Class<?>[] getArguments() {
            return arguments;
        }
    }
}
