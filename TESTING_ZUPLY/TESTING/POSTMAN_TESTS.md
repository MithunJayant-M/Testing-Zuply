# Zuply API — Postman Verification Guide

Run these requests in Postman to confirm each test method's endpoint behaves as expected.

## Base URL & Auth

```
Base URL : https://urban-space-winner-v666rvvrj557cp9r7-4200.app.github.dev
```

| Role | How to obtain JWT |
|---|---|
| **Admin** | `POST /api/auth/login` with `admin@zuply.in` / `Admin@123` |
| **Buyer** | First `POST /api/auth/register` (role=`CUSTOMER`, with `phone`), then `POST /api/auth/login` |
| **Seller** | First `POST /api/auth/register` (role=`SELLER`, with `phone`), then `POST /api/auth/login` |

Add header `Authorization: Bearer <token>` to all authenticated requests.
All POST/PUT/PATCH bodies use `Content-Type: application/json` (except `/api/upload` which is `multipart/form-data`).

---

## 1. Authentication — `AuthTests`

### Register (`testRegister`)
`POST /api/auth/register` — no auth
```json
{
  "name": "John Buyer",
  "email": "john.buyer.001@gmail.com",
  "password": "Test@1234",
  "role": "CUSTOMER",
  "phone": "9876543210"
}
```
Expected: `201 Created` (or `400` for duplicate email / missing fields)

### Login (`testLogin`, `testLoginReturnsJwt`)
`POST /api/auth/login` — no auth
```json
{ "email": "admin@zuply.in", "password": "Admin@123" }
```
Expected: `200 OK` with `data.token` populated.

---

## 2. Users — `UserTests`

| Test | Method | Endpoint | Auth | Body |
|---|---|---|---|---|
| testGetProfileValid | GET | `/api/users/profile` | Buyer | — |
| testUpdateProfile | PUT | `/api/users/profile` | Buyer | `{ "name": "Updated Buyer Name", "email": "updated.buyer@gmail.com" }` |

---

## 3. Products — `ProductTests`

| Test | Method | Endpoint | Auth |
|---|---|---|---|
| testSearchAll | GET | `/api/products` | None |
| testGetProductById | GET | `/api/products/{id}` (use an id created via POST first) | None |
| testGetProductByIdNotFound | GET | `/api/products/99999` | None |
| testGetProductsBySeller | GET | `/api/products/seller/1` | None |
| testCreateProduct | POST | `/api/products` | Seller |
| testCreateProductAsBuyerForbidden | POST | `/api/products` | Buyer (expect 403) |
| testUpdateProduct | PUT | `/api/products/{createdId}` | Seller |

### Create body
```json
{
  "name": "Wireless Bluetooth Headphones",
  "description": "Premium noise-cancelling headphones",
  "category": "Electronics",
  "price": 4999,
  "stock": 100
}
```

### Update body
```json
{
  "name": "Samsung Galaxy S24 Ultra",
  "price": 79999,
  "stock": 40,
  "description": "Updated model with enhanced camera"
}
```

---

## 4. Upload — `UploadTests`

| Test | Method | Endpoint | Auth | Form data |
|---|---|---|---|---|
| testUploadValidImage | POST | `/api/upload` | Seller | key `file`, value: any JPEG/PNG file |
| testUploadAsBuyerForbidden | POST | `/api/upload` | Buyer (expect 403) | same |

Set body type to `form-data` in Postman.

---

## 5. AI Listing — `ListingTests`

| Test | Method | Endpoint | Auth | Notes |
|---|---|---|---|---|
| testGenerate | POST | `/api/listing/generate/{imageId}` | Seller | Use imageId returned from upload |
| testPublish | POST | `/api/listing/{productId}/publish` | Seller | Use productId from generate |

No request body for either.

---

## 6. Cart — `CartTests`

| Test | Method | Endpoint | Auth | Body |
|---|---|---|---|---|
| testGetCart | GET | `/api/cart` | Buyer | — |
| testAddItem | POST | `/api/cart` | Buyer | `{ "productId": 1, "quantity": 1 }` |
| testUpdateItem | PUT | `/api/cart/{itemId}` | Buyer | `{ "quantity": 5 }` |
| testDeleteItem | DELETE | `/api/cart/{itemId}` | Buyer | — |

---

## 7. Orders — `OrderTests`

| Test | Method | Endpoint | Auth | Body |
|---|---|---|---|---|
| testPlaceOrder | POST | `/api/orders` | Buyer (cart must be non-empty) | see below |
| testGetOrders | GET | `/api/orders` | Buyer | — |
| testGetOrderById | GET | `/api/orders/{id}` | Buyer | — |

### Place order body
```json
{
  "addressLine": "42, Anna Nagar, Chennai",
  "city": "Chennai",
  "pincode": "600040",
  "paymentMethod": "RAZORPAY"
}
```

---

## 8. Payment — `PaymentTests`

| Test | Method | Endpoint | Auth | Body |
|---|---|---|---|---|
| testCreateOrder | POST | `/api/payment/create-order` | Buyer | `{ "orderId": 3, "amount": 7497 }` |
| testVerifyTamperedSignature | POST | `/api/payment/verify` | Buyer | tampered values, expect 400 |
| testStatus | GET | `/api/payment/status/{orderId}` | Buyer | — |

### Verify (tampered) body
```json
{
  "razorpayOrderId": "order_FAKE",
  "razorpayPaymentId": "pay_FAKE",
  "razorpaySignature": "tampered",
  "orderId": 3
}
```

---

## 9. Seller — `SellerTests`

| Test | Method | Endpoint | Auth | Body |
|---|---|---|---|---|
| testDashboard | GET | `/api/seller/dashboard` | Seller | — |
| testSellerProducts | GET | `/api/seller/products` | Seller | — |
| testSellerOrders | GET | `/api/seller/orders` | Seller | — |
| testUpdateOrderStatus | PATCH | `/api/seller/orders/1/status` | Seller | `{ "status": "SHIPPED" }` |

Valid status values: `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`.

---

## 10. Admin — `AdminTests`

| Test | Method | Endpoint | Auth | Body |
|---|---|---|---|---|
| testDashboard | GET | `/api/admin/dashboard` | Admin | — |
| testGetSellers | GET | `/api/admin/sellers` | Admin | — |
| testApproveSeller | PATCH | `/api/admin/sellers/2/approve` | Admin | — |
| testApproveProduct | PATCH | `/api/admin/products/5/approve` | Admin | — |
| testRejectProduct | PATCH | `/api/admin/products/6/reject` | Admin | `{ "reason": "Product description violates marketplace policy" }` |
| testAdminOrders | GET | `/api/admin/orders` | Admin | — |

---

## Recommended Postman flow

1. **Create a Postman environment** with variables `baseUrl`, `adminToken`, `buyerToken`, `sellerToken`, `productId`, `imageId`, `orderId`, `cartItemId`.
2. **Run admin login first**, save `data.token` to `adminToken`.
3. **Register + login buyer** → save `buyerToken`.
4. **Register + login seller** → save `sellerToken`.
5. **Upload an image as seller** → save `data.imageId`.
6. **Generate listing → publish** → save `data.productId`.
7. **Add to cart as buyer → place order** → save `data.orderId` and `data.cartItemId`.
8. **Run remaining endpoints** in any order — chain via the saved variables.

Each request response is wrapped:
```json
{ "success": true, "message": "...", "data": { ... } }
```
Use Postman test snippets to extract: `pm.environment.set("token", pm.response.json().data.token)`.
