package com.adobe.skyline.readiness.servlets;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.context.ServletContextHelper;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardContext;

/**
 * Servlet context used by the readiness Http Whiteboard servlets.
 *
 * <p>The context mounts readiness endpoints under
 * {@code /adobe/system/readiness} and provides JSON as the default MIME type
 * for resources resolved through this context.</p>
 */
@Component(service = ServletContextHelper.class)
@HttpWhiteboardContext(name = "com.adobe.skyline.readiness", 
path = "/adobe2/system/readiness")
public class ReadinessContext extends ServletContextHelper {

    /**
     * Returns the MIME type for resources served through the readiness context.
     *
     * @param name requested resource name
     * @return JSON MIME type for readiness responses
     */
    @Override
    public String getMimeType(final String name) {
        return "application/json";
    }
}
