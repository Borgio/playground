/*
 * Copyright (c) 2017 Nova Ordis LLC
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

package io.novaordis.playground.http.server;

import io.novaordis.playground.http.server.connection.Connection;
import io.novaordis.playground.http.server.connection.ConnectionException;
import io.novaordis.playground.http.server.http.HttpRequest;
import io.novaordis.playground.http.server.http.HttpResponse;
import io.novaordis.playground.http.server.http.HttpStatusCode;
import io.novaordis.playground.http.server.http.InvalidHttpRequestException;
import io.novaordis.playground.http.server.http.header.HttpEntityHeader;
import io.novaordis.playground.http.server.http.header.HttpResponseHeader;
import io.novaordis.playground.http.server.rhandler.FileRequestHandler;
import io.novaordis.playground.http.server.rhandler.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles a Connection (essentially the content arriving from the underlying socket) by extracting HTTP requests
 * and sending HTTP responses, on a dedicated thread. This way, each connection is handled by one, dedicated thread.
 * If connections are created and destroyed for each request, then each request such generated gets its own thread.
 *
 * The connection handler's job is to turn request bytes into a HttpRequest instance and turn a HttpResponse into bytes
 * and send it back over the connection. It does not handle the request. That is the RequestHandler's job.
 *
 * The handler is responsible for closing the connection, if appropriate.
 *
 * @see FileRequestHandler
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/4/17
 */
public class ConnectionHandler implements Runnable {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(ConnectionHandler.class);
    private static final boolean debug = log.isDebugEnabled();

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private final Connection connection;
    private final Thread connectionHandlerThread;

    private boolean closeConnectionAfterResponse;

    private volatile boolean active;

    private Server server;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param server reference to use to invoke the exit() method on, in case the exit request was sent into the server.
     */
    public ConnectionHandler(Server server, Connection connection) {

        this.server = server;
        this.connection = connection;
        this.active = true;
        this.closeConnectionAfterResponse = false;

        String threadName = connection.toString() + " Handling Thread";
        connectionHandlerThread = new Thread(this, threadName);
        connectionHandlerThread.setDaemon(true);
    }

    // Runtime implementation ------------------------------------------------------------------------------------------

    /**
     * The top level connection handling loop. Declared as the Runnable.run() method and not as a closure in constructor
     * for testing.
     */
    public void run() {

        try {

            while(active) {

                active = processRequestResponsePair();
            }
        }
        catch(ConnectionException e) {

            //
            // this is where we handle connection problems and close the connection
            //

            String msg = e.getMessage();
            msg = msg == null ? "connection failure: " + e.toString() : "connection failure: " + msg;
            log.error(msg);
            log.debug(msg, e);
        }
        finally {

            connection.close();

            //
            // leave the handler in a tidy state
            //
            active = false;
        }

    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * Initiate request processing on the handler's own thread
     */
    public void handleRequests() {

        connectionHandlerThread.start();
    }

    /**
     * @return true if the main loop is active, processing request/responses or blocked in either read or write.
     */
    public boolean isActive() {

        return active;
    }

    @Override
    public String toString() {

        return "handler for " + connection;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    /**
     * Processes a request/response sequence.
     *
     * @throws ConnectionException on connection troubles
     *
     * @return false if this was the last request on the input stream (and the input stream was consequently closed)
     * or true if we should expect more requests on the same input stream.
     */
    boolean processRequestResponsePair() throws ConnectionException {

        try {

            HttpRequest request = HttpRequest.readRequest(connection);

            if (request == null) {

                return false;
            }

            logRequest(request);

            HttpResponse response = passToHandler(request);

            response = prepareResponseForSending(response);

            sendResponse(response);
        }
        catch (InvalidHttpRequestException e) {

            //
            // failed because we were not able to parse the data came on the wire, do not exit from the request
            // handling loop, most likely something is wrong with this request only.
            //
            String msg = e.getMessage();
            log.error(msg);
            log.debug("failed to parse HTTP request", e);
            sendResponse(new HttpResponse(HttpStatusCode.BAD_REQUEST, msg.getBytes()));
        }

        return true;
    }

    /**
     * The method applies verifications and state updates required by a response before being sent to the client.
     *
     * Example:
     *
     * * Make sure Content-Length is set, and if no, set it to zero.
     * * Add the Server header.
     *
     * etc.
     *
     */
    HttpResponse prepareResponseForSending(HttpResponse response) {

        //
        // Content-Length
        //

        if (response.getHeader(HttpEntityHeader.CONTENT_LENGTH).isEmpty()) {

            response.addHeader(HttpEntityHeader.CONTENT_LENGTH, "0");
        }

        if (response.getHeader(HttpResponseHeader.SERVER).isEmpty()) {

            response.addHeader(HttpResponseHeader.SERVER, server.getServerType());
        }


        return response;
    }

    /**
     * This is the method that actually writes the response on the wire.
     *
     * The method does not close connection voluntarily, it lets the client enforce its own policy.
     *
     * @exception ConnectionException  if a connection-related failure occurs. ConnectionException are only used to
     * indicate Connection bad quality or instability. Once a ConnectionException has been triggered by a Connection
     * instance, the instance should be generally considered unreliable and closed as soon as possible.
     */
    void sendResponse(HttpResponse response) throws ConnectionException {

        // this includes the empty line and the appropriately set Connection-Length
        byte[] b = response.statusLineAndHeadersToWireFormat();

        connection.write(b);
        connection.flush();

        //
        // write the body
        //

        byte[] body = response.getBody();

        if (body != null && body.length > 0) {

            connection.write(body);
        }

        connection.flush();

        logResponse(response);

        //
        // close or do not close the connection depending on the configuration.
        //

        if (closeConnectionAfterResponse) {

            //
            // close our connection and stop our thread
            //
            active = false;

            log.debug("closing connection because " + this + " was configured to close the connection after a response");
            connection.close();
        }
    }

    void setCloseConnectionAfterResponse(boolean b) {

        this.closeConnectionAfterResponse = b;
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    private void logResponse(HttpResponse response) {

        HttpRequest request = response.getRequest();

        HttpStatusCode sc = response.getStatusCode();

        String s = request == null ? "request" : request.getMethod() + " " + request.getPath();
        s +=  " returned " + sc.getStatusCode() + " " + sc.getReasonPhrase() +
                " over " + connection;

        if (debug) {
            log.debug(s + ":\n" + HttpResponse.showResponse(response));
        }
        else {
            log.info(s);
        }
    }

    /**
     * Iterates over the list of handlers and passes the request for processing to the first handler that accepts
     * it.
     */
    private HttpResponse passToHandler(HttpRequest request) {

        for (RequestHandler h : server.getHandlers()) {

            if (h.accepts(request)) {

                return h.processRequest(request);
            }
        }

        //
        // no known handler, send a 422 Unprocessable Entity
        //

        return new HttpResponse(HttpStatusCode.UNPROCESSABLE_ENTITY);
    }

    private void logRequest(HttpRequest request) {

        if (!debug) {

            return;
        }

        log.debug("request read from " + connection + " input stream:\n" + HttpRequest.showRequest(request));
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
