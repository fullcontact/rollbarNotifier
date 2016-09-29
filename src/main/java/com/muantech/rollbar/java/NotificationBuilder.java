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

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

public class NotificationBuilder {
    private static final String NOTIFIER_VERSION = "0.1.3";

    private final String accessToken;
    private final String environment;

    private final JsonObject notifierData;
    private final JsonObject serverData;

    /**
     * Constructs a new rollbar notification builder.
     *
     * @param apiKey API Key to identify to rollbar against
     * @param environment Name of the environment notifications are being sent from
     * @param codePackageRoot Optional String to represent the root package, for example "com.fullcontact"
     */
    protected NotificationBuilder(String apiKey, String environment, String codePackageRoot) {
        this.accessToken = apiKey;
        this.environment = environment;

        notifierData = getNotifierData();
        serverData = getServerData(codePackageRoot);
    }

    public JsonObject build(String level, String message, Throwable throwable,
                            RollbarAttributeProvider attributeProvider) {
        if (attributeProvider == null) {
            attributeProvider = new RollbarAttributeAdapter();
        }
        JsonObject payload = new JsonObject();

        // access token
        payload.add("access_token", this.accessToken);

        // data
        JsonObject data = new JsonObject();

        // general values
        data.add("environment", this.environment);
        data.add("level", level);
        data.add("platform", attributeProvider.getPlatform());
        data.add("framework", attributeProvider.getFramework());
        data.add("language", "java");
        data.add("timestamp", System.currentTimeMillis() / 1000);

        // message data
        data.add("body", getBody(message, throwable));

        // request data
        JsonObject requestData = getRequestData(attributeProvider);
        if (requestData != null) {
            data.add("request", requestData);
        }

        // custom data
        JsonObject customData = new JsonObject();
        Map<String, String> customFields = attributeProvider.getCustomFields();
        if (customFields != null) {
            for (Entry<String, String> entry : customFields.entrySet()) {
                customData.add(entry.getKey(), entry.getValue());
            }
        }

        // log message
        if (message != null) {
            customData.add("message", message);
        }

        if (! customData.isEmpty()) {
            data.add("custom", customData);
        }

        // person data
        JsonObject personData = getPersonData(attributeProvider);
        if (personData != null) {
            data.add("person", personData);
        }

        // client data
        JsonObject clientData = getClientData(attributeProvider);
        if (clientData != null) {
            data.add("client", clientData);
        }

        // server data
        data.add("server", serverData);

        // notifier data
        data.add("notifier", notifierData);

        payload.add("data", data);

        return payload;
    }

    private JsonObject getBody(String message, Throwable original) {
        JsonObject body = new JsonObject();

        Throwable throwable = original;

        if (throwable != null) {
            List<JsonObject> traces = new ArrayList<JsonObject>(8);
            do {
                traces.add(0, createTrace(throwable));
                throwable = throwable.getCause();
            } while (throwable != null);
            JsonArray traceChain = new JsonArray();
            traces.forEach((t) -> traceChain.add(t));
            body.add("trace_chain", traceChain);
        }

        if (original == null && message != null) {
            JsonObject messageBody = new JsonObject();
            messageBody.add("body", message);
            body.add("message", messageBody);
        }

        return body;
    }

    private JsonObject getRequestData(RollbarAttributeProvider attributeProvider) {
        JsonObject requestData = new JsonObject();

        // url: full URL where this event occurred
        String url = attributeProvider.getUrl();
        if (url != null) {
            requestData.add("url", url);
        }

        // method: the request method
        String method = attributeProvider.getHttpMethod();
        if (method != null) {
            requestData.add("method", method);
        }

        // headers
        Map<String, String> headers = attributeProvider.getHeaders();
        if (headers != null && ! headers.isEmpty()) {
            JsonObject headersData = new JsonObject();
            for (Entry<String, String> entry : headers.entrySet()) {
                headersData.add(entry.getKey(), entry.getValue());
            }
            requestData.add("headers", headersData);
        }

        // params
        Map<String, String> params = attributeProvider.getParams();
        if (params != null && ! params.isEmpty()) {
            JsonObject paramsData = new JsonObject();
            for (Entry<String, String> entry : params.entrySet()) {
                paramsData.add(entry.getKey(), entry.getValue());
            }
            String key = method != null ? (method.equalsIgnoreCase("post") ? "POST" : "GET")
                                        : "parameters";
            requestData.add(key, paramsData);
        }

        // query string
        String query = attributeProvider.getQuery();
        if (query != null) {
            requestData.add("query_string", query);
        }

        // user ip
        String userIP = attributeProvider.getUserIp();
        if (userIP != null) {
            requestData.add("user_ip", userIP);
        }

        // sessionId
        String sessionId = attributeProvider.getSessionId();
        if (sessionId != null) {
            requestData.add("session", sessionId);
        }

        // protocol
        String protocol = attributeProvider.getProtocol();
        if (protocol != null) {
            requestData.add("protocol", protocol);
        }

        // requestId
        String requestId = attributeProvider.getRequestId();
        if (requestId != null) {
            requestData.add("id", requestId);
        }

        if (requestData.isEmpty()) {
            return null;
        } else {
            return requestData;
        }
    }

    private JsonObject getClientData(RollbarAttributeProvider attributeProvider) {
        JsonObject clientData = null;

        String userAgent = attributeProvider.getUserAgent();
        if (userAgent != null) {
            clientData = new JsonObject();

            JsonObject javascript = new JsonObject();
            javascript.add("browser", userAgent);

            clientData.add("javascript", javascript);
        }

        return clientData;
    }

    private JsonObject getPersonData(RollbarAttributeProvider attributeProvider) {
        JsonObject personData = null;

        String id = attributeProvider.getUserId();
        String username = attributeProvider.getUsername();
        String email = attributeProvider.getUserEmail();
        if (id != null || username != null || email != null) {
            personData = new JsonObject();

            setIfNotNull("id", personData, id);
            setIfNotNull("username", personData, username);
            setIfNotNull("email", personData, email);
        }
        return personData;
    }

    private JsonObject getNotifierData() {
        JsonObject notifier = new JsonObject();
        notifier.add("name", "rollbar-java");
        notifier.add("version", NOTIFIER_VERSION);
        return notifier;
    }

    private JsonObject getServerData(String codePackageRoot) {
        InetAddress localhost;
        try {
            localhost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        String host = localhost.getHostName();
        String ip = localhost.getHostAddress();

        JsonObject notifier = new JsonObject();
        notifier.add("host", host);
        notifier.add("ip", ip);
        if (codePackageRoot != null && ! codePackageRoot.isEmpty()) {
            notifier.add("root", codePackageRoot);
        }
        return notifier;
    }

    private void setIfNotNull(String jsonKey, JsonObject object, String value) {
        if (value != null) {
            object.add(jsonKey, value);
        }
    }

    private JsonObject createTrace(Throwable throwable) {
        JsonObject trace = new JsonObject();
        JsonArray frames = new JsonArray();

        StackTraceElement[] elements = throwable.getStackTrace();
        for (int i = elements.length - 1; i >= 0; --i) {
            StackTraceElement element = elements[i];

            JsonObject frame = new JsonObject();

            frame.add("class_name", element.getClassName());
            frame.add("filename", element.getFileName());
            frame.add("method", element.getMethodName());

            if (element.getLineNumber() > 0) {
                frame.add("lineno", element.getLineNumber());
            }

            frames.add(frame);
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

        trace.add("raw", baos.toString());

        JsonObject exceptionData = new JsonObject();
        exceptionData.add("class", throwable.getClass().getName());
        exceptionData.add("message", throwable.getMessage());

        trace.add("frames", frames);
        trace.add("exception", exceptionData);

        return trace;
    }
}