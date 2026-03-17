# API Endpoints

Visit Swagger UI (http://localhost:8080/swagger-ui/index.html) for a more detailed overview. The backend has to be running.

---

## Table of Contents

- [Authentication](#auth)
- [Two-Factor Authentication](#two-factor-authentication)
- [Products](#products)
- [Categories](#categories)
- [Category Attributes](#category-attributes)
- [Brands](#brands)
- [Filters](#filters)
- [Cart](#cart)
- [Orders](#orders)
- [Payments](#payments)

---

# API Endpoints

Base URL: `http://localhost:8080/api/v1`

---

## Auth

### `POST /auth/register`
Register a new user account.

**Request Body**
| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `email` | string (email) | ✅ | min 1 char |
| `password` | string | ✅ | 5–100 chars |
| `displayName` | string | ✅ | 1–30 chars |
| `captchaToken` | string | ✅ | min 1 char |

**Response `200`**
```json
{
  "accessToken": "string",
  "user": {
    "id": "uuid",
    "displayName": "string"
  }
}
```

---

### `POST /auth/login`
Authenticate and receive an access token. Include `twoFactorCode` if 2FA is enabled.

**Request Body**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `email` | string | ✅ | |
| `password` | string | ✅ | |
| `twoFactorCode` | string | ❌ | Required if 2FA is enabled |

**Response `200`** — returns auth object

---

### `POST /auth/logout`
Invalidates the current session.

**Response `200`** — empty body

---

### `GET /auth/refresh`
Exchange a refresh token for a new access token.

**Response `200`**
```json
{
  "accessToken": "string"
}
```

---

### `POST /auth/reset-password`
Send a password reset email.

**Request Body**
| Field | Type | Required |
|-------|------|----------|
| `email` | string (email) | ✅ |

**Response `200`** — empty body

---

### `POST /auth/reset-password-verify`
Complete a password reset using a token from email.

**Request Body**
| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `token` | string | ✅ | min 1 char |
| `password` | string | ✅ | 6–80 chars |

**Response `200`** — empty body

---

## Two-Factor Authentication

### `GET /2fa`
Get current 2FA status.

**Response `200`**
```json
{ "isEnabled": true }
```

---

### `POST /2fa/setup`
Begin 2FA setup. Returns a TOTP secret and QR code (base64 image).

**Response `200`**
```json
{
  "secret": "string",
  "qrCode": "string"
}
```

---

### `POST /2fa/verify`
Confirm a TOTP code to complete 2FA setup.

**Request Body**
| Field | Type | Required |
|-------|------|----------|
| `code` | string | ✅ |

**Response `200`** — empty body

---

### `POST /2fa/disable`
Disable 2FA for the authenticated user.

**Response `200`** — empty body

---

## Account

### `GET /account`
Get basic info for the authenticated user.

**Response `200`**
```json
{
  "id": "uuid",
  "displayName": "string"
}
```

---

### `PATCH /account`
Update the authenticated user's display name.

**Request Body**
| Field | Type | Required |
|-------|------|----------|
| `displayName` | string | ✅ |

**Response `200`** — returns updated `UserDto`

---

### `GET /account/details`
Get full account info including contact details.

**Response `200`**
```json
{
  "id": "uuid",
  "displayName": "string",
  "email": "string",
  "contact": {
    "name": "string",
    "email": "string",
    "phone": "string",
    "address": {
      "streetLine1": "string",
      "streetLine2": "string",
      "city": "string",
      "stateOrProvince": "string",
      "postalCode": "string",
      "country": "string"
    }
  }
}
```

---

### `GET /account/orders`
Get paginated order history for the authenticated user.

**Query Parameters**
| Param | Type | Required | Values |
|-------|------|----------|--------|
| `status` | string | ❌ | `PENDING_PAYMENT`, `PAID`, `FAILED`, `SHIPPED`, `DELIVERED`, `CANCELLED`, `REFUND_PENDING`, `REFUNDED`, `REFUND_FAILED` |
| `sort` | string | ❌ | `asc`, `desc` |
| `page` | integer | ❌ | |
| `pageSize` | integer | ❌ | |

**Response `200`** — `PagedResponseOrderDto` (see Orders section for `OrderDto` shape)

---

### `POST /account/reset-password`
Trigger a password reset email for the authenticated user.

**Response `200`** — empty body

---

## Products

### `GET /product`
Get a paginated list of products with optional filtering.

**Query Parameters**
| Param | Type | Required | Notes |
|-------|------|----------|-------|
| `page` | integer | ✅ | |
| `pageSize` | integer | ✅ | |
| `sort` | string | ✅ | `TOP`, `PRICE_ASC`, `PRICE_DESC` |
| `brands` | integer[] | ✅ | Can be empty array |
| `categoryId` | integer | ❌ | |
| `price.min` | number | ❌ | |
| `price.max` | number | ❌ | |
| `attributes` | map | ❌ | Key-value attribute filters e.g. `color=red` |

**Response `200`**
```json
{
  "content": [ ],
  "page": {
    "page": 0,
    "size": 20,
    "elements": 20,
    "totalElements": 100,
    "totalPages": 5,
    "isFirst": true,
    "isLast": false
  }
}
```

---

### `POST /product`
Create a new product with images.

**Request Body** (`multipart/form-data`)
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `product` | object | ✅ | See below |
| `images` | binary[] | ✅ | Image files |

**`product` object**
| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `name` | string | ✅ | min 1 char |
| `sku` | string | ✅ | min 1 char |
| `price` | number | ✅ | |
| `salePrice` | number | ✅ | |
| `stock` | integer | ✅ | |
| `isActive` | boolean | ✅ | |
| `description` | string | ❌ | |
| `brandId` | integer | ❌ | |
| `categoryId` | integer | ❌ | |
| `images` | ImageMetadata[] | ❌ | `{ fileName, sortOrder, altText? }` |
| `attributes` | ProductAttributeDto[] | ❌ | `{ name, values[] }` |

**Response `200`** — returns created `ProductDto`

---

### `GET /product/{id}`
Get a single product by ID.

**Path Parameters** — `id` (integer)

**Response `200`**
```json
{
  "id": 1,
  "name": "string",
  "description": "string",
  "sku": "string",
  "price": 99.99,
  "salePrice": 79.99,
  "stock": 50,
  "isActive": true,
  "brand": { "id": 1, "name": "string", "isActive": true },
  "category": { "id": 1 },
  "attributes": [ { "name": "color", "values": ["red"] } ],
  "images": [ { "id": 1, "url": "string", "sortOrder": 0, "altText": "string" } ]
}
```

---

### `PATCH /product/{id}`
Partially update a product and its images. All product fields are optional (JSON Nullable pattern — omit a field to leave it unchanged).

**Path Parameters** — `id` (integer)

**Request Body** (`multipart/form-data`)
| Field | Type | Required |
|-------|------|----------|
| `product` | EditProductDto | ✅ |
| `images` | binary[] | ✅ |
| `image_metadata` | ImageMetadata[] | ✅ |

**Response `200`** — returns updated `ProductDto`

---

### `DELETE /product/{id}`
Delete a product by ID.

**Response `200`** — empty body

---

### `GET /product/search`
Search products by keyword.

**Query Parameters**
| Param | Type | Required |
|-------|------|----------|
| `query` | string | ✅ |

**Response `200`** — `PagedResponseProductDto`

---

## Categories

### `POST /category`
Create a new category.

**Request Body**
| Field | Type | Required |
|-------|------|----------|
| `name` | string | ✅ |
| `attributeIds` | integer[] | ❌ |
| `parentId` | integer | ❌ |

**Response `200`** — returns `CategoryDto`

---

### `GET /category/{id}`
Get a category by ID, including its attributes and child category IDs.

**Response `200`**
```json
{
  "id": 1,
  "name": "string",
  "parentId": null,
  "childrenIds": [2, 3],
  "attributes": [ ]
}
```

---

### `PUT /category/{id}`
Replace a category's data.

**Request Body**
| Field | Type | Required |
|-------|------|----------|
| `id` | integer | ✅ |
| `name` | string | ✅ |
| `attributeIds` | integer[] | ❌ |
| `parentId` | integer | ❌ |

**Response `200`** — returns updated `CategoryDto`

---

### `DELETE /category/{id}`
Delete a category.

**Response `200`** — empty body

---

## Category Attributes

### `POST /category/attribute`
Create a new category attribute.

**Request Body**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `attribute` | string | ✅ | Internal key name |
| `label` | string | ✅ | Display label |
| `dataType` | string | ✅ | `TEXT`, `NUMBER`, `BOOLEAN` |
| `filterType` | string | ✅ | `CHECKBOX`, `SLIDER`, `DROPDOWN` |
| `displayOrder` | integer | ✅ | |
| `isPublic` | boolean | ✅ | |
| `unit` | string | ❌ | e.g. `kg`, `cm` |
| `categories` | integer[] | ❌ | Category IDs to associate |

**Response `200`** — returns `CategoryAttributeDto`

---

### `GET /category/attribute/{id}`
Get a category attribute by ID.

**Response `200`** — returns `CategoryAttributeDto`

---

### `PUT /category/attribute/{id}`
Update a category attribute. Accepts same fields as `POST`.

**Response `200`** — returns updated `CategoryAttributeDto`

---

### `DELETE /category/attribute/{id}`
Delete a category attribute.

**Response `200`** — empty body

---

## Brands

### `POST /brand`
Create a new brand.

**Request Body**
| Field | Type | Required |
|-------|------|----------|
| `name` | string | ✅ |
| `isActive` | boolean | ✅ |
| `image` | integer | ❌ | Image ID |

**Response `200`** — returns `BrandDto`

---

### `GET /brand/{id}`
Get a brand by ID.

**Response `200`**
```json
{
  "id": 1,
  "name": "string",
  "isActive": true,
  "image": { "id": 1, "url": "string", "sortOrder": 0, "altText": "string" }
}
```

---

### `PUT /brand/{id}`
Update a brand.

**Request Body**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `id` | integer | ✅ | |
| `name` | string | ✅ | |
| `isActive` | boolean | ✅ | |
| `image` | integer | ❌ | Image ID |

**Response `200`** — returns updated `BrandDto`

---

### `DELETE /brand/{id}`
Delete a brand.

**Response `200`** — empty body

---

### `GET /brand/{id}/image`
Stream the brand's image as binary.

**Response `200`** — binary image data

---

## Filters

### `GET /filter`
Get all available filter options for a given category (attributes, brands, price range).

**Query Parameters**
| Param | Type | Required |
|-------|------|----------|
| `categoryId` | integer | ✅ |

**Response `200`**
```json
{
  "categoryId": 1,
  "price": { "min": 0.0, "max": 999.99 },
  "brands": [
    { "id": 1, "name": "string", "count": 12 }
  ],
  "attributes": [
    {
      "id": 1,
      "attribute": "color",
      "label": "Color",
      "filterType": "CHECKBOX",
      "displayOrder": 1,
      "values": [ ]
    }
  ]
}
```

---

## Cart

> Cart sessions are tracked via the optional `X-Session-Id` header for guest carts. Omit for authenticated users.

### `GET /cart`
Retrieve the current cart.

**Headers**
| Header | Required |
|--------|----------|
| `X-Session-Id` | ❌ |

**Response `200`**
```json
{
  "id": "uuid",
  "sessionId": "string",
  "shippingType": "STANDARD",
  "shippingCost": 5.99,
  "subTotalPrice": 49.99,
  "totalPrice": 55.98,
  "totalQuantity": 2,
  "items": [
    {
      "productId": 1,
      "productName": "string",
      "imageUrl": "string",
      "price": 24.99,
      "quantity": 2
    }
  ]
}
```

---

### `PUT /cart`
Update cart items and/or shipping type.

**Headers**
| Header | Required |
|--------|----------|
| `X-Session-Id` | ❌ |

**Request Body**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `items` | ItemUpdateRequest[] | ❌ | `{ productId, quantity }` — set `quantity: 0` to remove |
| `shippingType` | string | ❌ | `STANDARD`, `EXPRESS` |

**Response `200`** — returns updated `CartDto`

---

### `POST /cart/checkout`
Initiate Stripe checkout for the current cart. Returns a `clientSecret` to complete payment on the frontend.

**Headers**
| Header | Required |
|--------|----------|
| `X-Session-Id` | ❌ |

**Response `200`**
```json
{ "clientSecret": "string" }
```

---

## Orders

### `POST /order/submit`
Submit an order with contact and shipping details.

**Headers**
| Header | Required |
|--------|----------|
| `X-Session-Id` | ❌ |

**Request Body**
| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `name` | string | ✅ | min 1 char |
| `phone` | string | ✅ | min 1 char |
| `email` | string (email) | ✅ | min 1 char |
| `shippingType` | string | ✅ | `STANDARD`, `EXPRESS` |
| `address` | object | ✅ | See below |
| `notes` | string | ❌ | |

**`address` fields**
| Field | Type | Required |
|-------|------|----------|
| `streetLine1` | string | ✅ |
| `city` | string | ✅ |
| `postalCode` | string | ✅ |
| `country` | string | ✅ |
| `streetLine2` | string | ❌ |
| `stateOrProvince` | string | ❌ |

**Response `200`** — returns `OrderDto`

---

### `GET /order`
Look up an order by Stripe payment intent ID.

**Query Parameters**
| Param | Type | Required |
|-------|------|----------|
| `paymentIntentId` | string | ❌ |

**Response `200`**
```json
{
  "id": "uuid",
  "status": "PAID",
  "paymentId": "string",
  "sessionId": "string",
  "shippingType": "STANDARD",
  "shippingCost": 5.99,
  "totalAmount": 55.98,
  "date": 1713000000000,
  "notes": "string",
  "items": [
    {
      "productId": 1,
      "productName": "string",
      "imageUrl": "string",
      "price": 24.99,
      "quantity": 2,
      "totalAmount": 49.98
    }
  ],
  "contact": {
    "name": "string",
    "email": "string",
    "phone": "string",
    "address": { "streetLine1": "string", "city": "string", "country": "string" }
  }
}
```

**Order statuses:** `PENDING_PAYMENT` · `PAID` · `FAILED` · `SHIPPED` · `DELIVERED` · `CANCELLED` · `REFUND_PENDING` · `REFUNDED` · `REFUND_FAILED`

---

## Payments

### `POST /payment/refund`
Request a refund for an existing order.

**Headers**
| Header | Required |
|--------|----------|
| `X-Session-Id` | ❌ |

**Request Body**
| Field | Type | Required |
|-------|------|----------|
| `orderId` | string (uuid) | ✅ |
| `reason` | string | ❌ |

**Response `200`** — empty body

---

### `POST /payment/webhook/stripe`
Stripe webhook receiver. Should only be called by Stripe.

**Headers**
| Header | Required |
|--------|----------|
| `Stripe-Signature` | ✅ |

**Request Body** — raw Stripe event payload (string)

**Response `200`** — empty body