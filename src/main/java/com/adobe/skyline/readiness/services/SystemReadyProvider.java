package com.adobe.skyline.readiness.services;

import org.osgi.annotation.versioning.ProviderType;

import com.adobe.skyline.readiness.exceptions.SystemReadyException;
import com.adobe.skyline.readiness.utils.ComponentResolver;

/**
 * Service contract for components that publish system readiness checks.
 *
 * <p>Implementations should perform a lightweight check of the subsystem they
 * represent and report whether that subsystem can serve traffic. The core
 * readiness servlet calls the registered {@code SystemReadyProvider} and uses
 * its callback messages to build the HTTP response.</p>
 */
@ProviderType
public interface SystemReadyProvider {

    /**
     * Determines whether the implementing subsystem is ready to serve traffic.
     *
     * @param componentResolver resolver for checking whether required OSGi
     *        services are registered
     * @return {@code true} when the subsystem is ready; {@code false} when it
     *         should be reported as unavailable or degraded
     * @throws SystemReadyException if the readiness check cannot be completed
     */
    boolean isReady(ComponentResolver componentResolver) throws SystemReadyException;


    /**
     * Called by the readiness servlet when
     * {@link #isReady(ComponentResolver)} returns {@code false}.
     *
     * <p>Implementations can use this callback to emit diagnostic logs, update
     * metrics, or capture failure state that helps explain why the subsystem is
     * not ready.</p>
     *
     * @return message to write to the readiness response body, or {@code null}
     *         if no body should be returned
     */
    String isReadyFailed();

    /**
     * Called by the readiness servlet when
     * {@link #isReady(ComponentResolver)} returns {@code true}.
     *
     * <p>Implementations can use this callback to clear prior failure state,
     * update metrics, or record that the subsystem passed the latest readiness
     * check.</p>
     *
     * @return message to write to the readiness response body, or {@code null}
     *         if no body should be returned
     */
    String isReadySucceeded();
}
