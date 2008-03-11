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

import org.qi4j.spi.structure.ServiceDescriptor;

/**
 * TODO
 */
public class ServiceInstance<T>
{
    private T instance;
    private ServiceDescriptor serviceDescriptor;
    private ServiceInstanceProvider serviceInstanceProvider;

    public ServiceInstance( T instance,
                            ServiceInstanceProvider serviceInstanceProvider,
                            ServiceDescriptor serviceDescriptor )
    {
        this.serviceDescriptor = serviceDescriptor;
        this.instance = instance;
        this.serviceInstanceProvider = serviceInstanceProvider;
    }

    public T getInstance()
    {
        return instance;
    }

    public ServiceDescriptor getServiceDescriptor()
    {
        return serviceDescriptor;
    }

    public ServiceInstanceProvider getServiceInstanceProvider()
    {
        return serviceInstanceProvider;
    }
}
