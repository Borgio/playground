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

package io.novaordis.playground.jee.invoker;

import io.novaordis.playground.jee.ejb.stateless.SimpleStateless;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 3/22/17
 */
public class InvokerServlet extends HttpServlet {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    @EJB
    private SimpleStateless bean;

    // Constructors ----------------------------------------------------------------------------------------------------

    // HttpServlet overrides -------------------------------------------------------------------------------------------

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException  {

        String result = bean.methodOne("something from servlet");

        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        out.println("response from EJB: " + result);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {
        return "InvokerServlet[" + Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
