package com.adobe.skyline.readiness.servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardContextSelect;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;

import com.adobe.skyline.readiness.exceptions.ContentReadyException;
import com.adobe.skyline.readiness.model.Content;
import com.adobe.skyline.readiness.services.ContentReadyProvider;
import com.google.gson.Gson;

/**
 * OSGi Http Whiteboard servlet that exposes content readiness details as JSON.
 *
 * <p>
 * The servlet delegates content collection to the registered
 * {@link ContentReadyProvider} and serializes the returned content readiness
 * results with Gson.
 * </p>
 */
@Component(service = Servlet.class, immediate = true)
@HttpWhiteboardServletPattern("/content/*")
@HttpWhiteboardContextSelect("(osgi.http.whiteboard.context.name=com.adobe.skyline.readiness)")
public class ReadinessContentServlet extends HttpServlet {

    private ContentReadyProvider contentReadyProvider;

    /**
     * Creates the servlet with the content readiness provider used for each
     * request.
     *
     * @param contentReadyProvider provider that supplies content readiness
     *                             results
     */
    @Activate
    public ReadinessContentServlet(
            @Reference(cardinality = ReferenceCardinality.OPTIONAL) ContentReadyProvider contentReadyProvider) {
        this.contentReadyProvider = contentReadyProvider;
    }

    /**
     * Handles a content readiness request and writes the provider results as
     * JSON.
     *
     * @param req  servlet request
     * @param resp servlet response
     * @throws IOException      if the JSON response cannot be written
     * @throws ServletException if the request cannot be handled by the servlet
     *                          container
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        try (PrintWriter stream = resp.getWriter()) {
            List<Content> content = Optional.ofNullable(this.contentReadyProvider)
                    .map(contentReadyProvider -> {
                        try {
                            return contentReadyProvider.getContent();
                        } catch (ContentReadyException e) {
                            return List.<Content>of();
                        }
                    })
                    .orElse(List.of());
            String json = new Gson().toJson(content);
            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_OK);
            stream.write(json);
            stream.flush();
        }
    }

}
