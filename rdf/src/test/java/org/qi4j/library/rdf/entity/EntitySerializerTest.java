/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.library.rdf.entity;

import java.io.PrintWriter;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.library.rdf.DcRdf;
import org.qi4j.library.rdf.Rdfs;
import org.qi4j.library.rdf.serializer.RdfXmlSerializer;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.typeregistry.EntityTypeRegistryService;
import org.qi4j.test.AbstractQi4jTest;

/**
 * JAVADOC
 */
public class EntitySerializerTest
    extends AbstractQi4jTest
{
    @Service EntityStore entityStore;
    @Uses EntityStateSerializer serializer;

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addServices( MemoryEntityStoreService.class, EntityTypeRegistryService.class );
        module.addEntities( TestEntity.class );
        module.addValues( TestValue.class );
        module.addObjects( EntityStateSerializer.class, EntitySerializerTest.class );
    }

    @Override @Before
    public void setUp() throws Exception
    {
        super.setUp();

        createDummyData();

        objectBuilderFactory.newObjectBuilder( EntitySerializerTest.class ).injectTo( this );
    }

    @Test
    public void testEntitySerializer() throws RDFHandlerException
    {
        EntityReference entityReference = new EntityReference( "test2" );
        EntityState entityState = entityStore.newUnitOfWork( UsecaseBuilder.newUsecase("Test" ), new MetaInfo()).getEntityState(entityReference);

        Iterable<Statement> graph = serializer.serialize( entityState );

        String[] prefixes = new String[]{ "rdf", "dc", " vc" };
        String[] namespaces = new String[]{ Rdfs.RDF, DcRdf.NAMESPACE, "http://www.w3.org/2001/vcard-rdf/3.0#" };


        new RdfXmlSerializer().serialize( graph, new PrintWriter( System.out ), prefixes, namespaces );
    }

    void createDummyData() throws UnitOfWorkCompletionException
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            ValueBuilder<TestValue> valueBuilder = valueBuilderFactory.newValueBuilder( TestValue.class );
            valueBuilder.prototype().test1().set( 4L );
            TestValue testValue = valueBuilder.newInstance();

            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder(TestEntity.class, "test1");
            TestEntity rickardTemplate = builder.prototype();
            rickardTemplate.name().set( "Rickard" );
            rickardTemplate.title().set( "Mr" );
            rickardTemplate.value().set( testValue );
            TestEntity testEntity = builder.newInstance();

            EntityBuilder<TestEntity> builder2 = unitOfWork.newEntityBuilder(TestEntity.class, "test2");
            TestEntity niclasTemplate = builder2.prototype();
            niclasTemplate.name().set( "Niclas" );
            niclasTemplate.title().set( "Mr" );
            niclasTemplate.association().set( testEntity );
            niclasTemplate.manyAssoc().add( 0, testEntity );
            niclasTemplate.group().add( 0, testEntity );
            niclasTemplate.group().add( 0, testEntity );
            niclasTemplate.group().add( 0, testEntity );
            valueBuilder = testValue.buildWith();
            valueBuilder.prototype().test1().set( 5L );
            testValue = valueBuilder.newInstance();
            niclasTemplate.value().set( testValue );
            TestEntity testEntity2 = builder2.newInstance();
            unitOfWork.complete();
        }
        catch( RuntimeException e )
        {
            unitOfWork.discard();
            throw e;
        }
        catch( UnitOfWorkCompletionException e )
        {
            unitOfWork.discard();
            throw e;
        }

    }
}


