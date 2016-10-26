/*
 * Copyright (c) 2016 Nova Ordis LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.novaordis.playground.wildfly.infinispan.hotrodclient.util;

import org.infinispan.client.hotrod.annotation.ClientCacheEntryCreated;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryModified;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryRemoved;
import org.infinispan.client.hotrod.annotation.ClientListener;
import org.infinispan.client.hotrod.event.ClientCacheEntryCreatedEvent;
import org.infinispan.client.hotrod.event.ClientCacheEntryModifiedEvent;
import org.infinispan.client.hotrod.event.ClientCacheEntryRemovedEvent;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 10/26/16
 */
@ClientListener
public class StatisticsEnabledCacheClientListener {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private AtomicLong creationsInTheLastInterval;
    private AtomicLong modificationsInTheLastInterval;
    private AtomicLong removalsInTheLastInterval;

    private long samplingIntervalMs;

    private Timer sampler;

    // Constructors ----------------------------------------------------------------------------------------------------

    public StatisticsEnabledCacheClientListener() {

        creationsInTheLastInterval = new AtomicLong();
        modificationsInTheLastInterval = new AtomicLong();
        removalsInTheLastInterval = new AtomicLong();

        samplingIntervalMs = 1000L;

        //
        // register the timer that reports stats
        //
        sampler = new Timer();
        sampler.scheduleAtFixedRate(new Sampler(), samplingIntervalMs, samplingIntervalMs);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    @ClientCacheEntryModified
    public void entryModified(ClientCacheEntryModifiedEvent event) {

        modificationsInTheLastInterval.incrementAndGet();
    }

    @ClientCacheEntryCreated
    public void entryCreated(ClientCacheEntryCreatedEvent event) {

        creationsInTheLastInterval.incrementAndGet();
    }

    @ClientCacheEntryRemoved
    public void entryRemoved(ClientCacheEntryRemovedEvent event) {

        removalsInTheLastInterval.incrementAndGet();
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    private class Sampler extends TimerTask {

        @Override
        public void run() {

            long accumulatedCreations = creationsInTheLastInterval.getAndSet(0L);
            long accumulatedModifications = modificationsInTheLastInterval.getAndSet(0L);
            long accumulatedRemovals = removalsInTheLastInterval.getAndSet(0L);

            System.out.println(accumulatedCreations + ", " + accumulatedModifications + ", " + accumulatedRemovals);

        }
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
