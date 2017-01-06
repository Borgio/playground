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

package io.novaordis.playground.http.server.http;

import io.novaordis.playground.http.server.connection.Connection;
import io.novaordis.playground.http.server.connection.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/4/17
 */
public class HttpRequest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    /**
     * Read a request from the current position in the given connection. Never returns null.
     *
     * @exception ConnectionException  if a connection-related failure occurs. ConnectionException are only used to
     * indicate Connection bad quality or instability. Once a ConnectionException has been triggered by a Connection
     * instance, the connection instance should be generally considered unreliable. The calling layer should close it as
     * soon as possible.
     *
     * @exception InvalidHttpRequestException if we fail to parse the data arrived on the wire into a HttpRequest.
     */
    public static HttpRequest readRequest(Connection connection)
            throws ConnectionException, InvalidHttpRequestException {

        ByteArrayOutputStream header = new ByteArrayOutputStream();

        //
        // read up to the blank line - this will include the request line and all headers
        //

        boolean newLine = false;

        while(true) {

            int i = connection.read();

            if (i == -1) {

                //
                // input stream closed, this is an interesting situation as it should not happen, but process it
                // nonetheless, build the request and let the response logic deal with it
                //

                log.warn("connection closed before encountering a blank line");

                break;
            }

            if ('\r' == (char)i) {

                if (newLine) {

                    //
                    // blank line encountered
                    //

                    break;
                }

                //
                // ignore it
                //

                continue;

            }
            else if ('\n' == (char)i) {

                if (newLine) {

                    //
                    // blank line encountered, or we're the first and the last line; it will NOT be written into the
                    // header buffer
                    //

                    break;
                }

                newLine = true;

                //
                // write it into the header buffer
                //
            }
            else {

                newLine = false;
            }

            header.write(i);
        }

        return new HttpRequest(header.toByteArray());
    }

    public static String showRequest(HttpRequest r) {

        String s = "";

        s += r.getMethod() + " " + r.getPath() + " " + r.getHttpVersion() + "\n";

        for(HttpHeader h: r.getHeaders()) {

            s += h.toString() + "\n";
        }

        return s;
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    private byte[] content;
    private HttpMethod method;
    private String path;
    private String version;

    //
    // maintains the headers in the order they were read from the socket
    //
    private List<HttpHeader> headers;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * The header content, as read from the socket. Does not include the blank line.
     *
     * @exception InvalidHttpHeaderException on faulty content that cannot be translated into a valid HTTP header
     */
    public HttpRequest(byte[] content) throws InvalidHttpRequestException {

        this.content = content;
        this.headers = new ArrayList<>();

        int from = 0;

        for(int i = 0; i < content.length; i ++) {

            if (content[i] == '\n' || i == content.length - 1) {

                //
                // correction for the last element
                //
                i = i == content.length - 1 && content[i] != '\n' ? i + 1 : i;

                if (from == 0) {

                    parseFirstLine(content, from, i);
                }
                else {

                    HttpHeader h = HttpHeader.parseHeader(content, from, i);
                    headers.add(h);
                }

                from = i + 1;
            }
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public HttpMethod getMethod() {

        return method;
    }

    public String getPath() {

        return path;
    }

    /**
     * @return the HTTP version string as extracted from the first line of the request.
     */
    public String getHttpVersion() {

        return version;
    }

    /**
     *
     * @return the headers in the order they were read from the socket. May return an empty list, but never null.
     * Returns the internal storage.
     */
    public List<HttpHeader> getHeaders() {

        return headers;
    }

    /**
     * @param fieldName the header's field name.
     *
     * @return Will return null if there is not such a header.
     */
    public HttpHeader getHeader(String fieldName) {

        for(HttpHeader h: headers) {

            if (h.getFieldName().equals(fieldName)) {

                return h;
            }
        }

        return null;
    }

    @Override
    public String toString() {

        if (content == null) {
            return "null content";
        }

        return getMethod() + " " + getPath() + " " + getHttpVersion();
    }

    // Package protected -----------------------------------------------------------------------------------------------

    /**
     * @param to the index of the first element <b>to ignore</b>. It may extend beyond the array boundary and the
     *           method implementation should be prepared to deal with this.
     */
    void parseFirstLine(byte[] content, int from, int to) throws InvalidHttpRequestException {

        int pathIndex = -1;
        int versionIndex = -1;
        int rightLimit = to >= content.length ? content.length : to;

        for(int i = from; i < rightLimit; i ++) {

            if (content[i] == ' ') {

                if (pathIndex == -1) {

                    String methodString = new String(content, 0, i);

                    try {

                        method = HttpMethod.valueOf(methodString);
                    }
                    catch(IllegalArgumentException e) {
                        throw new InvalidHttpMethodException("unknown HTTP method: \"" + methodString + "\"");
                    }
                    pathIndex = i + 1;
                    continue;
                }

                path = new String(content, pathIndex, i - pathIndex);
                versionIndex = i + 1;
                break;
            }
        }

        if (path == null) {

            path = new String(content, pathIndex, content.length - pathIndex);
        }

        if (versionIndex == -1 || versionIndex >= content.length) {

            throw new InvalidHttpRequestException("missing HTTP version");
        }

        this.version = new String(content, versionIndex, rightLimit - versionIndex);

        if (version.trim().isEmpty()) {

            throw new InvalidHttpRequestException("missing HTTP version");
        }

        if (!"HTTP/1.1".equals(version)) {

            throw new InvalidHttpRequestException(version + " protocol version not supported");
        }
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
