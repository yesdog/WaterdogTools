package protocolsupport.protocol.utils.types.nbt;

import io.netty.buffer.ByteBuf;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

public class Utils {
    public static byte[] readBytes(ByteBuf buf, int length) {
        byte[] result = new byte[length];
        buf.readBytes(result);
        return result;
    }

    public static String toStringAllFields(Object obj) {
        StringJoiner joiner = new StringJoiner(", ");
        Class<?> clazz = obj.getClass();
        do {
            try {
                for (Field field : clazz.getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        setAccessible(field);
                        Object value = field.get(obj);
                        if ((value == null) || !value.getClass().isArray()) {
                            joiner.add(field.getName() + ": " + Objects.toString(value));
                        } else {
                            joiner.add(field.getName() + ": " + Arrays.deepToString(new Object[] {value}));
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Unable to get object fields values", e);
            }
        } while ((clazz = clazz.getSuperclass()) != null);
        return obj.getClass().getName() + "(" + joiner.toString() + ")";
    }

    public static <T extends AccessibleObject> T setAccessible(T object) {
        object.setAccessible(true);
        return object;
    }
}
