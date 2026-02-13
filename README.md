## Shopping app backend


- [Requirements](#requirements)
- [Tech stack](#tech-stack)
- [Project overview](#project-overview)
- [Entity Relationship Diagram](#entity-relationship-diagram)
- [Setup](#setup)
- [Configuration](#configuration)
- [API](#api)

---

## Requirements

- Docker

---


## Tech stack

- **Containerization**: Docker
- **Language**: Kotlin 2.0.20
- **Framework**: Spring Boot 3.5.6
- **Build tool**: Gradle
- **Database**: PostgreSQL 18
- **Cache / store**: Redis
- **Auth**:
  - JWT via `jjwt`
  - Spring Security OAuth2 client (Google)
  - 2FA (TOTP) via `dev.samstevens.totp`

## Project overview

---

Backend for an e‑commerce catalogue focused on selling car tyres, 
but can support a wide range of products.

It provides:

#### Product catalogue

---

- Products with schemaless **PostgreSQL**'s jsonb attribute model.
- Categories with attributes for providing filters/facets, stored in an EAV model.
- Images for products and brands.

#### Authentication & security

---

- Registration with **Captcha** (Cloudflare Turnstile) integration and login 
via **email/password** or **OAuth2** (Google)

- **JWT** access tokens in Authorization header + refresh tokens in HTTP‑only cookie. Both are almost
 stateless with token versioning implemented in **Redis** for token revocation on demand

- Optional user enabled **Two‑factor authentication (2FA)**

- Password reset with email verification

- Spring Security–based configuration and route protection


#### Infrastructure & tooling

---

- PostgreSQL as the primary database
- Redis for caching / token versioning
- Database seeding via `DatabaseInitializer` for product catalogue
- Email sending via SMTP (Spring Mail)
- Cloudflare Turnstile CAPTCHA integration
- dev.samstevens.totp to generate and validate TOTP codes for 2FA.

The backend is designed to sit behind a separate frontend which is going to be implemented
in the next task and exposes its API under `/api/v1/**`.

---

### Entity Relationship Diagram

![Entity Relationship Diagram](./shopping_erd.png)

---

### Setup
#### 1. Clone the repository

```
git clone https://gitea.kood.tech/romangadjak/i-love-shopping1.git dot-com-retail
cd dot-com-retail
```

---

#### 2. Configure environment variables / secrets

Copy the contents of `sample.env` to `.env` and edit them there

#### Image storage
 - `UPLOAD_PATH`

Default should work.

**JWT**
  - `JWT_SECRET` - already pre-defined

**OAuth2**
  - `GOOGLE_CLIENT_ID`
  - `GOOGLE_CLIENT_SECRET`

This is used for authentication via Google's OAuth2. Can be be skipped if you want.

**Mail**
  - `MAIL_USERNAME` Gmail username
  - `MAIL_PASSWORD` Gmail password

This is used only for sending password reset emails, so you can skip it if you want.
You can't use your real password. Google requires you to enable 2FA and generate a separate password.

**Cloudflare Turnstile (captcha)**
  - `TURNSTILE_SECRET_KEY`

Captcha is only used for registering new user accounts.
You can skip this if you want, the .env file contains 2 dummy keys:

1) always pass
2) always fail

#### docker compose

You can edit environment variables per container for PostgresSQL and Redis in `docker-compose-dev.yml` and the backend in `docker-compose.yml`

---

#### 3. Build and run the container
```
docker compose up
```

#### For development run only Postgres and Redis

``docker compose -f docker-compose-dev.yml up -d``

And backend
``./gradlew bootRun``

or from your IDE (import the ``.env`` file)

Spring starts on **port 8080** by default.

---


#### Stopping
``docker compose down``

#### 4. Build and run the backend

From the `backend` directory:

```bash
cd backend
./gradlew clean build
./gradlew bootRun
```

---

### Endpoints

#### Authentication

Base path: `POST /api/v1/auth`

- #### Register
    - `POST /api/v1/auth/register`
    - Request body: `RegisterRequest` (email, password, captcha token).
    - Response: `201 Created` with a JWT access token and user DTO; refresh token is set as an HTTP‑only cookie.

- #### Login
    - `POST /api/v1/auth/login`
    - Request body: `LoginRequest` (email + password).
    - Behaviours:
        - On success: returns `200 OK` with access token and user DTO and sets refresh cookie.
        - When 2FA is required: returns `202 Accepted` indicating that a second factor is needed.

- #### Token refresh
    - `GET /api/v1/auth/refresh`
    - Uses the refresh token cookie to issue new tokens and invalidates old ones.
    - Updates refresh cookie header and returns access token in the body.

- #### Logout
    - `GET /api/v1/auth/logout`

- #### Password reset
    - `POST /api/v1/auth/reset-password`
    - Initiates password reset via `PasswordResetService` and sends an email.
    - `POST /api/v1/auth/reset-password-verify`
    - Verifies the reset token/code and changes the password.

#### Two‑factor authentication (2FA)

Base path: `/api/v1/2fa`

- #### Setup
  - `POST /api/v1/2fa/setup`
  - Generates and respond with 2FA token and a QR-code.
    
- #### Verify
  - `POST /api/v1/2fa/setup`
  - RequestBody: 2FA code
  - Finishes 2FA setup for the user.

- #### Disable
  - `POST /api/v1/2fa/disable`
  - RequestBody: 2FA code
  - Disables 2FA for the user.

#### OAuth2 login

Base path: `/api/v1/oauth2`

- #### Login
  - `/api/v1/oauth2/authorize/google`

- #### Redirect URI:
    - `http://localhost:8080/api/v1/oauth2/code/google`
    - 
- Success and failure are handled via custom `OAuth2SuccessHandler` and `OAuth2FailureHandler`; user information is then bound into the server and JWTs are issued.

#### Product catalogue

Base path: `/api/v1/product`

- #### Find products with filters
    - `GET /api/v1/product`
    - Accepts `ProductQueryParams` (filters like brand, category, attributes, etc).
    - Returns `PagedResponse<ProductDto>`.

- #### Get product by ID
    - `GET /api/v1/product/{id}`
    - Returns a single `ProductDto`.

- #### Create product
    - `POST /api/v1/product`
    - `multipart/form-data` with:
        - `product`: JSON body `CreateProduct`.
        - `images`: one or more `MultipartFile`s.
    - Returns `201 Created` with `ProductDto`.

- #### Edit product
    - `PUT /api/v1/product/{id}`
    - Request body: `multipart/form-data` with:
        - `product`: JSON body `EditProductDto`.
        - `images`: one or more `MultipartFile`s.

- #### Get product image
    - `GET /api/v1/product/{productId}/image/{imageId}`
    - Returns the image Resource.

- #### Search products
    - `GET /api/v1/product/search?query={textQuery}`
    - Returns paged products with the text filter

#### Filters
Base path: `/api/v1/filter`

- #### Get filters
  - `GET /api/v1/filter?categoryId={categoryId}`
    - Responds with all pre-defined filters + product data for requested category

    
#### Brands, categories

- #### Brands
Base path: `/api/v1/brand`

- Typical CRUD and image management endpoints (see `BrandController` for exact methods).

- #### Categories
Base path: `/api/v1/category`

- Typical CRUD management endpoints (see `CategoryController` for exact methods).

---

### File storage

Configured under `file.*` properties in `application.yml`:

- Base uploads directory: `{UPLOAD_PATH}`, calculated at runtime to support different environments
- Product images: `${uploads}/images/product`
- Brand images: `${uploads}/images/brand`

