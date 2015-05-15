package com.muantech.rollbar.java;

import java.util.Map;

public interface RollbarAttributeProvider {
    /**
     * Returns the platform for application.  "Java" is a sensible default.
     *
     * @return Platform name application is running on
     */
    public String getPlatform();

    /**
     * Returns the framework for application.  "Java" is a sensible default.
     *
     * @return Framework name application is running on
     */
    public String getFramework();

    /**
     * Returns the URL for the request which spawned the event to be sent to rollbar.
     *
     * (represented as request.url to rollbar)
     *
     * @return URL being requested, or {@code null} to not report any url
     */
    public String getUrl();

    /**
     * Returns the http method for the request which spawned the event to be sent to rollbar.
     * Examples would be "GET" or "POST".
     *
     * (represented as request.method to rollbar)
     *
     * @return Method of request, or {@code null} to not report any method
     */
    public String getHttpMethod();

    /**
     * Returns the headers for the request which spawned the event to be sent to rollbar.
     *
     * (represented as request.headers to rollbar)
     *
     * @return Headers of request, or {@code null} to not report any headers
     */
    public Map<String, String> getHeaders();

    /**
     * Returns the parameters relevant for the event to be sent to rollbar.
     *
     * @return Parameters, or {@code null} to not report any parameters
     */
    public Map<String, String> getParams();

    /**
     * Returns the query which spawned the event to be sent to rollbar.
     *
     * (represented as request.query_string to rollbar)
     *
     * @return Query string or {@code null} to not report any query
     */
    public String getQuery();

    /**
     * Returns the ip of the remote user which spawned the event to be sent to rollbar.
     *
     * (represented as request.user_ip to rollbar)
     *
     * @return IP string or {@code null} to not report any remote ip
     */
    public String getUserIp();

    /**
     * Returns the session id of the remote user which spawned the event to be sent to rollbar.
     *
     * (represented as request.session to rollbar)
     *
     * @return Session ID or {@code null} to not report any session id
     */
    public String getSessionId();

    /**
     * Returns the protocol used in the client request which spawned the event to be sent to rollbar.
     *
     * (represented as request.protocol to rollbar)
     *
     * @return Protocol name or {@code null} to not report any protocol
     */
    public String getProtocol();

    /**
     * Returns the request id of the remote user which spawned the event to be sent to rollbar.
     *
     * (represented as request.id to rollbar)
     *
     * @return Request ID or {@code null} to not report any request id
     */
    public String getRequestId();

    /**
     * Returns a map of any custom fields that should be sent to rollbar.  This is a key value map
     * which will show up in rollbar as custom.KEY = VALUE.
     *
     * @return Map of custom values to send or {@code null} to not send any custom values
     */
    public Map<String, String> getCustomFields();

    /**
     * Returns the user agent of the remote user which spawned the event to be sent to rollbar.
     *
     * (represented as client.javascript.browser to rollbar)
     *
     * @return User agent string or {@code null} to not report any user agent
     */
    public String getUserAgent();

    /**
     * Returns the id of the remote user which spawned the event to be sent to rollbar.
     *
     * (represented as person.id to rollbar)
     *
     * @return Id string or {@code null} to not report any user id
     */
    public String getUserId();

    /**
     * Returns the username of the remote user which spawned the event to be sent to rollbar.
     *
     * (represented as person.username to rollbar)
     *
     * @return Username or {@code null} to not report any username
     */
    public String getUsername();

    /**
     * Returns the email of the remote user which spawned the event to be sent to rollbar.
     *
     * (represented as person.email to rollbar)
     *
     * @return Email string or {@code null} to not report any user email
     */
    public String getUserEmail();
}
