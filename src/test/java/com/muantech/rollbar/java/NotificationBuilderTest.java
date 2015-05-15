package com.muantech.rollbar.java;

import static org.junit.Assert.*;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NotificationBuilderTest {
    private static final String TOKEN = "tkn";
    private static final String ENVIRONMENT = "enviro";

    private NotificationBuilder builder;

    @Before
    public void setup() throws JSONException, UnknownHostException {
        builder = new NotificationBuilder(TOKEN, ENVIRONMENT, null);
    }

    @After
    public void cleanup() {
        builder = null;
    }

    private void verifyMissing(JSONObject json, String key) {
        try {
            json.get(key);
            fail("Data item should not be present");
        } catch (JSONException e) {
            // expected
        }
    }

    @Test
    public void basicBuild() {
        JSONObject result = builder.build("INFO", null, null, null);

        assertEquals(TOKEN, result.get("access_token"));
        JSONObject data = result.getJSONObject("data");

        assertEquals(ENVIRONMENT, data.get("environment"));
        assertEquals("INFO", data.get("level"));
        assertNotNull(data.get("timestamp"));
        assertNotNull(data.get("server"));
        assertNotNull(data.get("notifier"));
        verifyMissing(data, "request");
        verifyMissing(data, "custom");
        verifyMissing(data, "person");
        verifyMissing(data, "client");
    }

    @Test
    public void requestDataBuild() {
        final String url = "foorl";
        final String httpMethod = "fooMethod";
        final Map<String, String> headers = new HashMap<String, String>();
        headers.put("fooKey1", "fooValue1");
        headers.put("fooKey2", "fooValue2");
        final String query = "fooQuery";
        final String userIp = "fooIp";
        final String sessionId = "fooSession";
        final String protocol = "fooProtocol";
        final String request = "fooRequest";

        JSONObject result = builder.build("INFO", null, null, new RollbarAttributeAdapter() {
            @Override
            public String getUrl() {
                return url;
            }

            @Override
            public String getHttpMethod() {
                return httpMethod;
            }

            @Override
            public Map<String, String> getHeaders() {
                return headers;
            }

            @Override
            public String getQuery() {
                return query;
            }

            @Override
            public String getUserIp() {
                return userIp;
            }

            @Override
            public String getSessionId() {
                return sessionId;
            }

            @Override
            public String getProtocol() {
                return protocol;
            }

            @Override
            public String getRequestId() {
                return request;
            }
        });

        JSONObject requestJson = result.getJSONObject("data").getJSONObject("request");

        assertEquals(url, requestJson.get("url"));
        assertEquals(httpMethod, requestJson.get("method"));
        for (Map.Entry<String, String> header : headers.entrySet()) {
            assertEquals(header.getValue(), requestJson.getJSONObject("headers").get(header.getKey()));
        }
        assertEquals(query, requestJson.get("query_string"));
        assertEquals(userIp, requestJson.get("user_ip"));
        assertEquals(sessionId, requestJson.get("session"));
        assertEquals(protocol, requestJson.get("protocol"));
        assertEquals(request, requestJson.get("id"));
    }

    @Test
    public void customDataBuild() {
        final String message = "fooMessage";
        final Map<String, String> customData = new HashMap<String, String>();
        customData.put("fooKey1", "fooValue1");
        customData.put("fooKey2", "fooValue2");

        JSONObject result = builder.build("INFO", message, null, new RollbarAttributeAdapter() {
            @Override
            public Map<String, String> getCustomFields() {
                return customData;
            }
        });

        JSONObject customJson = result.getJSONObject("data").getJSONObject("custom");

        assertEquals(message, customJson.get("message"));
        for (Map.Entry<String, String> header : customData.entrySet()) {
            assertEquals(header.getValue(), customJson.get(header.getKey()));
        }
    }

    @Test
    public void personDataBuild() {
        final String userId = "fooId";
        final String username = "fooName";
        final String userEmail = "fooEmail";

        JSONObject result = builder.build("INFO", null, null, new RollbarAttributeAdapter() {
            @Override
            public String getUserId() {
                return userId;
            }

            @Override
            public String getUsername() {
                return username;
            }

            @Override
            public String getUserEmail() {
                return userEmail;
            }
        });

        JSONObject personJson = result.getJSONObject("data").getJSONObject("person");

        assertEquals(userId, personJson.get("id"));
        assertEquals(username, personJson.get("username"));
        assertEquals(userEmail, personJson.get("email"));
    }

    @Test
    public void userAgentBuild() {
        final String userAgent = "fooAgent";

        JSONObject result = builder.build("INFO", null, null, new RollbarAttributeAdapter() {
            @Override
            public String getUserAgent() {
                return userAgent;
            }
        });

        assertEquals(userAgent, result.getJSONObject("data").getJSONObject("client")
                                                            .getJSONObject("javascript").get("browser"));
    }
}
