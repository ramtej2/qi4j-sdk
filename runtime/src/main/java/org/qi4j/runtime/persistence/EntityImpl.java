/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.runtime.persistence;

import java.net.URL;
import org.qi4j.annotation.scope.ThisCompositeAs;
import org.qi4j.persistence.Entity;
import org.qi4j.persistence.EntityComposite;
import org.qi4j.runtime.EntityCompositeInvocationHandler;

public final class EntityImpl
    implements Entity
{
    @ThisCompositeAs EntityComposite meAsEntity;

    public boolean isReference()
    {
        EntityCompositeInvocationHandler handler = EntityCompositeInvocationHandler.getInvocationHandler( meAsEntity );
        return handler.isReference();
    }

    public URL toURL()
    {
        return null;  //toDo()
    }
}
