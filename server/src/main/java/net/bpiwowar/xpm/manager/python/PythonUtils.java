package net.bpiwowar.xpm.manager.python;

import net.bpiwowar.xpm.manager.Constants;
import net.bpiwowar.xpm.manager.TypeName;
import net.bpiwowar.xpm.manager.json.*;
import net.bpiwowar.xpm.manager.scripting.ClassDescription;
import net.bpiwowar.xpm.manager.scripting.ExposeMode;
import net.bpiwowar.xpm.manager.scripting.MethodFunction;
import net.bpiwowar.xpm.manager.scripting.PropertyAccess;
import net.bpiwowar.xpm.manager.scripting.ScriptContext;
import net.bpiwowar.xpm.manager.scripting.ScriptingPath;
import net.bpiwowar.xpm.scheduler.Resource;
import net.bpiwowar.xpm.utils.JSUtils;
import org.python.core.PyObject;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class PythonUtils {
    public static Json toJSON(Object value) {
        if (value instanceof net.bpiwowar.xpm.manager.scripting.Wrapper) {
            value = ((net.bpiwowar.xpm.manager.scripting.Wrapper) value).unwrap();
        }

        if (value instanceof PythonObject) {
            value = ((PythonObject)value).object;
        }

        if (value instanceof Json)
            return (Json) value;

        // --- Simple cases
        if (value == null)
            return JsonNull.getSingleton();

        if (value instanceof Json)
            return (Json) value;

        if (value instanceof String)
            return new JsonString((String) value);

        if (value instanceof Double) {
            if ((double) ((Double) value).longValue() == (double) value)
                return new JsonInteger(((Double) value).longValue());
            return new JsonReal((Double) value);
        }
        if (value instanceof Float) {
            if ((double) ((Float) value).longValue() == (float) value)
                return new JsonInteger(((Float) value).longValue());
            return new JsonReal((Float) value);
        }

        if (value instanceof Integer)
            return new JsonInteger((Integer) value);

        if (value instanceof Long)
            return new JsonInteger((Long) value);

        if (value instanceof Boolean)
            return JsonBoolean.of((Boolean) value);

        // --- A map
        if (value instanceof Map) {
            JsonObject json = new JsonObject();
            PythonNamespaceContext nsContext = new PythonNamespaceContext();
            for (Object o : ((Map) value).entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                TypeName qname = TypeName.parse(toString(entry.getKey()), nsContext);
                Object pValue = entry.getValue();

                if (qname.equals(Constants.XP_TYPE))
                    pValue = TypeName.parse(JSUtils.toString(pValue), nsContext).toString();

                String key = qname.toString();
                final Json key_value = toJSON(pValue);
                json.put(key, key_value);
            }
            return json;
        }

        // -- An array
        if (value instanceof List) {
            List array = (List) value;
            JsonArray json = new JsonArray();
            for (int i = 0; i < array.size(); ++i)
                json.add(toJSON(array.get(i)));
            return json;
        }

        if (value.getClass().isArray()) {
            final int length = Array.getLength(value);
            JsonArray json = new JsonArray();
            for (int i = 0; i < length; i++)
                json.add(toJSON(Array.get(value, i)));
            return json;
        }

        if (value instanceof java.nio.file.Path) {
            return new JsonPath((java.nio.file.Path) value);
        }

        if (value instanceof ScriptingPath)
            return new JsonPath(((ScriptingPath) value).getObject());

        if (value instanceof Resource)
            return new JsonResource((Resource) value);

        ScriptContext.get().getLogger("PythonUtils").warn("Transformed %s into a JSON string", value.getClass());

        return new JsonString(value.toString());
    }

    private static String toString(Object key) {
        return key.toString();
    }

    /**
     * Wrap an iterator as Python Object
     * @param iterator
     * @return A Python object
     */
    public static PyObject wrapIterator(final Iterator iterator) {
        return new PyObject() {

            @Override
            public PyObject __iternext__() {
                if (iterator.hasNext()) {
                    return PythonRunner.wrap(iterator.next());
                }
                return null;
            }
        };
    }

    static public PyObject getAttribute(String name, Object object, ClassDescription description) {
        MethodFunction function = description.getMethod(name);
        if (function != null) {
            return new PythonMethod(object, function);
        }

        // Search for a property
        final PropertyAccess propertyAccess = description.getFields().get(name);
        if (propertyAccess != null) {
            return PythonRunner.wrap(propertyAccess.get(object).get(null));
        }

        // Search for property accessor
        function = description.getMethod(ExposeMode.FIELDS);
        if (function != null && !function.isEmpty()) {
            final PythonContext pcx = new PythonContext();
            return PythonRunner.wrap(function.call(pcx, object, null, name));
        }


        return null;
    }
}