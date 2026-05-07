package com.adobe.skyline.readiness.utils;

import org.osgi.framework.BundleContext;

/**
 * Helper for resolving OSGi component and service availability from the active
 * bundle context.
 */
public class ComponentResolver {

    private BundleContext bundleContext;

    /**
     * Creates a resolver backed by the bundle context that owns the readiness
     * servlet.
     *
     * @param bundleContext bundle context used to look up registered services
     */
    public ComponentResolver(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /**
     * Checks whether an OSGi service is currently registered.
     *
     * @param service fully qualified service interface or class name
     * @return {@code true} when a matching service reference is available;
     *         {@code false} otherwise
     */
    public boolean isPresent(String service) {
        return (this.bundleContext.getServiceReference(service) == null) ? false : true;
    }

}
