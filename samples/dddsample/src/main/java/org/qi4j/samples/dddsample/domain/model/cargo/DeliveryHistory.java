/*
 * Copyright 2008 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.samples.dddsample.domain.model.cargo;

import org.qi4j.api.query.Query;
import org.qi4j.samples.dddsample.domain.model.ValueObject;
import org.qi4j.samples.dddsample.domain.model.carrier.CarrierMovement;
import org.qi4j.samples.dddsample.domain.model.handling.HandlingEvent;
import org.qi4j.samples.dddsample.domain.model.location.Location;

public interface DeliveryHistory
    extends ValueObject<DeliveryHistory>
{
    /**
     * @return Handling events, ordered by the time the events occured.
     */
    Query<HandlingEvent> eventsOrderedByCompletionTime();

    HandlingEvent lastEvent();

    StatusCode status();

    Location currentLocation();

    CarrierMovement currentCarrierMovement();
}