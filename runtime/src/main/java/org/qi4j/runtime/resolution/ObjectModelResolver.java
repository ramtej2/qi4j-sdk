package org.qi4j.runtime.resolution;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.model.ConstructorDependency;
import org.qi4j.model.DependencyKey;
import org.qi4j.model.FieldDependency;
import org.qi4j.model.MethodDependency;
import org.qi4j.model.ObjectModel;
import org.qi4j.model.ParameterDependency;
import org.qi4j.spi.dependency.DependencyResolution;
import org.qi4j.spi.dependency.DependencyResolver;
import org.qi4j.spi.dependency.InvalidDependencyException;

/**
 * TODO
 */
public class ObjectModelResolver<K extends ObjectResolution>
{
    private DependencyResolver dependencyResolver;

    public ObjectModelResolver( DependencyResolver dependencyResolver )
    {
        this.dependencyResolver = dependencyResolver;
    }

    public <T extends ObjectModel> K resolveModel( ObjectModel<T> objectModel )
        throws InvalidDependencyException
    {
        List<ConstructorDependencyResolution> constructorDependencies = new ArrayList<ConstructorDependencyResolution>();
        resolveConstructorDependencies( objectModel.getConstructorDependencies(), constructorDependencies );
        List<FieldDependencyResolution> fieldDependencies = new ArrayList<FieldDependencyResolution>();
        resolveFieldDependencies( objectModel.getFieldDependencies(), fieldDependencies );
        List<MethodDependencyResolution> methodDependencies = new ArrayList<MethodDependencyResolution>();
        resolveMethodDependencies( objectModel.getMethodDependencies(), methodDependencies );

        ObjectResolution model = new ObjectResolution<T>( objectModel, constructorDependencies, fieldDependencies, methodDependencies );
        return (K) model;
    }

    protected void resolveConstructorDependencies( Iterable<ConstructorDependency> dependencies, List<ConstructorDependencyResolution> dependentConstructors )
        throws InvalidDependencyException
    {
        for( ConstructorDependency dependency : dependencies )
        {

            List<ParameterDependencyResolution> parameterResolutions = new ArrayList<ParameterDependencyResolution>();
            Iterable<ParameterDependency> parameterDependencies = dependency.getParameterDependencies();
            resolveParameterDependencies( parameterDependencies, parameterResolutions );
            ConstructorDependencyResolution constructorDependencyResolution = new ConstructorDependencyResolution( dependency, parameterResolutions );

            dependentConstructors.add( constructorDependencyResolution );
        }
    }

    protected void resolveMethodDependencies( Iterable<MethodDependency> dependencies, List<MethodDependencyResolution> dependentMethods )
        throws InvalidDependencyException
    {
        for( MethodDependency dependency : dependencies )
        {
            List<ParameterDependencyResolution> parameterResolutions = new ArrayList<ParameterDependencyResolution>();
            Iterable<ParameterDependency> parameterDependencies = dependency.getParameterDependencies();
            resolveParameterDependencies( parameterDependencies, parameterResolutions );
            MethodDependencyResolution constructorDependencyResolution = new MethodDependencyResolution( dependency, parameterResolutions );

            dependentMethods.add( constructorDependencyResolution );
        }
    }

    protected void resolveFieldDependencies( Iterable<FieldDependency> dependencies, List<FieldDependencyResolution> dependentFields )
        throws InvalidDependencyException
    {
        for( FieldDependency dependency : dependencies )
        {
            DependencyKey key = dependency.getKey();
            DependencyResolution resolution = dependencyResolver.resolveDependency( key );

            if( resolution == null )
            {
                // Check if this is optional
                if( dependency.isOptional() )
                {
                    resolution = new NullResolution();
                }
                else
                {
                    throw new InvalidDependencyException( "Non-optional field dependency " + dependency.getField().getName() + " for type " + dependency.getKey().getDependencyType().getName() + " in fragment " + dependency.getKey().getDependentType().getName() + " could not be resolved" );
                }
            }

            FieldDependencyResolution dependencyResolution = new FieldDependencyResolution( dependency, resolution );
            dependentFields.add( dependencyResolution );
        }
    }

    private void resolveParameterDependencies( Iterable<ParameterDependency> parameterDependencies, List<ParameterDependencyResolution> parameterResolutions )
        throws InvalidDependencyException
    {
        for( ParameterDependency parameterDependency : parameterDependencies )
        {
            DependencyResolution resolution = dependencyResolver.resolveDependency( parameterDependency.getKey() );
            if( resolution == null )
            {
                // Check if this is optional
                if( parameterDependency.isOptional() )
                {
                    resolution = new NullResolution();
                }
                else
                {
                    throw new InvalidDependencyException( "Non-optional parameter dependency " + parameterDependency.getKey() + " in fragment " + parameterDependency.getKey().getDependentType().getName() + " could not be resolved" );
                }
            }

            ParameterDependencyResolution parameterResolution = new ParameterDependencyResolution( parameterDependency, resolution );
            parameterResolutions.add( parameterResolution );
        }
    }
}