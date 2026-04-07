# API Endpoints

**Base URL:** `http://localhost:8080/api/v1`

**Common Headers:**

| Header | Description |
|--------|-------------|
| `Authorization: Bearer <token>` | Required for all authenticated routes |
| `Content-Type: application/json` | Required for all request bodies |
| `X-Session-Id: <string>` | Optional session identifier for guest cart/order tracking |

---

## Table of Contents

- [Authentication](#authentication)
- [Account](#account)
- [Two-Factor Authentication](#two-factor-authentication)
- [Products](#products)
- [Reviews](#reviews)
- [Cart](#cart)
- [Orders](#orders)
- [Categories](#categories)
- [Brands](#brands)
- [Filters](#filters)
- [Contact](#contact)
- [Payment Webhooks](#payment-webhooks)
- [Admin — Products](#admin--products)
- [Admin — Orders](#admin--orders)
- [Admin — Categories](#admin--categories)
- [Admin — Brands](#admin--brands)
- [Admin — Users](#admin--users)
- [Admin — Reviews](#admin--reviews)
- [Enums Reference](#enums-reference)

---

## Authentication

### `POST /auth/register`
Register a new user account.

**Request Body:**
```json
{
  "email": "user@example.com",       // required, valid email
  "password": "secret123",           // required, 5–100 chars
  "displayName": "John Doe",         // required, 1–30 chars
  "captchaToken": "03AGdBq..."       // required
}
```

**Response `200`:**
```json
{
  "accessToken": "eyJhbGci...",
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "displayName": "John Doe"
  }
}
```

---

### `POST /auth/login`
Login with email and password. Optionally include a 2FA code if enabled.

**Request Body:**
```json
{
  "email": "user@example.com",   // required
  "password": "secret123",       // required
  "twoFactorCode": "123456"      // optional, required if 2FA is enabled
}
```

**Response `200`:** Returns an object (structure depends on 2FA state — may return tokens or a 2FA challenge).

---

### `POST /auth/logout`
Logout the currently authenticated user. Invalidates the refresh token.

**Request Body:** none

**Response `200`:** empty

---

### `GET /auth/refresh`
Exchange a valid refresh token (stored in cookie) for a new access token.

**Request Body:** none

**Response `200`:**
```json
{
  "accessToken": "eyJhbGci..."
}
```

---

### `POST /auth/reset-password`
Request a password reset email.

**Request Body:**
```json
{
  "email": "user@example.com"   // required, valid email
}
```

**Response `200`:** empty

---

### `POST /auth/reset-password-verify`
Complete a password reset using the token from the reset email.

**Request Body:**
```json
{
  "token": "abc123...",     // required
  "password": "newpass1"    // required, 6–80 chars
}
```

**Response `200`:** empty

---

## Account

### `GET /account`
Get the authenticated user's basic profile.

**Response `200`:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "displayName": "John Doe"
}
```

---

### `PATCH /account`
Update the authenticated user's display name.

**Request Body:**
```json
{
  "displayName": "Jane Doe"   // required
}
```

**Response `200`:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "displayName": "Jane Doe"
}
```

---

### `GET /account/details`
Get full account details including email and contact info.

**Response `200`:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "displayName": "John Doe",
  "email": "user@example.com",
  "contact": {
    "name": "John Doe",
    "email": "user@example.com",
    "phone": "+1234567890",
    "address": {
      "streetLine1": "123 Main St",
      "streetLine2": "Apt 4B",
      "city": "New York",
      "stateOrProvince": "NY",
      "postalCode": "10001",
      "country": "US"
    }
  }
}
```

---

### `GET /account/orders`
Get paginated order history for the authenticated user.

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `status` | string (enum) | No | Filter by order status. One of: `PENDING_PAYMENT`, `PAID`, `FAILED`, `SHIPPED`, `DELIVERED`, `CANCELLED`, `REFUND_PENDING`, `REFUNDED`, `REFUND_FAILED` |
| `sort` | string (enum) | No | `asc` or `desc` |
| `page` | integer | No | Page number (0-based) |
| `pageSize` | integer | No | Items per page |

**Response `200`:**
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "status": "PAID",
      "paymentId": "pi_3NqX...",
      "sessionId": "sess_abc",
      "items": [
        {
          "productId": 1,
          "productName": "Widget Pro",
          "image": { "id": 10, "urls": { "sm": "...", "md": "...", "lg": "..." }, "sortOrder": 0, "altText": "Widget" },
          "price": 29.99,
          "quantity": 2,
          "totalAmount": 59.98
        }
      ],
      "shippingType": "STANDARD",
      "shippingCost": 4.99,
      "totalAmount": 64.97,
      "contact": {
        "name": "John Doe",
        "email": "user@example.com",
        "phone": "+1234567890",
        "address": { "streetLine1": "123 Main St", "city": "New York", "country": "US", "postalCode": "10001" }
      },
      "date": 1712345678000,
      "notes": "Leave at door"
    }
  ],
  "page": {
    "page": 0,
    "size": 10,
    "elements": 1,
    "totalElements": 1,
    "totalPages": 1,
    "isFirst": true,
    "isLast": true
  }
}
```

---

### `POST /account/reset-password`
Trigger a password reset email for the currently authenticated user.

**Request Body:** none

**Response `200`:** empty

---

## Two-Factor Authentication

### `GET /2fa`
Get the current 2FA status for the authenticated user.

**Response `200`:**
```json
{
  "isEnabled": false
}
```

---

### `POST /2fa/setup`
Initiate 2FA setup. Returns a TOTP secret and a QR code (base64 image) to display to the user.

**Response `200`:**
```json
{
  "secret": "JBSWY3DPEHPK3PXP",
  "qrCode": "data:image/png;base64,..."
}
```

---

### `POST /2fa/verify`
Verify a TOTP code to complete 2FA setup.

**Request Body:**
```json
{
  "code": "123456"   // required
}
```

**Response `200`:** empty

---

### `POST /2fa/disable`
Disable 2FA for the authenticated user.

**Response `200`:** empty

---

## Products

### `GET /product`
List products with filtering, sorting, and pagination.

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `page` | integer | Yes | Page number (0-based) |
| `size` | integer | Yes | Items per page |
| `sort` | string (enum) | Yes | `TOP`, `PRICE_ASC`, or `PRICE_DESC` |
| `brands` | integer[] | Yes | Filter by brand IDs (empty array for all) |
| `categoryId` | integer | No | Filter by category ID |
| `search` | string | No | Full-text search query |
| `price.min` | number | No | Minimum price filter |
| `price.max` | number | No | Maximum price filter |
| `attributes` | map | No | Attribute filters as a multi-value map e.g. `color=red&color=blue` |

**Response `200`:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Widget Pro",
      "description": "A great widget",
      "sku": "WGT-001",
      "price": 49.99,
      "salePrice": 39.99,
      "stock": 100,
      "brand": { "id": 1, "name": "Acme", "image": null, "isActive": true },
      "category": { "id": 2, "name": "Gadgets" },
      "attributes": [{ "name": "color", "values": ["red", "blue"] }],
      "images": [{ "id": 10, "urls": { "sm": "...", "md": "...", "lg": "..." }, "sortOrder": 0, "altText": "Widget" }],
      "reviewCount": 14,
      "averageRating": 4.3,
      "isActive": true
    }
  ],
  "page": {
    "page": 0, "size": 20, "elements": 1,
    "totalElements": 1, "totalPages": 1,
    "isFirst": true, "isLast": true
  }
}
```

---

### `GET /product/{id}`
Get a single product by ID.

**Path Parameters:** `id` — product ID (integer)

**Response `200`:** Single `ProductDto` (same shape as items above).

---

### `GET /product/{productId}/review`
Get paginated reviews for a product. Includes the current user's review eligibility if authenticated.

**Path Parameters:** `productId` — product ID (integer)

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `page` | integer | No | Page number |
| `size` | integer | No | Items per page (max 20) |

**Response `200`:**
```json
{
  "reviews": {
    "content": [
      {
        "id": 5,
        "rating": 4,
        "body": "Great product!",
        "votes": 12,
        "author": { "id": "550e8400-...", "displayName": "John Doe" },
        "hasVoted": false,
        "createdAt": "2024-04-01T12:00:00Z"
      }
    ],
    "page": { "page": 0, "size": 10, "elements": 1, "totalElements": 1, "totalPages": 1, "isFirst": true, "isLast": true }
  },
  "userReviewStatus": {
    "hasPurchased": true,
    "hasReviewed": false,
    "canReview": true
  }
}
```

---

### `POST /product/{productId}/review`
Submit a review for a product. User must have purchased the product and not already reviewed it.

**Path Parameters:** `productId` — product ID (integer)

**Request Body:**
```json
{
  "rating": 5,            // required, 1–5
  "body": "Loved it!"     // optional, max 512 chars
}
```

**Response `200`:** empty

---

## Reviews

### `POST /review/{reviewId}/vote`
Toggle a helpful vote on a review. Calling again removes the vote.

**Path Parameters:** `reviewId` — review ID (integer)

**Response `200`:**
```json
{
  "voted": true
}
```

---

### `DELETE /review/{reviewId}`
Delete the authenticated user's own review.

**Path Parameters:** `reviewId` — review ID (integer)

**Response `200`:** empty

---

## Cart

> All cart endpoints accept an optional `X-Session-Id` header for guest cart tracking.

### `GET /cart`
Get the current cart for the session or authenticated user.

**Headers:**

| Header | Required | Description |
|--------|----------|-------------|
| `X-Session-Id` | No | Guest session identifier |

**Response `200`:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "sessionId": "sess_abc",
  "items": [
    {
      "productId": 1,
      "productName": "Widget Pro",
      "image": { "id": 10, "urls": { "sm": "...", "md": "...", "lg": "..." }, "sortOrder": 0 },
      "price": 39.99,
      "quantity": 2
    }
  ],
  "subTotalPrice": 79.98,
  "totalPrice": 84.97,
  "shippingType": "STANDARD",
  "shippingCost": 4.99,
  "totalQuantity": 2
}
```

---

### `PUT /cart`
Replace cart items and/or update the shipping type.

**Headers:**

| Header | Required | Description |
|--------|----------|-------------|
| `X-Session-Id` | No | Guest session identifier |

**Request Body:**
```json
{
  "items": [
    { "productId": 1, "quantity": 2 },   // productId and quantity required
    { "productId": 3, "quantity": 1 }
  ],
  "shippingType": "EXPRESS"              // optional: STANDARD | EXPRESS
}
```

**Response `200`:** Updated `CartDto` (same shape as `GET /cart`).

---

### `POST /cart/checkout`
Initiate a Stripe payment session for the current cart. Returns a client secret used to confirm payment on the frontend.

**Headers:**

| Header | Required | Description |
|--------|----------|-------------|
| `X-Session-Id` | No | Guest session identifier |

**Response `200`:**
```json
{
  "clientSecret": "pi_3NqX..._secret_..."
}
```

---

## Orders

### `POST /order/submit`
Submit a new order using the current cart contents. Requires checkout to have been initiated first.

**Headers:**

| Header | Required | Description |
|--------|----------|-------------|
| `X-Session-Id` | No | Guest session identifier |

**Request Body:**
```json
{
  "name": "John Doe",               // required
  "phone": "+1234567890",           // required
  "email": "user@example.com",      // required, valid email
  "shippingType": "STANDARD",       // required: STANDARD | EXPRESS
  "notes": "Leave at door",         // optional
  "address": {
    "streetLine1": "123 Main St",   // required
    "streetLine2": "Apt 4B",        // optional
    "city": "New York",             // required
    "stateOrProvince": "NY",        // optional
    "postalCode": "10001",          // required
    "country": "US"                 // required
  }
}
```

**Response `200`:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PENDING_PAYMENT",
  "paymentId": "pi_3NqX...",
  "items": [
    { "productId": 1, "productName": "Widget Pro", "price": 39.99, "quantity": 2, "totalAmount": 79.98 }
  ],
  "shippingType": "STANDARD",
  "shippingCost": 4.99,
  "totalAmount": 84.97,
  "contact": { "name": "John Doe", "email": "user@example.com", "phone": "+1234567890", "address": { ... } },
  "date": 1712345678000,
  "notes": "Leave at door"
}
```

---

### `GET /order`
Retrieve an order by Stripe payment intent ID (used for post-payment confirmation).

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `paymentIntentId` | string | No | Stripe payment intent ID |

**Response `200`:** `OrderDto` (same shape as above).

---

## Categories

### `GET /category/{id}`
Get a category and its associated attributes.

**Path Parameters:** `id` — category ID (integer)

**Response `200`:**
```json
{
  "id": 2,
  "name": "Gadgets",
  "attributes": [
    {
      "id": 1,
      "attribute": "color",
      "label": "Color",
      "unit": null,
      "dataType": "TEXT",
      "filterType": "CHECKBOX",
      "displayOrder": 1,
      "isPublic": true,
      "categories": [2]
    }
  ]
}
```

---

### `GET /category/attribute/{id}`
Get a single category attribute by ID.

**Path Parameters:** `id` — attribute ID (integer)

**Response `200`:** Single `CategoryAttributeDto` (same shape as attribute object above).

---

## Brands

### `GET /brand/{id}`
Get a brand by ID.

**Path Parameters:** `id` — brand ID (integer)

**Response `200`:**
```json
{
  "id": 1,
  "name": "Acme",
  "image": {
    "id": 5,
    "urls": { "sm": "...", "md": "...", "lg": "..." },
    "sortOrder": 0,
    "altText": "Acme logo"
  },
  "isActive": true
}
```

---

## Filters

### `GET /filter`
Get all available filter options for a given category (brands, attributes, price range).

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `categoryId` | integer | Yes | Category to load filters for |

**Response `200`:**
```json
{
  "category": { "id": 2, "name": "Gadgets" },
  "brands": [
    { "id": 1, "name": "Acme", "count": 14 }
  ],
  "attributes": [
    {
      "id": 1,
      "attribute": "color",
      "label": "Color",
      "unit": null,
      "filterType": "CHECKBOX",
      "displayOrder": 1,
      "values": [ ... ]
    }
  ],
  "price": { "min": 4.99, "max": 999.99 }
}
```

---

## Contact

### `POST /contact`
Send a contact or support enquiry.

**Request Body:**
```json
{
  "name": "John Doe",              // required
  "email": "user@example.com",     // required
  "message": "I have a question."  // required
}
```

**Response `200`:** empty

---

## Payment Webhooks

### `POST /payment/webhook/stripe`
Stripe webhook receiver. Called by Stripe to notify of payment events.

**Headers:**

| Header | Required | Description |
|--------|----------|-------------|
| `Stripe-Signature` | Yes | Stripe webhook signature for request verification |

**Request Body:** Raw Stripe event JSON (string)

**Response `200`:** empty

---

## Admin — Products

> All `/admin/*` endpoints require an authenticated user with the `ADMIN` role.

### `GET /admin/product`
List all products with full admin detail.

**Query Parameters:** Same as `GET /product` — `page`, `size`, `sort`, `brands`, `categoryId`, `search`, `price`, `attributes`.

**Response `200`:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Widget Pro",
      "description": "...",
      "sku": "WGT-001",
      "price": 49.99,
      "salePrice": 39.99,
      "stock": 100,
      "brand": { "id": 1, "name": "Acme", "isActive": true },
      "category": { "id": 2, "name": "Gadgets" },
      "attributes": [ { "name": "color", "values": ["red"] } ],
      "images": [ { "id": 10, "urls": { "sm": "...", "md": "...", "lg": "..." }, "sortOrder": 0 } ],
      "reviewCount": 14,
      "averageRating": 4.3,
      "isActive": true
    }
  ],
  "page": { "page": 0, "size": 20, "elements": 1, "totalElements": 1, "totalPages": 1, "isFirst": true, "isLast": true }
}
```

---

### `POST /admin/product`
Create a new product with images. Sent as `multipart/form-data`.

**Request Body (`multipart/form-data`):**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `product` | JSON object | Yes | Product data (see below) |
| `images` | binary[] | Yes | Image files |

**`product` JSON:**
```json
{
  "name": "Widget Pro",             // required
  "sku": "WGT-001",                // required
  "description": "A great widget", // optional
  "price": 49.99,                  // required, >= 0
  "salePrice": 39.99,              // required, >= 0
  "stock": 100,                    // required, >= 0
  "brandId": 1,                    // optional
  "categoryId": 2,                 // optional
  "isActive": true,                // required
  "images": [
    { "fileName": "front.jpg", "sortOrder": 0, "altText": "Front view" }
  ],
  "attributes": [
    { "name": "color", "values": ["red", "blue"] }
  ]
}
```

**Response `200`:** `AdminProductDto`

---

### `GET /admin/product/{id}`
Get a product by ID (admin view, includes all fields).

**Path Parameters:** `id` — product ID (integer)

**Response `200`:** `AdminProductDto`

---

### `PATCH /admin/product/{id}`
Partially update a product. Sent as `multipart/form-data`. Fields are wrapped in `JsonNullable` — omit a field to leave it unchanged.

**Path Parameters:** `id` — product ID (integer)

**Request Body (`multipart/form-data`):**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `product` | JSON object | Yes | Partial product data |
| `images` | binary[] | Yes | Replacement image files |
| `image_metadata` | JSON array | Yes | Metadata for each image: `{ "fileName": "...", "sortOrder": 0, "altText": "..." }` |

**`product` JSON (all fields use `JsonNullable` wrapper):**
```json
{
  "id": 1,
  "name": { "present": true },         // set present: true with a value to update, omit or present: false to skip
  "sku": { "present": true },
  "description": { "present": true },
  "price": { "present": true },
  "salePrice": { "present": true },
  "stock": { "present": true },
  "brandId": { "present": true },
  "categoryId": { "present": true },
  "images": { "present": true },
  "attributes": { "present": true },
  "isActive": { "present": true }
}
```

**Response `200`:** `AdminProductDto`

---

### `DELETE /admin/product/{id}`
Delete a product.

**Path Parameters:** `id` — product ID (integer)

**Response `200`:** empty

---

## Admin — Orders

### `GET /admin/order/{orderId}`
Get full order details by ID.

**Path Parameters:** `orderId` — UUID

**Response `200`:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PAID",
  "paymentId": "pi_3NqX...",
  "sessionId": "sess_abc",
  "items": [
    { "productId": 1, "productName": "Widget Pro", "price": 39.99, "quantity": 2, "totalAmount": 79.98 }
  ],
  "shippingType": "EXPRESS",
  "shippingCost": 9.99,
  "totalAmount": 89.97,
  "contact": {
    "name": "John Doe",
    "email": "user@example.com",
    "phone": "+1234567890",
    "address": { "streetLine1": "123 Main St", "city": "New York", "country": "US", "postalCode": "10001" }
  },
  "date": 1712345678000,
  "notes": null
}
```

---

### `PATCH /admin/order/{orderId}`
Update an order's status or shipping type.

**Path Parameters:** `orderId` — UUID

**Request Body:**
```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",   // required
  "status": "SHIPPED",                                   // optional: see OrderStatus enum
  "shippingType": "EXPRESS"                              // optional: STANDARD | EXPRESS
}
```

**Response `200`:** `AdminOrderDto`

---

### `POST /admin/order/{orderId}/cancel`
Cancel an order.

**Path Parameters:** `orderId` — UUID

**Request Body:**
```json
{
  "reason": "Customer requested cancellation"   // required, max 512 chars
}
```

**Response `200`:** empty

---

### `POST /admin/order/{orderId}/refund`
Issue a refund for an order via Stripe.

**Path Parameters:** `orderId` — UUID

**Request Body:**
```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",   // required
  "reason": "Item was damaged"                           // optional
}
```

**Response `200`:** empty

---

## Admin — Categories

### `POST /admin/category`
Create a new category.

**Request Body:**
```json
{
  "name": "Gadgets",          // required
  "attributeIds": [1, 2, 3]  // optional, IDs of existing attributes to assign
}
```

**Response `200`:**
```json
{
  "id": 2,
  "name": "Gadgets",
  "attributes": [
    {
      "id": 1,
      "attribute": "color",
      "label": "Color",
      "unit": null,
      "dataType": "TEXT",
      "filterType": "CHECKBOX",
      "displayOrder": 1
    }
  ]
}
```

---

### `GET /admin/category/{id}`
Get a category by ID.

**Path Parameters:** `id` — category ID (integer)

**Response `200`:** `AdminCategoryDto`

---

### `PUT /admin/category/{id}`
Replace a category's name and assigned attributes.

**Path Parameters:** `id` — category ID (integer)

**Request Body:**
```json
{
  "id": 2,                   // required
  "name": "Electronics",     // required
  "attributeIds": [1, 4]     // optional
}
```

**Response `200`:** `AdminCategoryDto`

---

### `DELETE /admin/category/{id}`
Delete a category.

**Path Parameters:** `id` — category ID (integer)

**Response `200`:** empty

---

### `GET /admin/category/search`
Search categories by name (paginated).

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `query` | string | Yes | Search term |
| `page` | integer | Yes | Page number |
| `size` | integer | Yes | Items per page |

**Response `200`:** `PagedResponseAdminCategoryDto`

---

### `POST /admin/category/attribute`
Create a new category attribute.

**Request Body:**
```json
{
  "attribute": "color",       // required, internal key
  "label": "Color",           // required, display label
  "unit": "nm",               // optional
  "dataType": "TEXT",         // required: TEXT | NUMBER | BOOLEAN
  "filterType": "CHECKBOX",   // required: CHECKBOX | SLIDER | DROPDOWN
  "displayOrder": 1,          // required
  "isPublic": true,           // required
  "categories": [2, 3]        // optional, category IDs to assign to
}
```

**Response `200`:**
```json
{
  "id": 1,
  "attribute": "color",
  "label": "Color",
  "unit": null,
  "dataType": "TEXT",
  "filterType": "CHECKBOX",
  "displayOrder": 1,
  "isPublic": true,
  "categories": [2, 3]
}
```

---

### `GET /admin/category/attribute/{attributeId}`
Get a single category attribute by ID.

**Path Parameters:** `attributeId` — attribute ID (integer)

**Response `200`:** `AdminCategoryAttributeDto`

---

### `PUT /admin/category/attribute/{id}`
Update a category attribute.

**Path Parameters:** `id` — attribute ID (integer)

**Request Body:**
```json
{
  "id": 1,                    // required
  "attribute": "colour",      // required
  "label": "Colour",          // required
  "unit": null,               // optional
  "dataType": "TEXT",         // required: TEXT | NUMBER | BOOLEAN
  "filterType": "DROPDOWN",   // required: CHECKBOX | SLIDER | DROPDOWN
  "displayOrder": 2,          // required
  "isPublic": false,          // required
  "categories": [2]           // optional
}
```

**Response `200`:** `AdminCategoryAttributeDto`

---

### `DELETE /admin/category/attribute/{id}`
Delete a category attribute.

**Path Parameters:** `id` — attribute ID (integer)

**Response `200`:** empty

---

### `GET /admin/category/attribute/search`
Search category attributes by name (paginated).

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `query` | string | Yes | Search term |
| `page` | integer | No | Page number |
| `size` | integer | No | Items per page |

**Response `200`:** `PagedResponseAdminCategoryAttributeDto`

---

## Admin — Brands

### `POST /admin/brand`
Create a new brand.

**Request Body:**
```json
{
  "name": "Acme",    // required
  "image": 5,        // optional, existing image ID (integer)
  "isActive": true   // required
}
```

**Response `200`:**
```json
{
  "id": 1,
  "name": "Acme",
  "image": { "id": 5, "urls": { "sm": "...", "md": "...", "lg": "..." }, "sortOrder": 0 },
  "isActive": true
}
```

---

### `GET /admin/brand/{id}`
Get a brand by ID.

**Path Parameters:** `id` — brand ID (integer)

**Response `200`:** `AdminBrandDto`

---

### `PUT /admin/brand/{id}`
Replace a brand's details.

**Path Parameters:** `id` — brand ID (integer)

**Request Body:**
```json
{
  "id": 1,             // required
  "name": "Acme Corp", // required
  "image": 6,          // optional, new image ID
  "isActive": false    // required
}
```

**Response `200`:** `AdminBrandDto`

---

### `DELETE /admin/brand/{id}`
Delete a brand.

**Path Parameters:** `id` — brand ID (integer)

**Response `200`:** empty

---

### `GET /admin/brand/search`
Search brands by name (paginated).

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `query` | string | Yes | Search term |
| `page` | integer | Yes | Page number |
| `size` | integer | Yes | Items per page |

**Response `200`:** `PagedResponseAdminBrandDto`

---

## Admin — Users

### `GET /admin/user/{userId}`
Get a user by ID with full details.

**Path Parameters:** `userId` — UUID

**Response `200`:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "displayName": "John Doe",
  "twoFactorEnabled": false,
  "role": "USER",
  "contact": {
    "name": "John Doe",
    "email": "user@example.com",
    "phone": "+1234567890",
    "address": { "streetLine1": "123 Main St", "city": "New York", "country": "US" }
  }
}
```

---

### `PATCH /admin/user/{userId}`
Update a user's role.

**Path Parameters:** `userId` — UUID

**Request Body:**
```json
{
  "role": "ADMIN"   // required: ADMIN | USER
}
```

**Response `200`:** `AdminUserDto`

---

## Admin — Reviews

### `GET /admin/review/{reviewId}`
Get a review by ID with full author details.

**Path Parameters:** `reviewId` — review ID (integer)

**Response `200`:**
```json
{
  "id": 5,
  "rating": 3,
  "body": "It was okay.",
  "votes": 2,
  "author": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "displayName": "John Doe",
    "twoFactorEnabled": false,
    "role": "USER"
  }
}
```

---

### `DELETE /admin/review/{reviewId}`
Delete a review with an administrative reason.

**Path Parameters:** `reviewId` — review ID (integer)

**Request Body:**
```json
{
  "cause": "Violated community guidelines"   // required
}
```

**Response `200`:** empty

---

## Enums Reference

| Enum | Values |
|------|--------|
| `OrderStatus` | `PENDING_PAYMENT`, `PAID`, `FAILED`, `SHIPPED`, `DELIVERED`, `CANCELLED`, `REFUND_PENDING`, `REFUNDED`, `REFUND_FAILED` |
| `ShippingType` | `STANDARD`, `EXPRESS` |
| `ProductSort` | `TOP`, `PRICE_ASC`, `PRICE_DESC` |
| `DataType` | `TEXT`, `NUMBER`, `BOOLEAN` |
| `FilterType` | `CHECKBOX`, `SLIDER`, `DROPDOWN` |
| `Role` | `ADMIN`, `USER` |
| `SortOrder` | `asc`, `desc` |