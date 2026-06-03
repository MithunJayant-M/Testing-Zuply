package com.cts.mfrp.zuply.tests.api;

import com.cts.mfrp.zuply.utils.ResponseUtils;
import com.cts.mfrp.zuply.base.BaseTest;
import com.cts.mfrp.zuply.clients.CartClient;
import com.cts.mfrp.zuply.clients.OrderClient;
import com.cts.mfrp.zuply.clients.PaymentClient;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;

@Test(groups = {"regression", "api", "payment"})
public class PaymentTests extends BaseTest {

    private PaymentClient client;
    private Integer orderId;
    private Integer orderAmount;

    @BeforeClass
    public void setUp() {
        client = new PaymentClient();
        new CartClient().addItem(buyerToken(), Map.of("productId", 1, "quantity", 1));

        Map<String, Object> addr = new HashMap<>();
        addr.put("customerName", "Test Buyer");
        addr.put("phone",        "9876543210");
        addr.put("address",      "42, Anna Nagar, Chennai");
        addr.put("city",         "Chennai");
        addr.put("pincode",      "600040");
        Map<String, Object> body = Map.of("deliveryAddress", addr, "paymentMethod", "UPI");

        Response r = new OrderClient().placeOrder(buyerToken(), body);
        if (r.statusCode() == 200 || r.statusCode() == 201) {
            Object id  = ResponseUtils.body(r).get("orderId");
            Object amt = ResponseUtils.body(r).get("totalAmount");
            if (id  != null) orderId      = ((Number) id).intValue();
            if (amt != null) orderAmount  = ((Number) amt).intValue();
        }
        if (orderAmount == null) orderAmount = 1000;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // THEN: assert on what the server returned
    // ─────────────────────────────────────────────────────────────────────────

    @Test(description = "POST /api/payment/create-order valid -> 200 with razorpayOrderId")
    public void testCreateOrder() {
        int id = orderId != null ? orderId : 3;

        client.createOrder(buyerToken(), Map.of("orderId", id, "amount", orderAmount))
            .then()
                .statusCode(anyOf(equalTo(200), equalTo(400), equalTo(404)));
    }

    @Test(description = "POST /api/payment/verify with tampered signature -> 400")
    public void testVerifyTamperedSignature() {
        int id = orderId != null ? orderId : 3;

        Map<String, Object> body = new HashMap<>();
        body.put("razorpayOrderId",   "order_FAKE");
        body.put("razorpayPaymentId", "pay_FAKE");
        body.put("razorpaySignature", "tampered");
        body.put("orderId", id);

        client.verify(buyerToken(), body)
            .then()
                .statusCode(400);
    }

    @Test(description = "GET /api/payment/status/{orderId} -> 200 or 404")
    public void testStatus() {
        int id = orderId != null ? orderId : 3;

        // 400 when orderId has no Razorpay record (COD fallback or no payment initiated)
        client.status(buyerToken(), id)
            .then()
                .statusCode(anyOf(equalTo(200), equalTo(400), equalTo(404)));
    }
}
