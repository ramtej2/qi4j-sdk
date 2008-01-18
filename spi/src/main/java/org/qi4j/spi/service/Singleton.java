/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.spi.service;

import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.scope.Structure;
import org.qi4j.service.ServiceComposite;

/**
 * TODO
 */
public final class Singleton
    implements ServiceProvider
{
    private @Structure CompositeBuilderFactory cbf;

    private Object instance;

    public synchronized <T> T getService( Class<T> serviceType ) throws ServiceProviderException
    {
        if( instance == null )
        {
            instance = cbf.newComposite( serviceType );
        }
        return serviceType.cast( instance );
    }

    public void releaseService( ServiceComposite service )
    {
        // Ignore for now
    }
}
