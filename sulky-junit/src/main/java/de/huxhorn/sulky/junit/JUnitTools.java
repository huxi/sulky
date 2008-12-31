/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2008 Joern Huxhorn
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.huxhorn.sulky.junit;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertEquals;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class JUnitTools
{
    /**
     * Serializes the original and returns the deserialized instance.
     * 
     * @param original the original Serializable,
     * @return the deserialized instance.
     * @throws java.io.IOException In case of error during (de)serialization.
     * @throws ClassNotFoundException In case of error during (de)serialization.
     */
    public static <T extends Serializable> T serialize(T original)
            throws IOException, ClassNotFoundException
    {
        ByteArrayOutputStream os=new ByteArrayOutputStream();
        ObjectOutputStream oos=new ObjectOutputStream(os);
        oos.writeObject(original);
        oos.close();
        ByteArrayInputStream is=new ByteArrayInputStream(os.toByteArray());
        ObjectInputStream ois=new ObjectInputStream(is);
        //noinspection unchecked
        return (T) ois.readObject();
    }


    public static <T extends Serializable> T testSerialization(T original)
            throws IOException, ClassNotFoundException
	{
        return testSerialization(original, false);
    }
    
    public static <T extends Serializable> T testSerialization(T original, boolean same)
            throws IOException, ClassNotFoundException
	{
        T result=serialize(original);

        if(same)
        {
            assertSame(original, result);
        }
        else
        {
            assertEquals("Hashcodes of "+original+" and "+result+" differ!", original.hashCode(), result.hashCode());
            assertEquals(original, result);
        }
        return result;
	}


    public static <T extends Cloneable> T reflectionClone(T original)
           throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        Class<? extends Cloneable> clazz = original.getClass();
        Method method = clazz.getMethod("clone");

        //assertTrue("clone() method isn't accessible!", method.isAccessible());

        //noinspection unchecked
        return (T) method.invoke(original);
    }

    public static <T extends Cloneable> T testClone(T original)
           throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        return testClone(original, false);
    }

    public static <T extends Cloneable> T testClone(T original, boolean same)
           throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        T result=reflectionClone(original);

        if(same)
        {
            assertSame(original, result);
        }
        else
        {
            assertEquals("Hashcodes of "+original+" and "+result+" differ!", original.hashCode(), result.hashCode());
            assertEquals(original, result);
        }

        return result;
    }
        
}
