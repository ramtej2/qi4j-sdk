/*  Copyright 2008 Edward Yakop.
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
package org.qi4j.osgi.test;

import static org.junit.Assert.assertNotNull;
import org.junit.runner.RunWith;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.logProfile;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.qi4j.api.structure.Module;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
@RunWith( JUnit4TestRunner.class )
public abstract class AbstractTest
{
    private static final String SYMBOLIC_NAME_QI4J_EXAMPLE = "org.qi4j.core.osgi.qi4j-osgi-example";

    @Inject
    private BundleContext bundleContext;

    @Configuration
    public final Option[] configure()
    {
        return options(
            logProfile(),
            // this is how you set the default log level when using pax logging (logProfile)
            systemProperty( "org.ops4j.pax.logging.DefaultServiceLog.level" ).value( "INFO" )
        );
    }

    @Configuration
    public final Option[] baseProvision()
    {
        String qi4jVersion = System.getProperty( "version.qi4j", "0.7-SNAPSHOT" );

        return options( provision(
            "mvn:org.ops4j.pax.logging/pax-logging-api",
            "mvn:org.ops4j.pax.logging/pax-logging-service",
            "mvn:org.qi4j.core/qi4j-core-api/" + qi4jVersion,
            "mvn:org.qi4j.core/qi4j-core-spi/" + qi4jVersion,
            "mvn:org.qi4j.core/qi4j-core-runtime/" + qi4jVersion,
            "mvn:org.ops4j.base/ops4j-base-lang",
            "mvn:org.ops4j.pax.swissbox/pax-swissbox-extender",
            "mvn:org.ops4j.pax.swissbox/pax-swissbox-core",
            "mvn:org.ops4j.pax.swissbox/pax-swissbox-lifecycle",
            "mvn:org.qi4j.core/qi4j-core-bootstrap/" + qi4jVersion,
            "mvn:org.qi4j.core.osgi/qi4j-osgi-example/" + qi4jVersion + "@6"
        ) );
    }

    protected final Bundle getQi4jExampleBundle()
    {
        Bundle exampleBundle = null;
        Bundle[] bundles = bundleContext.getBundles();
        for( Bundle bundle : bundles )
        {
            String symbolicName = bundle.getSymbolicName();
            if( SYMBOLIC_NAME_QI4J_EXAMPLE.equals( symbolicName ) )
            {
                exampleBundle = bundle;
                break;
            }
        }

        assertNotNull( exampleBundle );
        return exampleBundle;
    }

    protected final ServiceReference getModuleServiceRef()
    {
        return bundleContext.getServiceReference( Module.class.getName() );
    }
}
