
# Multi-Tenant Webhook & Event Delivery Engine

Tests retries, state transitions, idempotency, and scheduling

## Problem Statement: 
Build a Spring Boot service where external tenants register webhook URLs to receive real-time payment events (e.g., INVOICE_PAID, PAYMENT_FAILED), with reliable delivery tracking and retry handling.

## Requirements:

- Tenants will register through a webhook, a rest endpoint on tenant side. Assuming webhooks need no auth and http only.
- Events are happening outside our system and we will receive those envents in our system
- We will identify event by merchant as belonging to which tenant
- We will do retry with webhook invocation for event notification
- We will keep audit trail of webhook invocations

## Design:

- Add a /v1/register rest endpoint in our service with request as:
    - url, retries_no, merchant_name
    - store in tentants table
- Add a /v1/payment rest endpoint which event processing platforms will invoke
    - request : payment_id (idempotency key), amount, date, invoice_id, merchant, event_type
    - store in events table
- Store following tables in h2 in-memory db
    - events : event_id, payment_id (idempotency key), amount, date, invoice_id, merchant, event_type
    - event_deliveries : event_id, retry_no, delivery_status, date_added
    - tenants : tenant_id, url, retries_no, merchant, date_added
- event_type = FULLY_PAID, PAYMENT_FAILED, PARTIALLY_PAID
- delivery_status = delivered, retrying, failed


## Application Details:

- Spring Boot, Maven and Java application
- /payment will receive one event at a time and webhook call will be made per event
- A thin client to invoke /payment api to store events
- A tenant webhook to receive events and send response randomly = {success, failure}


## Verification Scenarios:

- events are delivered in real-time succesfully
- event delivery fails and reties are done and then eventual success
- event deliverey fails after all retries