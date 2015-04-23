package com.muantech.rollbar.java;

import java.util.Map;

/**
 * <p>This is an adapter which provides default implementations for everything in the interface
 * {@link RollbarAttributeProvider}.  Thus allowing you to override the specific attributes you
 * want to have provided, and use the defaults for everything else.</p>
 */
public class RollbarAttributeAdapter implements RollbarAttributeProvider {
    @Override
    public String getPlatform() {
        return "java";
    }

    @Override
    public String getFramework() {
        return "java";
    }

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public String getHttpMethod() {
        return null;
    }

    @Override
    public Map<String, String> getHeaders() {
        return null;
    }

    @Override
    public Map<String, String> getParams() {
        return null;
    }

    @Override
    public String getQuery() {
        return null;
    }

    @Override
    public String getUserIp() {
        return null;
    }

    @Override
    public String getSessionId() {
        return null;
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public String getRequestId() {
        return null;
    }

    @Override
    public Map<String, String> getCustomFields() {
        return null;
    }

    @Override
    public String getUserAgent() {
        return null;
    }

    @Override
    public String getUserId() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public String getUserEmail() {
        return null;
    }
}
