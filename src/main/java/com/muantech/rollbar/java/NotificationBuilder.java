package com.muantech.rollbar.java;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NotificationBuilder {
    private static final String NOTIFIER_VERSION = "0.1.2";

    private final String accessToken;
    private final String environment;

    private final JSONObject notifierData;
    private final JSONObject serverData;

    protected NotificationBuilder(String accessToken, String environment) throws JSONException, UnknownHostException {
        this.accessToken = accessToken;
        this.environment = environment;

        notifierData = getNotifierData();
        serverData = getServerData();
    }

    public JSONObject build(String level, String message, Throwable throwable,
                            RollbarAttributeProvider attributeProvider) throws JSONException {
        if (attributeProvider == null) {
            attributeProvider = new RollbarAttributeAdapter();
        }
        JSONObject payload = new JSONObject();

        // access token
        payload.put("access_token", this.accessToken);

        // data
        JSONObject data = new JSONObject();

        // general values
        data.put("environment", this.environment);
        data.put("level", level);
        data.put("platform", attributeProvider.getPlatform());
        data.put("framework", attributeProvider.getFramework());
        data.put("language", "java");
        data.put("timestamp", System.currentTimeMillis() / 1000);

        // message data
        data.put("body", getBody(message, throwable));

        // request data
        JSONObject requestData = getRequestData(attributeProvider);
        if (requestData != null) {
            data.put("request", requestData);
        }

        // custom data
        JSONObject customData = new JSONObject();
        Map<String, String> customFields = attributeProvider.getCustomFields();
        if (customFields != null) {
            for (Entry<String, String> entry : customFields.entrySet()) {
                customData.put(entry.getKey(), entry.getValue());
            }
        }

        // log message
        if (message != null) {
            customData.put("message", message);
        }

        if (customData.length() > 0) {
            data.put("custom", customData);
        }

        // person data
        JSONObject personData = getPersonData(attributeProvider);
        if (personData != null) {
            data.put("person", personData);
        }

        // client data
        JSONObject clientData = getClientData(attributeProvider);
        if (clientData != null) {
            data.put("client", clientData);
        }

        // server data
        data.put("server", serverData);

        // notifier data
        data.put("notifier", notifierData);

        payload.put("data", data);

        return payload;
    }

    private JSONObject getBody(String message, Throwable original) throws JSONException {
        JSONObject body = new JSONObject();

        Throwable throwable = original;

        if (throwable != null) {
            List<JSONObject> traces = new ArrayList<JSONObject>();
            do {
                traces.add(0, createTrace(throwable));
                throwable = throwable.getCause();
            } while (throwable != null);

            body.put("trace_chain", new JSONArray(traces));
        }

        if (original == null && message != null) {
            JSONObject messageBody = new JSONObject();
            messageBody.put("body", message);
            body.put("message", messageBody);
        }

        return body;
    }

    private JSONObject getRequestData(RollbarAttributeProvider attributeProvider) throws JSONException {
        JSONObject requestData = new JSONObject();

        // url: full URL where this event occurred
        String url = attributeProvider.getUrl();
        if (url != null) {
            requestData.put("url", url);
        }

        // method: the request method
        String method = attributeProvider.getHttpMethod();
        if (method != null) {
            requestData.put("method", method);
        }

        // headers
        Map<String, String> headers = attributeProvider.getHeaders();
        if (headers != null && ! headers.isEmpty()) {
            JSONObject headersData = new JSONObject();
            for (Entry<String, String> entry : headers.entrySet()) {
                headersData.put(entry.getKey(), entry.getValue());
            }
            requestData.put("headers", headersData);
        }

        // params
        Map<String, String> params = attributeProvider.getParams();
        if (params != null && ! params.isEmpty()) {
            JSONObject paramsData = new JSONObject();
            for (Entry<String, String> entry : params.entrySet()) {
                paramsData.put(entry.getKey(), entry.getValue());
            }
            String key = method != null ? (method.equalsIgnoreCase("post") ? "POST" : "GET")
                                        : "parameters";
            requestData.put(key, paramsData);
        }

        // query string
        String query = attributeProvider.getQuery();
        if (query != null) {
            requestData.put("query_string", query);
        }

        // user ip
        String userIP = attributeProvider.getUserIp();
        if (userIP != null) {
            requestData.put("user_ip", userIP);
        }

        // sessionId
        String sessionId = attributeProvider.getSessionId();
        if (sessionId != null) {
            requestData.put("session", sessionId);
        }

        // protocol
        String protocol = attributeProvider.getProtocol();
        if (protocol != null) {
            requestData.put("protocol", protocol);
        }

        // requestId
        String requestId = attributeProvider.getRequestId();
        if (requestId != null) {
            requestData.put("id", requestId);
        }

        if (requestData.length() > 0) {
            return requestData;
        } else {
            return null;
        }
    }

    private JSONObject getClientData(RollbarAttributeProvider attributeProvider) throws JSONException {
        JSONObject clientData = null;

        String userAgent = attributeProvider.getUserAgent();
        if (userAgent != null) {
            clientData = new JSONObject();

            JSONObject javascript = new JSONObject();
            javascript.put("browser", userAgent);

            clientData.put("javascript", javascript);
        }

        return clientData;
    }

    private JSONObject getPersonData(RollbarAttributeProvider attributeProvider) throws JSONException {
        JSONObject personData = null;

        String id = attributeProvider.getUserId();
        String username = attributeProvider.getUsername();
        String email = attributeProvider.getUserEmail();
        if (id != null || username != null || email != null) {
            personData = new JSONObject();

            setIfNotNull("id", personData, id);
            setIfNotNull("username", personData, username);
            setIfNotNull("email", personData, email);
        }
        return personData;
    }

    private JSONObject getNotifierData() throws JSONException {
        JSONObject notifier = new JSONObject();
        notifier.put("name", "rollbar-java");
        notifier.put("version", NOTIFIER_VERSION);
        return notifier;
    }

    private JSONObject getServerData() throws JSONException, UnknownHostException {
        InetAddress localhost = InetAddress.getLocalHost();

        String host = localhost.getHostName();
        String ip = localhost.getHostAddress();

        JSONObject notifier = new JSONObject();
        notifier.put("host", host);
        notifier.put("ip", ip);
        return notifier;
    }

    private void setIfNotNull(String jsonKey, JSONObject object, String value) throws JSONException {
        if (value != null) {
            object.put(jsonKey, value);
        }
    }

    private JSONObject createTrace(Throwable throwable) throws JSONException {
        JSONObject trace = new JSONObject();

        JSONArray frames = new JSONArray();

        StackTraceElement[] elements = throwable.getStackTrace();
        for (int i = elements.length - 1; i >= 0; --i) {
            StackTraceElement element = elements[i];

            JSONObject frame = new JSONObject();

            frame.put("class_name", element.getClassName());
            frame.put("filename", element.getFileName());
            frame.put("method", element.getMethodName());

            if (element.getLineNumber() > 0) {
                frame.put("lineno", element.getLineNumber());
            }

            frames.put(frame);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        throwable.printStackTrace(ps);
        ps.close();
        try {
            baos.close();
        } catch (IOException e) {
            // not possible with a ByteArrayOutputStream
            throw new RuntimeException(e);
        }

        trace.put("raw", baos.toString());

        JSONObject exceptionData = new JSONObject();
        exceptionData.put("class", throwable.getClass().getName());
        exceptionData.put("message", throwable.getMessage());

        trace.put("frames", frames);
        trace.put("exception", exceptionData);

        return trace;
    }
}