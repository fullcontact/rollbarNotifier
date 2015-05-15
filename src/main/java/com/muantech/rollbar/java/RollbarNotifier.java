package com.muantech.rollbar.java;

import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONObject;

public class RollbarNotifier {
    private static final int MAX_RETRIES = 5;

    public enum Level {
        DEBUG, INFO, WARNING, ERROR
    }

    private NotificationBuilder builder;
    private URL rollbarURL;

    /**
     * Constructs a new rollbar notifier which sends notifications out on demand to rollbar.
     *
     * @param rollbarURL Url to hit rollbar with (typically {@code "https://api.rollbar.com/api/1/item/"}
     * @param apiKey API Key to notify against rollbar with
     * @param env Name of the environment sending the notifications
     * @throws MalformedURLException Thrown if unable to parse URL
     */
    public RollbarNotifier(String rollbarURL, String apiKey, String env) throws MalformedURLException {
        this(new URL(rollbarURL), apiKey, env, null);
    }

    /**
     * Constructs a new rollbar notifier which sends notifications out on demand to rollbar.
     *
     * @param rollbarURL Url to hit rollbar with (typically {@code "https://api.rollbar.com/api/1/item/"}
     * @param apiKey API Key to notify against rollbar with
     * @param env Name of the environment sending the notifications
     */
    public RollbarNotifier(URL rollbarURL, String apiKey, String env) {
        this(rollbarURL, apiKey, env, null);
    }

    /**
     * Constructs a new rollbar notifier which sends notifications out on demand to rollbar.
     *
     * @param rollbarURL Url to hit rollbar with (typically {@code "https://api.rollbar.com/api/1/item/"}
     * @param apiKey API Key to notify against rollbar with
     * @param env Name of the environment sending the notifications
     * @param codePackageRoot code package root (for example {@code "com.fullcontact"}), to be used with github integration
     * @throws MalformedURLException Thrown if unable to parse URL
     */
    public RollbarNotifier(String rollbarURL, String apiKey, String env, String codePackageRoot) throws MalformedURLException {
        this(new URL(rollbarURL), apiKey, env, codePackageRoot);
    }

    /**
     * Constructs a new rollbar notifier which sends notifications out on demand to rollbar.
     *
     * @param rollbarURL Url to hit rollbar with (typically {@code "https://api.rollbar.com/api/1/item/"}
     * @param apiKey API Key to notify against rollbar with
     * @param env Name of the environment sending the notifications
     * @param codePackageRoot code package root (for example {@code "com.fullcontact"}), to be used with github integration
     */
    public RollbarNotifier(URL rollbarURL, String apiKey, String env, String codePackageRoot) {
        this.rollbarURL = rollbarURL;
        builder = new NotificationBuilder(apiKey, env, codePackageRoot);
    }

    public void notify(String message) {
        notify(Level.INFO, message, null);
    }

    public void notify(String message, RollbarAttributeProvider attributeProvider) {
        notify(Level.INFO, message, attributeProvider);
    }

    public void notify(Level level, String message) {
        notify(level, message, null, null);
    }

    public void notify(Level level, String message, RollbarAttributeProvider attributeProvider) {
        notify(level, message, null, attributeProvider);
    }

    public void notify(Throwable throwable) {
        notify(Level.ERROR, throwable, null);
    }

    public void notify(Throwable throwable, RollbarAttributeProvider attributeProvider) {
        notify(Level.ERROR, throwable, attributeProvider);
    }

    public void notify(String message, Throwable throwable) {
        notify(Level.ERROR, message, throwable, null);
    }

    public void notify(String message, Throwable throwable, RollbarAttributeProvider attributeProvider) {
        notify(Level.ERROR, message, throwable, attributeProvider);
    }

    public void notify(Level level, Throwable throwable) {
        notify(level, null, throwable, null);
    }

    public void notify(Level level, Throwable throwable, RollbarAttributeProvider attributeProvider) {
        notify(level, null, throwable, attributeProvider);
    }

    public void notify(Level level, String message, Throwable throwable, RollbarAttributeProvider attributeProvider) {
        JSONObject payload = builder.build(level.toString(), message, throwable, attributeProvider);
        postJson(payload);
    }

    private void postJson(JSONObject json) {
        HttpRequest request = new HttpRequest(rollbarURL, "POST");

        request.setRequestProperty("Content-Type", "application/json");
        request.setRequestProperty("Accept", "application/json");
        request.setBody(json.toString());

        boolean success = request.execute();
        while (! success && request.getAttemptNumber() < MAX_RETRIES) {
            try {
                // delay attempt to execute again
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // exit without sending result
                return;
            }
            success = request.execute();
        }
    }
}
