package me.siebe.flux.util.string;

import java.lang.reflect.Field;

public class StringUtils {
    public static String toString(final Object obj, boolean expanded) {
        if (obj == null) return "null";

        Class<?> clazz = obj.getClass();
        StringBuilder sb = new StringBuilder();
        sb.append(clazz.getSimpleName()).append(" { ");

        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];

            if (field.isAnnotationPresent(ToStringIgnore.class)) continue;
            if (!shouldShowField(obj, field)) continue;

            field.setAccessible(true);
            if (expanded) {
                sb.append("\n\t");
            }

            try {
                Object value = field.get(obj);
                sb.append(field.getName()).append(": ").append(value);
            } catch (IllegalAccessException e) {
                sb.append(field.getName()).append(": <inaccessible>");
            }
            if (i < fields.length - 1) {
                sb.append(", ");
            } else {
                sb.append(" ");
            }
        }

        if (expanded) {
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    public static String toString(Object obj) {
        return toString(obj, false);
    }


    private static boolean shouldShowField(Object obj, Field field) {
        if (field.getName().equals("serialVersionUID")) return false;
        return true;
    }
}
