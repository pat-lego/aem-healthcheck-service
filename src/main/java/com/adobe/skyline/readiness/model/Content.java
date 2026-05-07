package com.adobe.skyline.readiness.model;

import lombok.Getter;
import lombok.Setter;

/**
 * JSON-serializable readiness result for a single content path.
 */
@Getter
@Setter
public class Content {

    /**
     * Content path that was checked.
     */
    String contentPath;

    /**
     * HTTP response code observed when the content path was checked.
     */
    int responseCode;
}
