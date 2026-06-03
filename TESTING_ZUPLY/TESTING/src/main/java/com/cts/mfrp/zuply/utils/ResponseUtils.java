package com.cts.mfrp.zuply.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * The Zuply API wraps every response in {"success":..., "message":..., "data":...}.
 * The OpenAPI/spec doc shows the unwrapped shape, so tests assert against the inner
 * payload. Use {@link #body(Response)} to get a JsonPath rooted at the inner data.
 *
 * If the response has no "data" envelope (older or partially migrated endpoints),
 * the raw JsonPath is returned so tests continue to work.
 */
public final class ResponseUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ResponseUtils() {}

    /** JsonPath rooted at response.data if present, else at response root. */
    public static JsonPath body(Response r) {
        try {
            JsonNode node = MAPPER.readTree(r.asString());
            JsonNode data = node.get("data");
            if (data != null && !data.isNull()) {
                return JsonPath.from(data.toString());
            }
            return r.jsonPath();
        } catch (Exception e) {
            return r.jsonPath();
        }
    }

    /** Read response.success when present (defaults to true if envelope absent). */
    public static boolean isSuccess(Response r) {
        try {
            JsonNode node = MAPPER.readTree(r.asString());
            JsonNode s = node.get("success");
            return s == null || s.asBoolean(true);
        } catch (Exception e) {
            return true;
        }
    }
}
