/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.api.common;

import java.lang.reflect.Method;
import java.io.Serializable;

/**
 * A QualifiedName is created by combining the name of a method and the
 * name of the type that declares the method.
 */
public class QualifiedName
    implements Comparable<QualifiedName>, Serializable
{
    private final TypeName typeName;
    private final String name;

    public static QualifiedName fromMethod(Method method)
    {
        if (method==null) throw new IllegalArgumentException( "method must not be null");
        return fromClass(method.getDeclaringClass(),method.getName());
    }

    public static QualifiedName fromClass(Class type, String name)
    {
        return new QualifiedName(TypeName.nameOf(type), name);
    }

    public static QualifiedName fromName(String type, String name) {
        return new QualifiedName(TypeName.nameOf(type),name);        
    }
    public QualifiedName( TypeName typeName, String name )
    {
        if (typeName ==null) throw new IllegalArgumentException( "TypeName must not be null");
        if (name==null || name.trim().length()==0) throw new IllegalArgumentException( "Name must not be null or empty");
        this.typeName = typeName;
        this.name = name;
    }

    public static QualifiedName fromQN(String qualifiedName) {
        if (qualifiedName == null || qualifiedName.trim().length() == 0 ) throw new IllegalArgumentException( "qualifiedName must not be null or empty");
        int idx = qualifiedName.lastIndexOf( ":" );
        if (idx == -1)
        {
            throw new IllegalArgumentException("Name '"+qualifiedName+"' is not a qualified name");
        }
        final String type = qualifiedName.substring(0, idx);
        final String name = qualifiedName.substring(idx + 1);
        return new QualifiedName(TypeName.nameOf(type), name);
    }

    public String type()
    {
        return typeName.normalized();
    }

    public TypeName typeName()
    {
        return typeName;
    }
    
    public String name()
    {
        return name;
    }

    public String toURI()
    {
        return toNamespace() + name;
    }

    public String toNamespace()
    {
        return typeName.toURI() + "#";
    }

    @Override public String toString()
    {
        return typeName.normalized() +":"+name;
    }

    @Override
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        QualifiedName that = (QualifiedName) o;

        return name.equals( that.name ) && typeName.equals( that.typeName);

    }

    @Override
    public int hashCode()
    {
        return 31 * typeName.hashCode() + name.hashCode();
    }

    public int compareTo( QualifiedName o )
    {
        return toString().compareTo( o.toString() );
    }
}
