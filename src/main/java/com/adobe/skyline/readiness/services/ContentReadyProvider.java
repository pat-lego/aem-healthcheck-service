package com.adobe.skyline.readiness.services;

import java.util.List;

import org.osgi.annotation.versioning.ProviderType;

import com.adobe.skyline.readiness.exceptions.ContentReadyException;
import com.adobe.skyline.readiness.model.Content;

/**
 * Service contract for components that publish content readiness checks.
 *
 * <p>Implementations are responsible for returning the content paths that
 * should be checked by the readiness endpoint and the response code observed
 * for each path.</p>
 */
@ProviderType
public interface ContentReadyProvider {

    /**
     * Returns the content readiness results that should be exposed by the
     * content readiness servlet.
     *
     * @return content readiness results, one entry per checked content path
     * @throws ContentReadyException if the content readiness results cannot be
     *         collected
     */
    List<Content> getContent() throws ContentReadyException;

}
