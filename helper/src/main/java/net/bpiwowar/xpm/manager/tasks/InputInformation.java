package net.bpiwowar.xpm.manager.tasks;

import net.bpiwowar.xpm.manager.Constants;
import net.bpiwowar.xpm.utils.introspection.ClassInfo;
import net.bpiwowar.xpm.utils.introspection.FieldInfo;

import java.util.Map;

import static net.bpiwowar.xpm.manager.QName.parse;

/**
 * Information about an input value
 */
public class InputInformation {
    private final TaskInputType type;

    final String help;
    final String copyTo;
    final boolean required;

    public InputInformation(Map<String, String> namespaces, FieldInfo field) {
        JsonArgument jsonArgument = field.getAnnotation(JsonArgument.class);
        this.type = getType(jsonArgument, field.getType(), namespaces);
        help = jsonArgument.help();
        required = jsonArgument.required();
        copyTo = jsonArgument.copyTo();
    }

    private TaskInputType getType(JsonArgument jsonArgument, ClassInfo type, Map<String, String> namespaces) {
        if (type.belongs(java.lang.Integer.class) || type.belongs(Integer.TYPE)
                || type.belongs(java.lang.Long.class) || type.belongs(Long.TYPE)
                || type.belongs(Short.class) || type.belongs(Short.TYPE)) {
            return new TaskInputType.Json(Constants.XP_INTEGER);
        }

        if (type.belongs(java.lang.Double.class) || type.belongs(java.lang.Float.class)) {
            return new TaskInputType.Json(Constants.XP_REAL);
        }

        if (type.belongs(String.class))
            return new TaskInputType.Json(Constants.XP_STRING);

        // Get from class
        final JsonType jsonTypeInfo = type.getAnnotation(JsonType.class);
        if (jsonTypeInfo != null) {
            return new TaskInputType.Json(parse(jsonTypeInfo.type(), namespaces));
        }

        // Check the type
        if (!jsonArgument.type().isEmpty()) {
            return new TaskInputType.Json(parse(jsonArgument.type(), namespaces));
        }

        // Otherwise, just return any
        return new TaskInputType.Json(Constants.XP_ANY);
    }

    public TaskInputType getType() {
        return type;
    }

}