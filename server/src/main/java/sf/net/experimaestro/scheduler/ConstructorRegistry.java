package sf.net.experimaestro.scheduler;

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

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import sf.net.experimaestro.exceptions.XPMRuntimeException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * A registry of constructors
 */
public class ConstructorRegistry<T> {
    private final Class[] parameterTypes;

    Long2ObjectLinkedOpenHashMap<Constructor<? extends T>> map = new Long2ObjectLinkedOpenHashMap<>();

    public ConstructorRegistry(Class[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public ConstructorRegistry<T> add(Class<? extends T>... classes) {
        for (Class<? extends T> aClass : classes) {
            try {
                final TypeIdentifier annotation = aClass.getAnnotation(TypeIdentifier.class);
                if (annotation == null) {
                    throw new RuntimeException("Class " + aClass + " has no TypeIdentifier annotation");
                }
                final long typeValue = DatabaseObjects.getTypeValue(annotation.value());
                map.put(typeValue,
                        aClass.getDeclaredConstructor(parameterTypes));
            } catch (NoSuchMethodException e) {
                throw new XPMRuntimeException(e, "Cannot add class %s to registry", aClass);
            }
        }
        return this;
    }

    public Constructor<? extends T> get(long typeId) {
        return map.get(typeId);
    }

    public T newInstance(long type, Object... objects) {
        try {
            final Constructor<? extends T> constructor = get(type);
            if (constructor.isAccessible()) {
                return constructor.newInstance(objects);
            }
            constructor.setAccessible(true);
            final T t = constructor.newInstance(objects);
            constructor.setAccessible(false);
            return t;

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new XPMRuntimeException("Cannot create object of type %s");
        }
    }
}