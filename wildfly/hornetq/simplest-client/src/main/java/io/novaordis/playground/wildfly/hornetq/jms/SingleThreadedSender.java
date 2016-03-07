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

package io.novaordis.playground.wildfly.hornetq.jms;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleThreadedSender implements Runnable
{
    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private int id;
    private AtomicInteger remainingToSend;
    private AtomicInteger messagesSent;
    private CyclicBarrier barrier;
    private Connection connection;
    private Destination destination;

    // Constructors ----------------------------------------------------------------------------------------------------

    public SingleThreadedSender(int id, Connection connection, Destination destination,
                                AtomicInteger remainingToSend, AtomicInteger messagesSent,
                                CyclicBarrier barrier)
    {
        this.id = id;
        this.connection = connection;
        this.destination = destination;
        this.remainingToSend = remainingToSend;
        this.messagesSent = messagesSent;
        this.barrier = barrier;
    }

    // Runnable implementation -----------------------------------------------------------------------------------------

    public void run()
    {
        Session session;

        System.out.println(this + " using connection " + connection);

        try
        {
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(destination);

            while (true)
            {
                int remaining = remainingToSend.getAndDecrement();

                if (remaining <= 0)
                {
                    break;
                }

                //Message m = session.createTextMessage("test");
                Message m = session.createObjectMessage("test-" + System.currentTimeMillis());
                producer.send(m);
                messagesSent.incrementAndGet();
            }
        }
        catch(Exception e)
        {
            System.out.println("thread " + Thread.currentThread().getName() + " failed: " + e);
        }
        finally
        {
            try
            {
                barrier.await();
            }
            catch(Exception e)
            {
                System.out.println("failed to wait on barrier: " + e);
            }
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public String toString()
    {
        return "SingleThreadedRunner[" + id + "]";
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
