package org.qi4j.runtime.resolution;

import org.qi4j.api.model.DependencyKey;
import org.qi4j.api.model.FragmentDependencyKey;
import org.qi4j.spi.dependency.DependencyInjectionContext;
import org.qi4j.spi.dependency.DependencyResolution;
import org.qi4j.spi.dependency.DependencyResolver;
import org.qi4j.spi.dependency.FragmentDependencyInjectionContext;
import org.qi4j.spi.dependency.InvalidDependencyException;

/**
 * TODO
 */
public class ThisAsDependencyResolver
    implements DependencyResolver
{
    public DependencyResolution resolveDependency( DependencyKey key )
        throws InvalidDependencyException
    {
        if( key instanceof FragmentDependencyKey )
        {
            FragmentDependencyKey fragmentKey = (FragmentDependencyKey) key;
            // Check if the composite implements the desired type
            if( key.getRawType().isAssignableFrom( fragmentKey.getCompositeType() ) )
            {
                return new ThisAsDependencyResolution();
            }
            else
            {
                throw new InvalidDependencyException( "Composite " + fragmentKey.getCompositeType() + " does not implement @ThisAs type " + key.getDependencyType() + " in fragment " + key.getDependentType() );
            }
        }
        else
        {
            throw new InvalidDependencyException( "Object " + key.getDependentType() + " may not se @ThisAs" );
        }
    }

    private class ThisAsDependencyResolution implements DependencyResolution
    {
        public Object getDependencyInjection( DependencyInjectionContext context )
        {
            FragmentDependencyInjectionContext fragmentContext = (FragmentDependencyInjectionContext) context;
            return fragmentContext.getThisAs();
        }
    }
}
