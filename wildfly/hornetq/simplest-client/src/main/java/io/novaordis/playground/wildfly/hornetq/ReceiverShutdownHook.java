/*
 * Copyright (c) 2015 Nova Ordis LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.novaordis.playground.wildfly.hornetq;

import javax.jms.Connection;
import java.util.concurrent.atomic.AtomicLong;

public class ReceiverShutdownHook extends Thread
{
    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private Connection connection;
    private AtomicLong messageReceived;

    // Constructors ----------------------------------------------------------------------------------------------------

    public ReceiverShutdownHook(Connection connection, AtomicLong messageReceived)
    {
        this.connection = connection;
        this.messageReceived = messageReceived;
    }

    // Thread overrides ------------------------------------------------------------------------------------------------


    @Override
    public void run()
    {
        try
        {
            if (connection != null)
            {
                connection.close();
                System.out.println(
                    "\nreceiver shutdown hook executed, connection cleanly closed, messages received: " + messageReceived.get());
            }
        }
        catch(Exception e)
        {
            System.out.println("failed to cleanly close connection: " + e);
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
