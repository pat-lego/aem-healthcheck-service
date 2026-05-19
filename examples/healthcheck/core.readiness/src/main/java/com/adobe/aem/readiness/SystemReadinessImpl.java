package com.adobe.aem.readiness;

import org.osgi.service.component.annotations.Component;

import com.adobe.skyline.readiness.exceptions.SystemReadyException;
import com.adobe.skyline.readiness.services.SystemReadyProvider;
import com.adobe.skyline.readiness.utils.ComponentResolver;

@Component(immediate = true, service = SystemReadyProvider.class)
public class SystemReadinessImpl implements SystemReadyProvider {

    @Override
    public boolean isReady(ComponentResolver cr) throws SystemReadyException {
            //cr.isPresent("com.adobe.aem.support.test1");
            return true;
    }

    @Override
    public String isReadyFailed() {
        // Handle case if service reference failed
        return null;
    }

    @Override
    public String isReadySucceeded() {
         // Handle case if service reference succeeded
        return null;
    }
    
}
