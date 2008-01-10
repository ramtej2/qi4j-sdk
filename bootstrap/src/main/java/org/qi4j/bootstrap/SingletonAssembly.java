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

package org.qi4j.bootstrap;

import org.qi4j.Qi4j;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.runtime.Energy4Java;
import org.qi4j.runtime.Qi4jRuntime;
import org.qi4j.runtime.structure.ApplicationInstance;
import org.qi4j.runtime.structure.LayerInstance;
import org.qi4j.runtime.structure.ModuleInstance;

/**
 * TODO
 */
public abstract class SingletonAssembly
    implements Assembly
{
    private Qi4j is = new Energy4Java();

    private Qi4jRuntime runtime = (Qi4jRuntime) is;
    private ApplicationAssemblyFactory applicationAssemblyFactory = new ApplicationAssemblyFactory();
    private ApplicationFactory applicationFactory = new ApplicationFactory( runtime, applicationAssemblyFactory );
    private ApplicationInstance applicationInstance;
    private CompositeBuilderFactory compositeBuilderFactory;
    private ObjectBuilderFactory objectBuilderFactory;

    public SingletonAssembly()
        throws IllegalStateException
    {
        try
        {
            applicationInstance = applicationFactory.newApplication( this ).newApplicationInstance( "Simple application" );
        }
        catch( AssemblyException e )
        {
            throw new IllegalStateException( "Could not instantiate application", e );
        }

        ModuleInstance moduleInstance = applicationInstance.getLayerInstances().iterator().next().getModuleInstances().iterator().next();
        compositeBuilderFactory = moduleInstance.getModuleContext().getCompositeBuilderFactory();
        objectBuilderFactory = moduleInstance.getModuleContext().getObjectBuilderFactory();
    }

    public Qi4jRuntime getRuntime()
    {
        return runtime;
    }

    public ApplicationAssemblyFactory getApplicationAssemblyFactory()
    {
        return applicationAssemblyFactory;
    }

    public ApplicationFactory getApplicationFactory()
    {
        return applicationFactory;
    }

    public ApplicationInstance getApplicationInstance()
    {
        return applicationInstance;
    }

    public LayerInstance getLayerInstance()
    {
        return applicationInstance.getLayerInstances().get( 0 );
    }

    public ModuleInstance getModuleInstance()
    {
        return getLayerInstance().getModuleInstances().get( 0 );
    }

    public CompositeBuilderFactory getCompositeBuilderFactory()
    {
        return compositeBuilderFactory;
    }

    public ObjectBuilderFactory getObjectBuilderFactory()
    {
        return objectBuilderFactory;
    }
}