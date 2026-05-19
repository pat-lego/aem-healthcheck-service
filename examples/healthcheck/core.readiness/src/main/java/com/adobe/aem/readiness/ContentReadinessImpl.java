package com.adobe.aem.readiness;

import java.util.List;

import org.osgi.service.component.annotations.Component;

import com.adobe.skyline.readiness.exceptions.ContentReadyException;
import com.adobe.skyline.readiness.model.Content;
import com.adobe.skyline.readiness.services.ContentReadyProvider;

@Component(immediate = true, service = ContentReadyProvider.class)
public class ContentReadinessImpl implements ContentReadyProvider {

    @Override
    public List<Content> getContent() throws ContentReadyException {
        return List.of(createContent("/content/sample/e1.html", 200));
    }

    public Content createContent(String path, int code) {
        Content c1 = new Content();
        c1.setContentPath(path);
        c1.setResponseCode(code);

        return c1;
    }
}
