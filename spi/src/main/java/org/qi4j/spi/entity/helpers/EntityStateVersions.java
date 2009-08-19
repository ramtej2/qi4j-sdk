/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.spi.entity.helpers;

import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.entity.ConcurrentEntityStateModificationException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.structure.ModuleSPI;
import org.qi4j.spi.unitofwork.EntityStoreUnitOfWork;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * JAVADOC
 */
@Mixins(EntityStateVersions.EntityStateVersionsMixin.class)
public interface EntityStateVersions
{
    void forgetVersions( Iterable<EntityState> states);

    void rememberVersion( EntityReference identity, String version );

    void checkForConcurrentModification(Iterable<EntityState> loaded, ModuleSPI module)
        throws ConcurrentEntityStateModificationException;

    class EntityStateVersionsMixin
        implements EntityStateVersions
    {
        @This
        EntityStore store;

        private final Map<EntityReference, String> versions = new WeakHashMap<EntityReference, String>();
        private MetaInfo checkInfo;

        public synchronized void forgetVersions( Iterable<EntityState> states)
        {
            for (EntityState state : states)
            {
                versions.remove(state.identity());
            }
        }

        public synchronized void rememberVersion( EntityReference identity, String version )
        {
            versions.put( identity, version );
        }

        public synchronized void checkForConcurrentModification(Iterable<EntityState> loaded, ModuleSPI module)
                throws ConcurrentEntityStateModificationException
        {
            List<EntityReference> changed = null;
            for (EntityState entityState : loaded)
            {
                if (entityState.status().equals(EntityStatus.NEW))
                    continue;
                
                String storeVersion = versions.get(entityState.identity());
                if (storeVersion == null)
                {
                    checkInfo = new MetaInfo();
                    EntityStoreUnitOfWork unitOfWork = store.newUnitOfWork(Usecase.DEFAULT, checkInfo, module);
                    EntityState state = unitOfWork.getEntityState(entityState.identity());
                    storeVersion = state.version();
                    unitOfWork.discard();
                }

                if (!entityState.version().equals(storeVersion))
                {
                    if (changed == null)
                        changed = new ArrayList<EntityReference>();
                    changed.add(entityState.identity());
                }
            }

            if (changed != null)
                throw new ConcurrentEntityStateModificationException(changed);
        }
    }
}