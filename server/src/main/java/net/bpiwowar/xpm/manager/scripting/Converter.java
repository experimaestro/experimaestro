package net.bpiwowar.xpm.manager.scripting;

import com.google.gson.JsonElement;
import org.apache.commons.lang.ClassUtils;
import net.bpiwowar.xpm.scheduler.Resource;
import net.bpiwowar.xpm.utils.arrays.ListAdaptator;

import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

/**
 * A converter
 */
public class Converter {

    public static final int NON_MATCHING_COST = 1000;
    static private final Function<Object, Object> IDENTITY = Function.identity();
    static private final Function<Object, Object> TOSTRING = x -> x.toString();

    final Declaration declaration;
    int score = Integer.MAX_VALUE;
    public Function<Arguments, Object>[] functions;

    public <T extends Executable> Converter(Declaration declaration) {
        this.declaration = declaration;
    }

    /**
     * Returns a function that convert the object into a given type
     *
     * @param object   The object to convert
     * @param targetType     The type of the target argument
     * @param nullable Whether the value can be null
     * @return
     */
    Function<Object, Object> converter(Object object, Class<?> targetType, boolean nullable) {
        if (object == null) {
            if (!nullable) {
                return nonMatching();
            }
            if (targetType.isPrimitive()) {
                return nonMatching();
            }
            score--;
            return IDENTITY;
        }

        // Assignable: OK
        targetType = ClassUtils.primitiveToWrapper(targetType);
        if (targetType.isAssignableFrom(object.getClass())) {
            if (object.getClass() != targetType)
                score--;
            return IDENTITY;
        }

        // Arrays
        if (targetType.isArray()) {
            Class<?> innerType = targetType.getComponentType();

            if (object.getClass().isArray())
                object = ListAdaptator.create((Object[]) object);

            if (object instanceof Collection) {
                final Collection array = (Collection) object;
                final Iterator iterator = array.iterator();
                final GenericFunction.ListConverter listConverter = new GenericFunction.ListConverter(innerType);

                while (iterator.hasNext()) {
                    listConverter.add(converter(iterator.next(), innerType, true));
                    if (score == Integer.MIN_VALUE) {
                        return null;
                    }
                }

                return listConverter;

            }
        }

        // Case of string: anything can be converted, but with different
        // scores
        if (targetType == String.class) {
            if (object instanceof CharSequence) {
                score--;
            } else {
                score -= 10;
            }
            return TOSTRING;
        }

        // Cast to integer
        if (targetType == Integer.class && object instanceof Number) {
            if ((((Number) object).intValue()) == ((Number) object).doubleValue()) {
                return input -> ((Number) input).intValue();
            }
        }

        // cast to long
        if (targetType == Long.class && object instanceof Number) {
            if ((((Number) object).longValue()) == ((Number) object).doubleValue()) {
                return input -> ((Number) input).longValue();
            }
        }


//        // JSON inputs
//        if (JsonElement.class.isAssignableFrom(targetType)) {
//            if (object instanceof Map
//                    || object instanceof List || object instanceof Double || object instanceof Float
//                    || object instanceof Integer || object instanceof Long
//                    || object instanceof Path || object instanceof Boolean
//                    || object instanceof Resource || object instanceof ScriptingPath
//                    || object instanceof BigInteger || object instanceof String) {
//                score -= 10;
//                return x -> JsonElement.toJSON(x);
//            }
//
//        }

        return nonMatching();
    }

    private Function nonMatching() {
        score = score > 0 ? 0 : score - NON_MATCHING_COST;
        return null;
    }

    public boolean isOK() {
        return score > 0;
    }

    public Converter invalidate() {
        score = Integer.MIN_VALUE;
        return this;
    }


    /**
     * Transform the arguments
     *
     * @param arguments The method arguments
     * @return The transformed arguments
     */
    Object[] transform(Arguments arguments) {
        Object methodArgs[] = new Object[functions.length];

        for (int i = 0; i < functions.length; ++i) {
            if (functions[i] != null) {
                methodArgs[i] = functions[i].apply(arguments);
            }
        }

        return methodArgs;
    }

    static public class VarArgsConverter implements Function<Arguments, Object> {
        private final Class<?> type;
        ArrayList<Function<Arguments, Object>> list = new ArrayList<>();

        public VarArgsConverter(Class<?> type) {
            this.type = type;
        }

        @Override
        public Object apply(Arguments arguments) {
            final Object array = Array.newInstance(this.type, list.size());
            for (int i = 0; i < list.size(); ++i) {
                if (list.get(i) != null) {
                    Array.set(array, i, list.get(i).apply(arguments));
                }
            }
            return array;
        }

        public void add(Function<Arguments, Object> function) {
            list.add(function);
        }
    }
}
