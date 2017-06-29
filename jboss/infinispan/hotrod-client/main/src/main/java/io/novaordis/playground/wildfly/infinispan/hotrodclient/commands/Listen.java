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

package io.novaordis.playground.wildfly.infinispan.hotrodclient.commands;

import io.novaordis.playground.wildfly.infinispan.hotrodclient.Console;
import io.novaordis.playground.wildfly.infinispan.hotrodclient.UserErrorException;
import io.novaordis.playground.wildfly.infinispan.hotrodclient.util.CacheClientListener;
import io.novaordis.playground.wildfly.infinispan.hotrodclient.util.StatisticsEnabledCacheClientListener;
import org.infinispan.client.hotrod.RemoteCache;

import java.util.Set;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 5/29/16
 */
@SuppressWarnings("unused")
public class Listen extends CacheCommand {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Command implementation ------------------------------------------------------------------------------------------

    @Override
    public void execute(String restOfTheLine) throws Exception {

        RemoteCache defaultCache = insureConnected();

        //noinspection unchecked
        Set<Object> listeners = defaultCache.getListeners();

        if (!listeners.isEmpty()) {
            throw new UserErrorException("a listener has been already registered");
        }

        defaultCache.addClientListener(new CacheClientListener());
        //defaultCache.addClientListener(new StatisticsEnabledCacheClientListener());
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
