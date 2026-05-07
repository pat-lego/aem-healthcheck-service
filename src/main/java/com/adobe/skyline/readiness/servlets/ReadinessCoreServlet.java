package com.adobe.skyline.readiness.servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Optional;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardContextSelect;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;

import com.adobe.skyline.readiness.exceptions.SystemReadyException;
import com.adobe.skyline.readiness.services.SystemReadyProvider;
import com.adobe.skyline.readiness.utils.ComponentResolver;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * OSGi Http Whiteboard servlet that exposes the core system readiness check.
 *
 * <p>
 * The servlet delegates the readiness decision to the registered
 * {@link SystemReadyProvider}. A successful check returns HTTP 200 with the
 * provider success message, while an unsuccessful or failed check returns HTTP
 * 500.
 * </p>
 */
@Component(service = Servlet.class, immediate = true)
@HttpWhiteboardServletPattern("/core/*")
@HttpWhiteboardContextSelect("(osgi.http.whiteboard.context.name=com.adobe.skyline.readiness)")
public class ReadinessCoreServlet extends HttpServlet {

    private ComponentResolver componentResolver;
    private SystemReadyProvider systemReady;

    /**
     * Creates the servlet with the active OSGi bundle context and system
     * readiness provider.
     *
     * @param bundleContext bundle context used to resolve supporting services
     * @param systemReady   provider that performs the core readiness check
     */
    @Activate
    public ReadinessCoreServlet(BundleContext bundleContext,
            @Reference(cardinality = ReferenceCardinality.OPTIONAL) SystemReadyProvider systemReady) {
        this.componentResolver = new ComponentResolver(bundleContext);
        this.systemReady = systemReady;
    }

    /**
     * Handles a readiness probe request.
     *
     * @param req  servlet request
     * @param resp servlet response
     * @throws ServletException if the request cannot be handled by the servlet
     *                          container
     * @throws IOException      if the readiness response cannot be written
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (PrintWriter stream = resp.getWriter()) {
            boolean isReady = Optional.ofNullable(this.systemReady)
                    .map(systemReady -> {
                        try {
                            return systemReady.isReady(this.componentResolver);
                        } catch (SystemReadyException e) {
                            return false;
                        }
                    })
                    .orElse(true);
            resp.setContentType("application/json");
            
            String message = null;
            if (isReady) {
                resp.setStatus(HttpServletResponse.SC_OK);
                message = Optional.ofNullable(this.systemReady.isReadySucceeded())
                        .orElse("");
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                message = Optional.ofNullable(this.systemReady.isReadyFailed())
                        .orElse("");
            }
            stream.write(message);
            stream.flush();
        }
    }
}
