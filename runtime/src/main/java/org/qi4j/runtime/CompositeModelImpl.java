/*
 * Copyright 2007 Rickard Öberg
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.runtime;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.api.Composite;
import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.api.annotation.ModifiedBy;
import org.qi4j.api.annotation.Uses;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.api.model.InvalidCompositeException;
import org.qi4j.api.model.MixinModel;
import org.qi4j.api.model.ModifierModel;
import org.qi4j.api.model.NullArgumentException;
import org.qi4j.api.persistence.Lifecycle;
import org.qi4j.api.persistence.EntityComposite;
import org.qi4j.api.persistence.Identity;
import org.qi4j.runtime.LifecycleImpl;
import org.qi4j.runtime.persistence.EntityImpl;

/**
 * Composites are descriptors of what an interface represent. <TODO better docs needed here>
 */
public final class CompositeModelImpl<T extends Composite>
    implements CompositeModel<T>
{
    // Attributes ----------------------------------------------------
    private Class<T> compositeClass;
    private List<MixinModel> mixinModels;
    private Map<Class, MixinModel> mixins;
    private List<ModifierModel> modifierModels;
    private Map<Method, List<ModifierModel>> modifiers;
    private Set<Class> unresolvedImplementations;
    private Set<Field> unresolvedUses;
    private Set<Class> allUnresolved;

    // Constructors --------------------------------------------------
    CompositeModelImpl( Class<T> compositeClass )
    {
        if( compositeClass == null )
        {
            throw new NullArgumentException( "compositeClass is null" );
        }
        if( !compositeClass.isInterface() )
        {
            String message = compositeClass.getName() + " is not an interface.";
            throw new InvalidCompositeException( message, compositeClass );
        }
        if( !Composite.class.isAssignableFrom( compositeClass ) )
        {
            String message = compositeClass.getName() + " does not extend from " + Composite.class.getName();
            throw new InvalidCompositeException( message, compositeClass );
        }
        this.compositeClass = compositeClass;
        unresolvedImplementations = new HashSet<Class>();
        mixinModels = new ArrayList<MixinModel>();
        mixinModels.add( new MixinModel<CompositeImpl>( CompositeImpl.class ) );

        if( Identity.class.isAssignableFrom( compositeClass ) )
        {
            mixinModels.add( new MixinModel<IdentityImpl>( IdentityImpl.class ) );
        }
        if( EntityComposite.class.isAssignableFrom( compositeClass )  )
        {
            mixinModels.add( new MixinModel<EntityImpl>( EntityImpl.class ) );
        }
        mixins = new HashMap<Class, MixinModel>();
        modifierModels = new ArrayList<ModifierModel>();
        modifierModels.add( new ModifierModel( CompositeServicesModifier.class ) );
        modifiers = new HashMap<Method, List<ModifierModel>>();

        findImplementations( this.compositeClass );
        findModifiers( this.compositeClass );

        Method[] methods = compositeClass.getMethods();
        for( Method method : methods )
        {
            // Find mixin
            Class<?> mixinType = method.getDeclaringClass();
            if( getMixin( mixinType ) == null )
            {
                findMixin( mixinType );
            }
        }

        for( Method method : methods )
        {
            // Find modifiers for method
            findModifiers( method );
        }

        // Special care needed for the Lifecycle interface, since it is not exposed in the Composite.
        findMixin( Lifecycle.class );
        for( Method method : Lifecycle.class.getMethods() )
        {
            // Find modifiers for method
            findModifiers( method );
        }

        unresolvedUses = new HashSet<Field>();
        findUnresolvedUses( unresolvedUses );

        allUnresolved = new HashSet<Class>();
        for( Field field : unresolvedUses )
        {
            allUnresolved.add( field.getType() );
        }
        allUnresolved.addAll( unresolvedImplementations );
        mixinModels = Collections.unmodifiableList( mixinModels );
        mixins = Collections.unmodifiableMap( mixins );
        modifierModels = Collections.unmodifiableList( modifierModels );
        modifiers = Collections.unmodifiableMap( modifiers );
        unresolvedImplementations = Collections.unmodifiableSet( unresolvedImplementations );
        unresolvedUses = Collections.unmodifiableSet( unresolvedUses );
        allUnresolved = Collections.unmodifiableSet( allUnresolved );
    }

    // Public --------------------------------------------------------
    public Class<T> getCompositeClass()
    {
        return compositeClass;
    }

    public List<MixinModel> getImplementations()
    {
        return mixinModels;
    }

    public List<ModifierModel> getModifiers()
    {
        return modifierModels;
    }

    public boolean isAbstract()
    {
        if( unresolvedImplementations.isEmpty() )
        {
            for( Field field : unresolvedUses )
            {
                Uses uses = field.getAnnotation( Uses.class );
                if( !uses.optional() )
                {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public List<MixinModel> getImplementations( Class aType )
    {
        List<MixinModel> impls = new ArrayList<MixinModel>();

        // Check non-generic impls first
        for( MixinModel implementation : mixinModels )
        {
            if( !implementation.isGeneric() )
            {
                Class fragmentClass = implementation.getFragmentClass();
                if( aType.isAssignableFrom( fragmentClass ) )
                {
                    impls.add( implementation );
                }
            }
        }

        // Check generic impls
        for( MixinModel implementation : mixinModels )
        {
            if( implementation.isGeneric() )
            {
                // Check AppliesTo
                Class appliesTo = implementation.getAppliesTo();
                if( appliesTo == null || appliesTo.isAssignableFrom( aType ) )
                {
                    impls.add( implementation ); // This generic mixin can handle the given type
                }
            }
        }

        return impls;
    }

    public MixinModel getMixin( Class mixinType )
    {
        return mixins.get( mixinType );
    }

    private void findMixin( Class mixinType )
    {
        MixinModel mixinModel = null;

        List<MixinModel> possibleMixinModels = getImplementations( mixinType );

        for( MixinModel possibleMixinModel : possibleMixinModels )
        {
            // Check if this mixinModel is valid
            Class fragmentClass = possibleMixinModel.getFragmentClass();
            if( mixinType.isAssignableFrom( fragmentClass )
                || InvocationHandler.class.isAssignableFrom( fragmentClass ) )
            {
                mixinModel = possibleMixinModel;
                break;
            }
        }

        if( mixinModel == null )
        {
            if( mixinType.equals( Lifecycle.class ) )
            {
                mixinModel = new MixinModel<LifecycleImpl>( LifecycleImpl.class );
            }
        }
        if( mixinModel == null )
        {
            unresolvedImplementations.add( mixinType );
        }
        else
        {
            mixins.put( mixinType, mixinModel );
        }
    }

    public boolean isAssignableFrom( Class anInterface )
    {
        boolean hasMixin = getMixin( anInterface ) != null;
        boolean canAssignComposite = anInterface.isAssignableFrom( compositeClass );
        return hasMixin || canAssignComposite;
    }

    public List<ModifierModel> getModifiers( Method aMethod )
    {
        return modifiers.get( aMethod );
    }

    // Object overrides ---------------------------------------------
    public String toString()
    {
        StringWriter str = new StringWriter();
        PrintWriter out = new PrintWriter( str );
        out.println( compositeClass.getName() );

        out.println( "  implementations available" );
        for( MixinModel implementation : mixinModels )
        {
            out.println( "    " + implementation.getFragmentClass().getName() );
        }

        out.println( "  modifiers available" );
        for( ModifierModel modifierModel : modifierModels )
        {
            out.println( "    " + modifierModel.getFragmentClass().getName() );
        }
        out.println( "  actual" );
        for( Map.Entry<Class, MixinModel> entry : mixins.entrySet() )
        {
            Class interfaceClass = entry.getKey();
            MixinModel mixinModel = entry.getValue();
            out.println( "    " + interfaceClass.getName() );
            out.println( "    implemented by " + mixinModel.getFragmentClass().getName() );
            Method[] methods = interfaceClass.getMethods();
            for( Method method : methods )
            {
                out.println( "      " + method.toGenericString() );
                List<ModifierModel> methodModifierModels = getModifiers( method );
                for( ModifierModel methodModifierModel : methodModifierModels )
                {
                    out.println( "        " + methodModifierModel.getFragmentClass().getName() );
                }
            }
        }
        out.close();
        return str.toString();
    }


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

        CompositeModelImpl composite1 = (CompositeModelImpl) o;

        return compositeClass.equals( composite1.compositeClass );

    }

    public int hashCode()
    {
        return compositeClass.hashCode();
    }

    private void findImplementations( Class aType )
    {
        ImplementedBy impls = (ImplementedBy) aType.getAnnotation( ImplementedBy.class );
        if( impls != null )
        {
            for( Class impl : impls.value() )
            {
                mixinModels.add( new MixinModel( impl ) );
            }
        }

        // Check subinterfaces
        Class[] subTypes = aType.getInterfaces();
        for( Class subType : subTypes )
        {
            findImplementations( subType );
        }
    }

    private void findModifiers( Class aClass )
    {
        ModifiedBy modifiedBy = (ModifiedBy) aClass.getAnnotation( ModifiedBy.class );
        if( modifiedBy != null )
        {
            for( Class<? extends Object> modifier : modifiedBy.value() )
            {
                modifierModels.add( new ModifierModel( modifier ) );
            }
        }

        // Check subinterfaces
        Class[] subTypes = aClass.getInterfaces();
        for( Class subType : subTypes )
        {
            findModifiers( subType );
        }
    }

    public Set<Field> getUnresolvedUses()
    {
        return unresolvedUses;
    }

    public Set<Class> getUnresolvedImplementations()
    {
        return unresolvedImplementations;
    }

    public Set<Class> getUnresolved()
    {
        return allUnresolved;
    }

    private void findUnresolvedUses( Set<Field> result )
    {
        for( ModifierModel model : modifierModels )
        {
            List<Field> fields = model.getUsesFields();
            for( Field field : fields )
            {
                Class type = field.getType();
                if( !type.isAssignableFrom( compositeClass ) )
                {
                    result.add( field );
                }
            }
        }
        for( MixinModel model : mixinModels )
        {
            List<Field> fields = model.getUsesFields();
            for( Field field : fields )
            {
                Class type = field.getType();
                if( !type.isAssignableFrom( compositeClass ) )
                {
                    result.add( field );
                }
            }
        }
    }

    private void findModifiers( Method method )
    {
        List<ModifierModel> modifierClassModels = new ArrayList<ModifierModel>();

        // 1) Interface modifiers
        addModifiers( method, getModifiers(), modifierClassModels );

        // 2) MixinModel modifiers
        Class<?> methodClass = method.getDeclaringClass();
        MixinModel mixinModel = getMixin( methodClass );
        if( mixinModel != null )
        {
            addModifiers( method, mixinModel.getModifiers(), modifierClassModels );
        }

        // 3) Modifiers from other mixins
        for( Map.Entry<Class, MixinModel> mapping : mixins.entrySet() )
        {
            if( !methodClass.equals( mapping.getKey() ) )
            {
                addModifiers( method, mapping.getValue().getModifiers(), modifierClassModels );
            }
        }

        modifiers.put( method, modifierClassModels );
    }

    private void addModifiers( Method method, List<ModifierModel> aModifierList, List<ModifierModel> aMethodModifierList )
    {
        nextmodifier:
        for( ModifierModel modifierModel : aModifierList )
        {
            if( !aMethodModifierList.contains( modifierModel ) )
            {
                // Check AppliesTo
                Class appliesTo = modifierModel.getAppliesTo();
                if( appliesTo != null )
                {
                    // Check AppliesTo
                    if( appliesTo.isAnnotation() )
                    {
                        MixinModel mixinModel = getMixin( method.getDeclaringClass() );


                        if( mixinModel.getFragmentClass().getAnnotation( appliesTo ) == null )
                        {
                            // Check method
                            if( !mixinModel.isGeneric() )
                            {
                                try
                                {
                                    Method implMethod = mixinModel.getFragmentClass().getMethod( method.getName(), method.getParameterTypes() );
                                    if( implMethod.getAnnotation( appliesTo ) == null )
                                    {
                                        continue; // Skip this modifierModel
                                    }
                                }
                                catch( NoSuchMethodException e )
                                {
                                    continue; // Skip this modifierModel
                                }
                            }
                            else
                            {
                                continue; // Skip this modifierModel
                            }
                        }
                    }
                    else
                    {
                        Class<?> methodDeclaringClass = method.getDeclaringClass();
                        MixinModel mixin = getMixin( methodDeclaringClass );
                        if( mixin == null )
                        {
                            throw new InvalidCompositeException( methodDeclaringClass + " has no implementation.", compositeClass );
                        }
                        Class fragmentClass = mixin.getFragmentClass();
                        if( !appliesTo.isAssignableFrom( fragmentClass ) && !appliesTo.isAssignableFrom( methodDeclaringClass ) )
                        {
                            continue; // Skip this modifierModel
                        }
                    }
                }

                // Check interface
                if( !modifierModel.isGeneric() )
                {
                    if( !method.getDeclaringClass().isAssignableFrom( modifierModel.getFragmentClass() ) )
                    {
                        continue; // ModifierModel does not implement interface of this method
                    }
                }
                // ModifierModel is ok!
                aMethodModifierList.add( modifierModel );
            }
        }
    }
}