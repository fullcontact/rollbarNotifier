package com.muantech.rollbar.java;

import static org.junit.Assert.*;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.eclipsesource.json.JsonObject;

public class NotificationBuilderTest {
    private static final String TOKEN = "tkn";
    private static final String ENVIRONMENT = "enviro";

    private NotificationBuilder builder;

    @Before
    public void setup() throws UnknownHostException {
        builder = new NotificationBuilder(TOKEN, ENVIRONMENT, null);
    }

    @After
    public void cleanup() {
        builder = null;
    }

    @Test
    public void basicBuild() {
        JsonObject result = builder.build("INFO", null, null, null);

        assertEquals(TOKEN, result.getString("access_token", null));
        JsonObject data = result.get("data").asObject();

        assertEquals(ENVIRONMENT, data.getString("environment", null));
        assertEquals("INFO", data.getString("level", null));
        assertTrue(data.getLong("timestamp", -1) > 0);
        assertNotNull(data.get("server"));
        assertNotNull(data.get("notifier"));
        assertNull(data.getString("request", null));
        assertNull(data.getString("person", null));
        assertNull(data.getString("client", null));
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

        JsonObject result = builder.build("INFO", null, null, new RollbarAttributeAdapter() {
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

        JsonObject requestJson = result.get("data").asObject().get("request").asObject();

        assertEquals(url, requestJson.getString("url", null));
        assertEquals(httpMethod, requestJson.getString("method", null));
        for (Map.Entry<String, String> header : headers.entrySet()) {
            assertEquals(header.getValue(), requestJson.get("headers").asObject().getString(header.getKey(), null));
        }
        assertEquals(query, requestJson.getString("query_string", null));
        assertEquals(userIp, requestJson.getString("user_ip", null));
        assertEquals(sessionId, requestJson.getString("session", null));
        assertEquals(protocol, requestJson.getString("protocol", null));
        assertEquals(request, requestJson.getString("id", null));
    }

    @Test
    public void customDataBuild() {
        final String message = "fooMessage";
        final Map<String, String> customData = new HashMap<String, String>();
        customData.put("fooKey1", "fooValue1");
        customData.put("fooKey2", "fooValue2");

        JsonObject result = builder.build("INFO", message, null, new RollbarAttributeAdapter() {
            @Override
            public Map<String, String> getCustomFields() {
                return customData;
            }
        });

        JsonObject customJson = result.get("data").asObject()
                                      .get("custom").asObject();

        assertEquals(message, customJson.getString("message", null));
        for (Map.Entry<String, String> header : customData.entrySet()) {
            assertEquals(header.getValue(), customJson.getString(header.getKey(), null));
        }
    }

    @Test
    public void personDataBuild() {
        final String userId = "fooId";
        final String username = "fooName";
        final String userEmail = "fooEmail";

        JsonObject result = builder.build("INFO", null, null, new RollbarAttributeAdapter() {
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

        JsonObject personJson = result.get("data").asObject().get("person").asObject();

        assertEquals(userId, personJson.getString("id", null));
        assertEquals(username, personJson.getString("username", null));
        assertEquals(userEmail, personJson.getString("email", null));
    }

    @Test
    public void userAgentBuild() {
        final String userAgent = "fooAgent";

        JsonObject result = builder.build("INFO", null, null, new RollbarAttributeAdapter() {
            @Override
            public String getUserAgent() {
                return userAgent;
            }
        });

        assertEquals(userAgent, result.get("data").asObject()
                                      .get("client").asObject()
                                      .get("javascript").asObject()
                                      .getString("browser", null));
    }
}
