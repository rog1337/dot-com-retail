## Shopping app backend


- [Requirements](#requirements)
- [Tech stack](#tech-stack)
- [Project overview](#project-overview)
- [Entity Relationship Diagram](#entity-relationship-diagram)
- [Setup](#setup)
- [Endpoints](./endpoints.md)
- [File storage](#file-storage)


## Tech stack

### Backend

|                       |                   |
|-----------------------|-------------------|
| Language              | Kotlin 2.0.20     |
| Framework             | Spring Boot 3.5.6 |
| Build tool            | Gradle            |
| Database              | PostgreSQL 18     |
| Cache / session store | Redis             |
| Message broker        | RabbitMQ          |
| Payments              | Stripe            |
| Containerization      | Docker            |

### Frontend
|                 |                         |
|-----------------|-------------------------|
| Framework       | Next.js 15 (App Router) |
| Language        | TypeScript              |
| Styling         | Tailwind CSS            |
| Package manager | pnpm                    |

## Project overview


Backend for an e‑commerce catalogue focused on selling car tyres, 
but can support a wide range of products.

It provides:

### Product catalogue

- Products with schemaless **PostgreSQL**'s jsonb attribute model.
- Categories with attributes for providing filters/facets, stored in an EAV model.
- Images for products and brands.

### Cart
- Guest cart identified by a `X-Session-Id` header, persisted temporarily in the database
- Persistent cart for logged-in users, retained across sessions
- Real-time total calculations including shipping cost
- Pessimistic locking on cart reads to prevent race conditions at checkout
- Stock validation on every cart interaction

### Checkout & payments
- Single-page checkout collecting contact details, shipping address, and shipping type
- Stripe Elements for PCI-compliant card input — no card data touches the server
- Contact details are encrypted on rest
- Payment related functionality is moved to a RabbitMQ message queue.
- Abandoned orders (stuck in `PENDING_PAYMENT` for over 1 hour) are automatically cancelled by a scheduled cleanup job

### Email notifications
- Password reset
- Order confirmation and refunds
- Emails are sent asynchronously

### Concurrency & stock
- Optimistic locking on products to prevent overselling
- If two payments succeed for the last item simultaneously, the second order is automatically refunded and cancelled

### Authentication & security

- Registration with **Cloudflare Turnstile** CAPTCHA and login 
via **email/password** or **OAuth2** (Google)

- **JWT** access tokens in Authorization header + refresh tokens in HTTP‑only cookie. Both are almost
 stateless with token versioning implemented in **Redis** for token revocation on demand

- Optional user enabled **Two‑factor authentication (2FA)**

- Password reset with email verification

- Spring Security–based configuration and route protection

### Entity Relationship Diagram

![Entity Relationship Diagram](./database-erd.png)

## Setup

### Requirements
- Docker
- [Stripe CLI](https://stripe.com/docs/stripe-cli) (for local payment simulation)


### 1. Clone the repository

```
git clone https://gitea.kood.tech/romangadjak/i-love-shopping1.git dot-com-retail
cd dot-com-retail
```


### 2. Configure environment variables / secrets

Copy `sample.env` to `.env` and fill in the values:

```bash
cp sample.env .env
```

| Variable                | Required                                                                                                                                                    | Description |
|-------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------|
| `UPLOAD_PATH`           | File upload directory. Default works out of the box.                                                                                                        |
| `JWT_SECRET`            | Pre-defined in sample.env.                                                                                                                                  |
| `GOOGLE_CLIENT_ID`      | OAuth2 login via Google.                                                                                                                                    |
| `GOOGLE_CLIENT_SECRET`  | OAuth2 login via Google.                                                                                                                                    |
| `MAIL_USERNAME`         | Gmail address for sending emails.                                                                                                                           |
| `MAIL_PASSWORD`         | Gmail [app password](https://support.google.com/accounts/answer/185833). Your regular password won't work — Google requires 2FA + a generated app password. |
| `TURNSTILE_SECRET_KEY`  | Cloudflare Turnstile CAPTCHA for registration. `sample.env` includes dummy keys that always pass or always fail.                                            |
| `STRIPE_SECRET_KEY`     | Stripe secret key from your [Stripe dashboard](https://dashboard.stripe.com/apikeys).                                                                       |
| `STRIPE_WEBHOOK_SECRET` | Webhook signing secret. Run `stripe listen --forward-to localhost:8080/api/v1/payments/webhook/stripe` to get it during development.                        |

For frontend copy `.env.example` to `.env.local`:

```bash
cp sample.env .env
```

| Variable                             | Description                                                                                 |
|--------------------------------------|---------------------------------------------------------------------------------------------|
| `NEXT_PUBLIC_TURNSTILE_SITE_KEY`     | Cloudflare Turnstile CAPTCHA for registration. Contains dummy keys that always pass or fail |
| `NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY` | Stripe publishable key from Stripe dashboard                                                |

### Run with Docker

### 3. Build and run the container
```
docker compose up
```

#### For development run only Postgres, Redis and RabbitMQ in docker

``docker compose -f docker-compose-dev.yml up -d``

Then backend and frontend

``./gradlew bootRun``

```
cd frontend
pnpm dev
```

Spring starts on **port 8080**, Next.js on **port 3000** by default.

### 4. Stripe webhook

Install the [Stripe CLI](https://stripe.com/docs/stripe-cli) and forward events to your local server:

```bash
stripe listen --forward-to localhost:8080/api/payments/webhook
```

Copy the webhook signing secret it outputs into your `.env` as `STRIPE_WEBHOOK_SECRET`.


### Stopping
``docker compose down``


## File storage

Configured under `file.*` in `application.yml`:

| Path                           | Contents       |
|--------------------------------|----------------|
| `{UPLOAD_PATH}/images/product` | Product images |
| `{UPLOAD_PATH}/images/brand`   | Brand images   |
 
---

## Endpoints

See [endpoints.md](./endpoints.md) for the full API reference.

