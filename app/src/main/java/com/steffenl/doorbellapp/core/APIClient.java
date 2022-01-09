package com.steffenl.doorbellapp.core;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.steffenl.doorbellapp.core.config.APIConfig;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

public class APIClient {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient();
    private final APIConfig config;

    public APIClient(final APIConfig config) {
        this.config = config;
    }

    public void postDeviceToken(final String token) throws Exception {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", token);
        final String json = jsonObject.toString();
        final RequestBody body = RequestBody.create(json, JSON);
        final Request request = new Request.Builder()
                .url(config.getEndpoint() + "/monitor")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
        }
    }

    public void ring() throws Exception {
        final RequestBody body = RequestBody.create("{}", JSON);
        final Request request = new Request.Builder()
                .url(config.getEndpoint() + "/ring")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
        }
    }

    public DeviceHealthResponseData getDeviceHealth() throws Exception {
        final Request request = new Request.Builder()
                .url(config.getEndpoint() + "/device-health")
                .get()
                .header("Accept", JSON.toString())
                .build();
        try (Response response = client.newCall(request).execute()) {
            // TODO: Handle non-JSON response
            final String json = response.body().string();
            if (json.isEmpty()) {
                return null;
            }
            final Moshi moshi = new Moshi.Builder().build();
            final JsonAdapter<DeviceHealthResponseData> adapter = moshi.adapter(DeviceHealthResponseData.class);
            final DeviceHealthResponseData responseData = adapter.fromJson(json);
            return responseData;
        }
    }

    public static class DeviceHealthResponseData {
        public String batteryLevel;
        public int batteryVoltage;
        public String firmwareVersion;
    }
}
