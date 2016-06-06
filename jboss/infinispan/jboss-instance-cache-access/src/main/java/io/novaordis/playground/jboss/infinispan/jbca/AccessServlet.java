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

package io.novaordis.playground.jboss.infinispan.jbca;

import org.infinispan.Cache;
import org.jboss.as.clustering.infinispan.DefaultCacheContainer;
import org.jboss.as.clustering.infinispan.subsystem.CacheService;
import org.jboss.as.clustering.msc.AsynchronousService;
import org.jboss.as.clustering.msc.ServiceContainerHelper;
import org.jboss.as.server.CurrentServiceContainer;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 */
public class AccessServlet extends HttpServlet {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(AccessServlet.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // HttpServlet overrides -------------------------------------------------------------------------------------------

    @Override
    public void init()
    {
        log.info(this + " initialized");
    }

    @Override
    public void destroy()
    {
        log.info(this + " destroyed");
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {

        ServiceContainer sc = CurrentServiceContainer.getServiceContainer();

//        for(ServiceName sn: sc.getServiceNames()) {
//            log.info("" + sn);
//        }

        ServiceName sn = ServiceName.of("jboss", "infinispan", "web", "repl");
        ServiceController scon = sc.getService(sn);
        Cache cache = (Cache)scon.getValue();
        log.info("" + cache);


        String path = req.getPathInfo();

        Object o = null;

        if ("/put".equals(path)) {
            cache.put("test", "blah");
        }
        else if ("/get".equals(path)) {

            o = cache.get("test");
        }

        res.setContentType("text/html");
        PrintWriter out = res.getWriter();

        if (o != null) {
            out.println(o);
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {
        return "AccessServlet[" + Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
