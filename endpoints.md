# API Endpoints

---

## Table of Contents

- [Authentication](#authentication)
- [Two-Factor Authentication](#two-factor-authentication)
- [Products](#products)
- [Categories](#categories)
- [Category Attributes](#category-attributes)
- [Brands](#brands)
- [Filters](#filters)
- [Misc](#misc)
- [Data Models](#data-models)

---

## Authentication

### `POST /api/v1/auth/register`
Register a new user account.

**Request Body** `application/json`
```json
{
  "email": "string",         // required, valid email
  "password": "string",      // required, 5–100 chars
  "displayName": "string",   // required, 1–30 chars
  "captchaToken": "string"   // required
}
```

**Response** `200 OK`
```json
{
  "accessToken": "string",
  "user": { "id": "string", "email": "string", "displayName": "string" }
}
```

---

### `POST /api/v1/auth/login`
Authenticate a user.

**Request Body** `application/json`
```json
{
  "email": "string",         // required
  "password": "string",      // required
  "twoFactorCode": "string"  // optional
}
```

**Response** `200 OK` — Returns user session object.

---

### `GET /api/v1/auth/refresh`
Refresh the access token using the refresh cookie.

**Response** `200 OK`
```json
{
  "accessToken": "string"
}
```

---

### `GET /api/v1/auth/logout`
Log the current user out.

**Response** `200 OK`

---

### `POST /api/v1/auth/reset-password`
Initiate a password reset. Sends a reset email.

**Request Body** `application/json`
```json
{
  "email": "string"  // required
}
```

**Response** `200 OK`

---

### `POST /api/v1/auth/reset-password-verify`
Complete a password reset using a token.

**Request Body** `application/json`
```json
{
  "token": "string",     // required
  "password": "string"   // required
}
```

**Response** `200 OK`

---

## Two-Factor Authentication

### `POST /api/v1/2fa/setup`
Generate a 2FA secret and QR code for the authenticated user.

**Response** `200 OK`
```json
{
  "secret": "string",
  "qrCode": "string"
}
```

---

### `POST /api/v1/2fa/verify`
Verify and enable 2FA using a TOTP code.

**Request Body** `application/json`
```json
{
  "code": "string"  // required
}
```

**Response** `200 OK`

---

### `POST /api/v1/2fa/disable`
Disable 2FA for the authenticated user.

**Response** `200 OK`

---

## Products

### `GET /api/v1/product`
Get a paginated list of products with filtering and sorting.

**Query Parameters** — `params` (object)

| Field | Type | Required | Description |
|---|---|---|---|
| `categoryId` | `Long` | No | Filter by category |
| `brands` | `Long[]` | Yes | Filter by brand IDs |
| `attributes` | `ProductAttributeDto[]` | No | Filter by attributes |
| `page` | `Int` | Yes | Page number |
| `pageSize` | `Int` | Yes | Items per page |
| `sort` | `String` | Yes | `TOP` \| `PRICE_ASC` \| `PRICE_DESC` |
| `price` | `RangeData` | No | Min/max price filter |

**Response** `200 OK` — `PagedResponseProductDto`

---

### `POST /api/v1/product`
Create a new product.

**Request Body** `application/json`

| Field | Type | Required |
|---|---|---|
| `product` | `CreateProduct` | Yes |
| `images` | `binary[]` | Yes |

**`CreateProduct` fields:**

| Field | Type | Required |
|---|---|---|
| `name` | `String` | Yes |
| `sku` | `String` | Yes |
| `description` | `String` | No |
| `price` | `Number` | Yes |
| `salePrice` | `Number` | Yes |
| `stock` | `Int` | Yes |
| `brandId` | `Long` | No |
| `categoryId` | `Long` | No |
| `images` | `ImageMetadata[]` | No |
| `attributes` | `ProductAttributeDto[]` | No |
| `isActive` | `Boolean` | Yes |

**Response** `200 OK` — `ProductDto`

---

### `GET /api/v1/product/{id}`
Get a single product by ID.

**Path Parameters:** `id` (Long) — required

**Response** `200 OK` — `ProductDto`

---

### `PUT /api/v1/product/{id}`
Update a product by ID.

**Path Parameters:** `id` (Long) — required

**Request Body** `multipart/form-data`

| Field | Type | Required |
|---|---|---|
| `product` | `EditProductDto` | Yes |
| `images` | `binary[]` | Yes |

**Response** `200 OK` — `ProductDto`

---

### `DELETE /api/v1/product/{id}`
Delete a product by ID.

**Path Parameters:** `id` (Long) — required

**Response** `200 OK`

---

### `GET /api/v1/product/search`
Search products by query string.

**Query Parameters:**

| Field | Type | Required |
|---|---|---|
| `query` | `String` | Yes |

**Response** `200 OK` — `PagedResponseProductDto`

---

### `GET /api/v1/product/{productId}/image/{imageId}`
Retrieve a specific product image.

**Path Parameters:** `productId` (Long), `imageId` (Long) — both required

**Response** `200 OK` — Binary image data

---

## Categories

### `POST /api/v1/category`
Create a new category.

**Request Body** `application/json`
```json
{
  "name": "string",          // required
  "attributeIds": [0],       // optional, Long[]
  "parentId": 0              // optional
}
```

**Response** `200 OK` — `CategoryDto`

---

### `GET /api/v1/category/{id}`
Get a category by ID.

**Path Parameters:** `id` (Long) — required

**Response** `200 OK` — `CategoryDto`

---

### `PUT /api/v1/category/{id}`
Update a category by ID.

**Path Parameters:** `id` (Long) — required

**Request Body** `application/json`
```json
{
  "id": 0,                   // required
  "name": "string",          // required
  "attributeIds": [0],       // optional, Long[]
  "parentId": 0              // optional
}
```

**Response** `200 OK` — `CategoryDto`

---

### `DELETE /api/v1/category/{id}`
Delete a category by ID.

**Path Parameters:** `id` (Long) — required

**Response** `200 OK`

---

## Category Attributes

### `POST /api/v1/category/attribute`
Create a new category attribute.

**Request Body** `application/json`

| Field          | Type      | Required | Notes                                |
|----------------|-----------|----------|--------------------------------------|
| `attribute`    | `String`  | Yes      |                                      |
| `label`        | `String`  | Yes      |                                      |
| `unit`         | `String`  | No       |                                      |
| `dataType`     | `String`  | Yes      | `TEXT` \| `NUMBER` \| `BOOLEAN`      |
| `filterType`   | `String`  | Yes      | `CHECKBOX` \| `DROPDOWN` \| `SLIDER` |
| `displayOrder` | `Int`     | Yes      |                                      |
| `isPublic`     | `Boolean` | Yes      |                                      |
| `categories`   | `Long[]`  | No       |                                      |

**Response** `200 OK` — `CategoryAttributeDto`

---

### `GET /api/v1/category/attribute/{id}`
Get a category attribute by ID.

**Path Parameters:** `id` (Long) — required

**Response** `200 OK` — `CategoryAttributeDto`

---

### `PUT /api/v1/category/attribute/{id}`
Update a category attribute.

**Request Body** `application/json` — Same fields as create, plus required `id` (Long).

**Response** `200 OK` — `CategoryAttributeDto`

---

### `DELETE /api/v1/category/attribute/{id}`
Delete a category attribute by ID.

**Path Parameters:** `id` (Long) — required

**Response** `200 OK`

---

## Brands

### `POST /api/v1/brand`
Create a new brand.

**Request Body** `application/json`
```json
{
  "name": "string",    // required
  "image": 0,          // optional, image ID (Long)
  "isActive": true     // required
}
```

**Response** `200 OK` — `BrandDto`

---

### `GET /api/v1/brand/{id}`
Get a brand by ID.

**Path Parameters:** `id` (Long) — required

**Response** `200 OK` — `BrandDto`

---

### `PUT /api/v1/brand/{id}`
Update a brand by ID.

**Request Body** `application/json`
```json
{
  "id": 0,             // required
  "name": "string",    // required
  "image": 0,          // optional, image ID (Long)
  "isActive": true     // required
}
```

**Response** `200 OK` — `BrandDto`

---

### `DELETE /api/v1/brand/{id}`
Delete a brand by ID.

**Path Parameters:** `id` (Long) — required

**Response** `200 OK`

---

### `GET /api/v1/brand/{id}/image`
Retrieve the image for a brand.

**Path Parameters:** `id` (Long) — required

**Response** `200 OK` — Binary image data

---

## Filters

### `GET /api/v1/filter`
Get available filter options for a category.

**Query Parameters:**

| Field | Type | Required |
|---|---|---|
| `categoryId` | `Long` | Yes |

**Response** `200 OK` — `Filter`

```json
{
  "categoryId": 0,
  "attributes": [ /* FilterAttribute[] */ ],
  "brands": [ /* ProductBrandCount[] */ ],
  "price": { "min": 0.0, "max": 0.0 }
}
```

---

## Misc

### `GET /`
Health check / home.

**Response** `200 OK` — `String`

---

### `GET /info`
Server info.

**Response** `200 OK` — Object

---

### `POST /test`
Dev test endpoint.

**Request Body** `application/json`
```json
{
  "test": "string"  // required, 5–10 chars
}
```

**Response** `200 OK` — `String`

---

## Data Models

### `ProductDto`
| Field | Type | Required |
|---|---|---|
| `id` | `Long` | Yes |
| `name` | `String` | Yes |
| `description` | `String` | No |
| `sku` | `String` | Yes |
| `price` | `Number` | Yes |
| `salePrice` | `Number` | Yes |
| `stock` | `Int` | Yes |
| `brand` | `BrandDto` | No |
| `category` | `CategoryDto` | No |
| `attributes` | `ProductAttributeDto[]` | No |
| `images` | `ImageDto[]` | No |
| `isActive` | `Boolean` | Yes |

### `CategoryDto`
| Field | Type | Required |
|---|---|---|
| `id` | `Long` | Yes |
| `name` | `String` | Yes |
| `attributes` | `CategoryAttributeDto[]` | Yes |
| `childrenIds` | `Long[]` | No |
| `parentId` | `Long` | No |

### `CategoryAttributeDto`
| Field          | Type                             | Required |
|----------------|----------------------------------|----------|
| `id`           | `Long`                           | Yes      |
| `attribute`    | `String`                         | Yes      |
| `label`        | `String`                         | Yes      |
| `unit`         | `String`                         | No       |
| `dataType`     | `TEXT \| NUMBER \| BOOLEAN`      | Yes      |
| `filterType`   | `CHECKBOX \| DROPDOWN \| SLIDER` | Yes      |
| `displayOrder` | `Int`                            | Yes      |
| `isPublic`     | `Boolean`                        | Yes      |
| `categories`   | `Long[]`                         | No       |

### `BrandDto`
| Field | Type | Required |
|---|---|---|
| `id` | `Long` | Yes |
| `name` | `String` | Yes |
| `image` | `ImageDto` | No |
| `isActive` | `Boolean` | Yes |

### `ImageDto`
| Field | Type | Required |
|---|---|---|
| `id` | `Long` | Yes |
| `url` | `String` | Yes |
| `sortOrder` | `Int` | Yes |
| `altText` | `String` | No |

### `PagedResponseProductDto`
| Field | Type |
|---|---|
| `content` | `ProductDto[]` |
| `page` | `PageDto` |

### `PageDto`
| Field | Type |
|---|---|
| `page` | `Int` |
| `size` | `Int` |
| `elements` | `Int` |
| `totalElements` | `Long` |
| `totalPages` | `Int` |
| `isLast` | `Boolean` |
| `isFirst` | `Boolean` |

### `UserDto`
| Field | Type |
|---|---|
| `id` | `String` |
| `email` | `String` |
| `displayName` | `String` |