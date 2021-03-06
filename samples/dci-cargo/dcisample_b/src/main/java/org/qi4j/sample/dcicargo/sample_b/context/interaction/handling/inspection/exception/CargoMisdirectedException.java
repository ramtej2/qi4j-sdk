/*
 * Copyright 2011 Marc Grue.
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
package org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.exception;

import org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEvent;
import org.qi4j.sample.dcicargo.sample_b.data.structure.itinerary.Itinerary;

/**
 * CargoMisdirectedException
 *
 * This would have to set off notifying the cargo owner and request a re-route.
 */
public class CargoMisdirectedException extends InspectionException
{
    private Itinerary itinerary;

    public CargoMisdirectedException( HandlingEvent handlingEvent, String msg )
    {
        super( createMessage(msg, handlingEvent, null) );
    }

    public CargoMisdirectedException( HandlingEvent handlingEvent, Itinerary itinerary, String msg )
    {
        super( msg );
        this.itinerary = itinerary;
    }

    private static String createMessage( String msg, HandlingEvent handlingEvent, Itinerary itinerary )
    {
        String id = handlingEvent.trackingId().get().id().get();
        String city = handlingEvent.location().get().name().get();

        String itineraryString = "";
        if( itinerary != null )
        {
            itineraryString = itinerary.print();
        }

        return "\nCargo is MISDIRECTED! " + msg + "\n" + handlingEvent.print() + itineraryString
               + "MOCKUP REQUEST TO CARGO OWNER: Please re-route misdirected cargo '" + id + "' (now in " + city + ").";
    }
}