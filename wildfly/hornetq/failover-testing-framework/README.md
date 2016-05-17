#HornetQ Failover Testing Framework

A multi-component framework that can be used to test HornetQ failover. It consists in a remote client that
sends and records message IDs on file storage and various receivers (MDBs, remote receivers, etc.) that receive and 
record the IDs of the received messages. The framework comes with reconciliation utilities that can tell whether
messages have not been delivered, or duplicate messages have been delivered.

##Client

A simple JMS client that sends messages in a loop, while recording their IDs and failures
(if they occur), so the set of messages that was effectively sent can be compared with the set of 
received messages.

Builds in top of the "simplest-client" so most the options supported by that client are available here. We don't
support multiple connections, because we want to experiment with connection failover.

    ./bin/send \
     --jndi 127.0.0.1:4447 \
     --destination /queue/novaordis \
     --connection-factory /jms/RemoteConnectionFactory \
     [--username jmsuser --password jmsuser123] \
     [--threads 10] \
     [--sleep-between-sends-ms 1000] \
     [--messages 100] \
     --output /fully/qualified/name/of/the/file/to/write/statistics/into.csv

Also see:

https://github.com/NovaOrdis/playground/tree/master/wildfly/hornetq/simplest-client

Note that the client can also be used to receive messages. 

##MDB

The output file is configured with -Dplayground.failover.testing.framework.output.file in the JBoss configuration. 
Once common option is to set it in its .conf file:

    JAVA_OPTS="${JAVA_OPTS} -Dplayground.failover.testing.framework.output.file=/Users/ovidiu/tmp"
    
Alternatively it can be set in the standalone.xml <properties> section.

The MDB is deployed as an EAR (built by the 'ear' module).

##Tools

The "tools" module contains various command line script to reconcile the results the framework generates.


    reconcile <sender-file> <receiver-file-1> <receiver-file-2> ...
     
Output example:
     
     $ reconcile ./sent.csv received-A.csv ./received-B.csv
     @main 19:43:25,137 INFO  [Reconcile] sent messages:         998
     @main 19:43:25,137 INFO  [Reconcile] received messages:     998
     @main 19:43:25,137 INFO  [Reconcile] all sent messages were received
    