/*
 * Copyright (c) 2010-2012, Stanislav Muhametsin. All Rights Reserved.
 * Copyright (c) 2012-2014, Paul Merlin. All Rights Reserved.
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
package org.qi4j.test.performance.entitystore.sql;

import java.sql.Connection;
import java.sql.Statement;
import org.junit.Ignore;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.ApplicationAssemblerAdapter;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.entitystore.sql.assembly.PostgreSQLEntityStoreAssembler;
import org.qi4j.entitystore.sql.internal.SQLs;
import org.qi4j.library.sql.assembly.DataSourceAssembler;
import org.qi4j.library.sql.common.SQLConfiguration;
import org.qi4j.library.sql.common.SQLUtil;
import org.qi4j.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.qi4j.test.performance.entitystore.AbstractEntityStorePerformanceTest;

/**
 * Performance test for PostgreSQLEntityStore.
 * <p/>
 * WARN This test is deactivated on purpose, please do not commit it activated.
 * <p/>
 * To run it see PostgreSQLEntityStoreTest.
 */
@Ignore( "WARN Tearing down this test is broken!" )
public class PostgreSQLEntityStorePerformanceTest
    extends AbstractEntityStorePerformanceTest
{

    public PostgreSQLEntityStorePerformanceTest()
    {
        super( "PostgreSQLEntityStore", createAssembler() );
    }

    private static Assembler createAssembler()
    {
        return new Assembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                ModuleAssembly config = module.layer().module( "config" );
                config.services( MemoryEntityStoreService.class );

                // DataSourceService
                new DBCPDataSourceServiceAssembler().
                    identifiedBy( "postgresql-datasource-service" ).
                    visibleIn( Visibility.module ).
                    withConfig( config ).
                    withConfigVisibility( Visibility.layer ).
                    assemble( module );

                // DataSource
                new DataSourceAssembler().
                    withDataSourceServiceIdentity( "postgresql-datasource-service" ).
                    identifiedBy( "postgresql-datasource" ).
                    withCircuitBreaker().
                    assemble( module );

                // SQL EntityStore
                new PostgreSQLEntityStoreAssembler().
                    withConfig( config ).
                    withConfigVisibility( Visibility.layer ).
                    assemble( module );
            }

        };
    }

    @Override
    protected void cleanUp()
        throws Exception
    {
        try
        {
            super.cleanUp();
        }
        finally
        {

            Energy4Java qi4j = new Energy4Java();
            Assembler[][][] assemblers = new Assembler[][][]
            {
                {
                    {
                        createAssembler()
                    }
                }
            };
            Application application = qi4j.newApplication( new ApplicationAssemblerAdapter( assemblers )
            {
            } );
            application.activate();

            Module moduleInstance = application.findModule( "Layer 1", "config" );
            UnitOfWorkFactory uowf = moduleInstance;
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                SQLConfiguration config = uow.get( SQLConfiguration.class,
                                                   PostgreSQLEntityStoreAssembler.DEFAULT_ENTITYSTORE_IDENTITY );
                // TODO fix AbstractEntityStorePerformanceTest to extend from AbstractQi4jTest
                Connection connection = null; // SQLUtil.getConnection( this.serviceLocator );
                String schemaName = config.schemaName().get();
                if( schemaName == null )
                {
                    schemaName = SQLs.DEFAULT_SCHEMA_NAME;
                }

                Statement stmt = null;
                try
                {
                    stmt = connection.createStatement();
                    stmt.execute( String.format( "DELETE FROM %s." + SQLs.TABLE_NAME, schemaName ) );
                    connection.commit();
                }
                finally
                {
                    SQLUtil.closeQuietly( stmt );
                }
            }
            finally
            {
                uow.discard();
            }
        }
    }

}
