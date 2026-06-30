# Multi-Carrier API Integration Checklist
**Carriers:** FedEx (REST) · ShipHawk · Canada Post (REST)  
**Objective:** Technical discovery matrix for integrating FedEx, ShipHawk, and Canada Post into a unified shipping aggregator.

---

## 1. Authentication

| # | Attribute | FedEx | ShipHawk | Canada Post |
|---|-----------|-------|----------|-------------|
| 1 | Auth Type | OAuth 2.0 (Bearer Token) | Static API Key | HTTP Basic Auth (Base64) |
| 2 | Credentials | `client_id` + `client_secret` | `api_key` | `username` + `password` |
| 3 | Token Endpoint | `POST /oauth/token` | N/A | N/A |
| 4 | Request Header | `Authorization: Bearer <token>` | `X-Api-Key: <key>` | `Authorization: Basic <base64(user:pass)>` |
| 5 | Token Expiry | 3600 s (1 hour) | No expiry | No expiry |
| 6 | Refresh Flow | New `POST /oauth/token`; no refresh_token grant | Not applicable | Not applicable |
| 7 | Sandbox URL | `apis-sandbox.fedex.com` | `sandbox.shiphawk.com` | `ct.soa-gw.canadapost.ca` |
| 8 | Credential Source | FedEx Developer Portal → Create Project | ShipHawk Portal → Developer Settings | Canada Post Developer Program |
| 9 | Multi-tenant / 3PL | `child_key` + `child_secret` for CSP accounts | Per-tenant accounts configured in portal | `mailed-by` / `mobo` customer numbers in URL |
| 10 | TLS Required | Yes (TLS 1.2+) | Yes | Yes |

**Aggregator Note:** Cache FedEx token; refresh 5 min before expiry. ShipHawk and Canada Post are stateless per-request.

---

## 2. Endpoints & Communication Protocol

| # | Attribute | FedEx | ShipHawk | Canada Post |
|---|-----------|-------|----------|-------------|
| 1 | Production Base URL | `https://apis.fedex.com` | `https://shiphawk.com/api/v4` | `https://soa-gw.canadapost.ca/rs` |
| 2 | Sandbox Base URL | `https://apis-sandbox.fedex.com` | `https://sandbox.shiphawk.com/api/v4` | `https://ct.soa-gw.canadapost.ca/rs` |
| 3 | API Style | REST / JSON | REST / JSON | REST / XML (SOAP 1.1 also available) |
| 4 | Auth Endpoint | `POST /oauth/token` | N/A | N/A |
| 5 | Rate Endpoint | `POST /rate/v1/rates/quotes` | `POST /api/v4/rates` | `POST /rs/{cust}/{mobo}/service` |
| 6 | Create Shipment | `POST /ship/v1/shipments` | `POST /api/v4/shipments` | `POST /rs/{cust}/{mobo}/shipment` |
| 7 | Cancel Shipment | `PUT /ship/v1/shipments/{id}/cancel` | `DELETE /api/v4/shipments/{id}` | `POST /rs/{cust}/{mobo}/shipment/{id}/void` |
| 8 | Track Shipment | `POST /track/v1/trackingnumbers` | `GET /api/v4/shipments/{id}/track` | `GET /rs/track/ncshippinginquiry/{pin}` |
| 9 | Address Validation | `POST /address/v1/addresses/resolve` | `POST /api/v4/addresses/validate` | Not available (use Address Complete API) |
| 10 | Content-Type (Request) | `application/json` | `application/json` | `application/vnd.cpc.ship.rate-v4+xml` |
| 11 | Accept Header | `application/json` | `application/json` | `application/vnd.cpc.ship.rate-v4+xml` (PDF for label fetch) |
| 12 | API Versioning | URL path (`/v1/`) | URL path (`/v4/`) | Media-type versioning (`vnd.cpc.*-v4`) |
| 13 | Webhook Support | Not available | Yes (tracking, shipment status) | Not available (polling only) |
| 14 | SOAP Support | Deprecated (use REST) | Not offered | Yes (SOAP 1.1) |

---

## 3. Rate Request Schema

| # | Field | FedEx (JSON) | ShipHawk (JSON) | Canada Post (XML) | Required | Normalised Field |
|---|-------|-------------|-----------------|-------------------|----------|-----------------|
| 1 | Origin Postal Code | `shipper.address.postalCode` | `origin_zip` | `<origin-postal-code>` | ✔ | `origin.postalCode` |
| 2 | Origin Country | `shipper.address.countryCode` | `origin_country` | Inferred (CA) | ✔ | `origin.countryCode` |
| 3 | Origin City | `shipper.address.city` | `origin_city` | Not required | Optional | `origin.city` |
| 4 | Origin State | `shipper.address.stateOrProvinceCode` | `origin_state` | Not required | Optional | `origin.state` |
| 5 | Dest Postal Code | `recipient.address.postalCode` | `destination_zip` | `<postal-code>` | ✔ | `destination.postalCode` |
| 6 | Dest Country | `recipient.address.countryCode` | `destination_country` | `<country-code>` | ✔ | `destination.countryCode` |
| 7 | Residential Flag | `recipient.address.residential` | `residential` (bool) | Not supported | Optional | `destination.isResidential` |
| 8 | Weight Value | `weight.value` | `items[].weight` | `<weight>` | ✔ | `packages[].weight.value` |
| 9 | Weight Unit | `weight.units` (LB/KG) | `weight_unit` (lbs) | KG only (fixed) | ✔ | `packages[].weight.unit` |
| 10 | Length | `dimensions.length` | `items[].length` (inches) | `<length>` (cm) | Optional | `packages[].dimensions.length` |
| 11 | Width | `dimensions.width` | `items[].width` (inches) | `<width>` (cm) | Optional | `packages[].dimensions.width` |
| 12 | Height | `dimensions.height` | `items[].height` (inches) | `<height>` (cm) | Optional | `packages[].dimensions.height` |
| 13 | Dimension Unit | `dimensions.units` (IN/CM) | Inches (default) | CM only (fixed) | ✔ | `packages[].dimensions.unit` |
| 14 | Package Count | `packageCount` (int) | Derived from items array | Item count in characteristics | ✔ | `packages[].quantity` |
| 15 | Declared Value | `declaredValue.amount` | `insurance_amount` | `<declared-value>` | Optional | `packages[].declaredValue` |
| 16 | Service Filter | `serviceType` (e.g. `FEDEX_GROUND`) | `carrier_type` / `service_name` | Service code in URL path | Optional | `serviceFilter.serviceCode` |
| 17 | Ship Date | `shipDateStamp` (YYYY-MM-DD) | `ship_date` | `<expected-mailing-date>` | Optional | `shipDate` |
| 18 | Account Number | `shippingChargesPayment.payor.accountNumber` | Configured in portal | `<customer-number>` + `<contract-id>` | ✔* | `billingAccount.accountNumber` |
| 19 | Payment Type | `shippingChargesPayment.paymentType` | Not per-request | `<intended-method-of-payment>` | Optional | `billing.paymentType` |
| 20 | HS Code (Intl) | `commodity.harmonizedCode` | `items[].hts_code` | N/A (domestic) | Intl only | `packages[].hsCode` |
| 21 | Freight Class (LTL) | Not applicable | `items[].freight_class` (NMFC) | Not applicable | ShipHawk LTL | `packages[].freightClass` |

---

## 4. Rate Response Schema

| # | Field | FedEx (JSON) | ShipHawk (JSON) | Canada Post (XML) | Normalised Field |
|---|-------|-------------|-----------------|-------------------|-----------------|
| 1 | Response Root | `output.rateReplyDetails[]` | `rates[]` | `<price-quotes><price-quote>[]` | `rates[]` |
| 2 | Service Code | `rateReplyDetails[].serviceType` | `rates[].service_name` | `<service-code>` | `rate.serviceCode` |
| 3 | Service Name | `rateReplyDetails[].serviceName` | `rates[].service_name` | `<service-name>` | `rate.serviceName` |
| 4 | Total Net Charge | `ratedShipmentDetails[].totalNetCharge.amount` | `rates[].total_amount` (cents) | `<base>` + adjustments | `rate.totalCharge.amount` |
| 5 | Currency | `totalNetCharge.currency` | `rates[].currency` | CAD (implicit) | `rate.totalCharge.currency` |
| 6 | Fuel Surcharge | `surcharges[type='FUEL'].amount` | Included in total | `<adjustment type='FUEL'>` | `rate.surcharges[].fuel` |
| 7 | Residential Surcharge | `surcharges[type='RESIDENTIAL_DELIVERY']` | Included in total | `<adjustment type='RESIDENTIAL'>` | `rate.surcharges[].residential` |
| 8 | Taxes | `shipmentRateDetail.totalTaxes` | `rates[].taxes[]` | `<gst>` / `<hst>` / `<pst>` | `rate.taxes[]` |
| 9 | Billable Weight | `shipmentRateDetail.totalBillingWeight.value` | `rates[].billable_weight` | `<parcel-weight>` | `rate.billableWeight` |
| 10 | Transit Days | `operationalDetail.transitTime` | `rates[].transit_days` (int) | `<expected-transit-time>` (int) | `rate.transitDays` |
| 11 | Estimated Delivery | `operationalDetail.deliveryDate` | `rates[].estimated_delivery_date` | `<expected-delivery-date>` | `rate.estimatedDeliveryDate` |
| 12 | Guaranteed Delivery | `operationalDetail.commitDate` | `rates[].guaranteed` (bool) | `<guaranteed-delivery>` (bool) | `rate.isGuaranteed` |
| 13 | Rate ID / Quote Token | Not returned | `rates[].id` (valid 2 hrs) | Not returned | `rate.rateId` |
| 14 | Rate Type | `ratedShipmentDetails[].rateType` | `rates[].rate_type` | Contract vs Retail (by input) | `rate.rateType` |
| 15 | Warnings | `output.alerts[].code` + `message` | HTTP error body | `<messages><message>[]` | `response.warnings[]` |

---

## 5. Label Generation (Shipment Creation)

| # | Attribute | FedEx | ShipHawk | Canada Post |
|---|-----------|-------|----------|-------------|
| 1 | Endpoint | `POST /ship/v1/shipments` | `POST /api/v4/shipments` | `POST /rs/{cust}/{mobo}/shipment` |
| 2 | Shipper Contact | Required (name + phone) | Required (name + phone) | Required (contact-name + phone) |
| 3 | Service Selection | `serviceType` field | `rate_id` from prior rate call | Service code in `<delivery-spec>` |
| 4 | Package Details | `requestedPackageLineItems[]` | `items[]` / `packages[]` | `<parcel-characteristics>` |
| 5 | Multi-piece Support | Yes — `packageCount > 1`; subsequent calls reference `masterTrackingId` | Yes — multiple items in array | No — create separate shipment per package |
| 6 | Label Spec | `labelSpecification.imageType` + `labelStockType` | `label_format` param or portal default | `<print-preferences><output-format>` |
| 7 | Tracking Number | `output.transactionShipments[].masterTrackingNumber` | `shipment.tracking_number` | `<shipment-info><tracking-pin>` |
| 8 | Label in Response | Base64 in `encodedLabel` field | URL (`shipment.label_url`) | HATEOAS link — GET returns binary PDF/ZPL |
| 9 | Shipment ID | `transactionShipments[].trackingNumber` | `shipment.id` | `<shipment-id>` |
| 10 | Total Cost in Response | `shipmentAdvisoryDetails` | `shipment.total_amount` (cents) | `<shipment-info><shipment-price>` |
| 11 | Cancel Endpoint | `PUT .../cancel` | `DELETE /api/v4/shipments/{id}` | `POST .../void` |
| 12 | Cancel Window | Before carrier pickup | Before manifesting | Before Transmit Shipments call |
| 13 | Return Label | `isReturnShipment` flag | `return_label: true` | Authorised Returns / Open Returns API |
| 14 | Manifest / End-of-Day | Optional for Ground; required for Express | Auto or manual batch manifest | **Required** — `Transmit Shipments` before pickup |

---

## 6. Document Rendering

| # | Attribute | FedEx | ShipHawk | Canada Post |
|---|-----------|-------|----------|-------------|
| 1 | PDF | ✔ | ✔ | ✔ (default) |
| 2 | ZPL II | ✔ | ✔ | ✔ |
| 3 | PNG | ✔ (min 600 DPI) | ✔ | ✗ |
| 4 | EPL2 | ✔ | ✔ | ✗ |
| 5 | DPL | ✔ | Depends on PrintNote config | ✗ |
| 6 | 4×6 in (thermal) | ✔ `STOCK_4X6` | ✔ | ✔ |
| 7 | 4×8 / 4×9 (doc-tab) | ✔ `STOCK_4X8` / `STOCK_4X9` | Varies | ✗ |
| 8 | 8.5×11 letter | ✔ `PAPER_8.5X11_TOP_HALF_LABEL` | ✔ | ✔ `output-format: 8.5x11` |
| 9 | Label Encoding | Base64 string in JSON body | URL to download file | HATEOAS URL (GET returns binary) |
| 10 | Label Expiry | Until shipment voided | ~24–48 hrs | 90 days (return label: 5 days) |
| 11 | Multi-piece Labels | Separate label per package in `pieceResponses[]` | Separate URLs per item | Separate shipment per package → separate label |
| 12 | Commercial Invoice | ✔ ETD via `CommercialInvoice` block | ✔ `commercial_invoice` document type | ✔ `<customs-invoice>` for international |
| 13 | Packing Slip | ✗ | ✔ `packing_slip` document type | ✔ `show-packing-instructions` flag |
| 14 | Bill of Lading | ✗ (parcel only) | ✔ (LTL shipments) | ✗ |
| 15 | Label Certification | **Required** — submit PDF/PNG/ZPL at 600 DPI to `label@fedex.com` before production | Not required | Not required |

---

## 7. Error Handling

| # | HTTP Code | FedEx | ShipHawk | Canada Post | Aggregator Action |
|---|-----------|-------|----------|-------------|-------------------|
| 1 | 200 OK | Success | Success | Success — **but inspect XML body for `<messages>`** | Always parse body for Canada Post |
| 2 | 400 Bad Request | Invalid schema / missing fields | Validation failure | Business rule violation | Parse error; return `ValidationError` with field path |
| 3 | 401 Unauthorized | Token expired or invalid | Invalid API key | Invalid Basic Auth | FedEx: refresh token → retry once. Others: alert ops |
| 4 | 403 Forbidden | Account not authorised for service | Permission issue | Account restricted | Surface to user; do not retry |
| 5 | 404 Not Found | Resource not found | Resource not found | Resource not found / label link expired | Return `CarrierResourceNotFound` with carrier ref |
| 6 | 429 Too Many Requests | Rate limit hit | Rate limit hit | May occur | Exponential back-off; respect `Retry-After` header |
| 7 | 500 / 503 | Internal error | Internal error | Service unavailable | Retry up to 3× with back-off; surface `ServiceUnavailableError` |

### Error Body Structures

| | FedEx | ShipHawk | Canada Post |
|-|-------|----------|-------------|
| Format | JSON | JSON | XML |
| Structure | `{ "errors": [{ "code": "...", "message": "..." }] }` | `{ "errors": [{ "message": "..." }] }` | `<messages><message><code>` + `<description>` |
| Soft Warnings | `output.alerts[]` in 200 response | Not separated | `<messages>` inside 200 body |

### Retry Strategy

| Scenario | Retry? | Strategy |
|----------|--------|----------|
| 401 — FedEx | Yes | Refresh OAuth token → retry once |
| 401 — ShipHawk / Canada Post | No | Alert; key rotation required |
| 429 | Yes | Exponential back-off with jitter |
| 5xx | Yes | Up to 3 attempts; back-off (1 s, 2 s, 4 s) |
| 400 | No | Fix request payload; do not retry |

### Additional Practices

| # | Practice | FedEx | ShipHawk | Canada Post |
|---|----------|-------|----------|-------------|
| 1 | Idempotency | Use `customerTransactionId` in request | Not documented | `group-id` in Create Shipment prevents duplicate |
| 2 | Circuit Breaker | Trip after 5 consecutive 5xx | Trip after 5 consecutive 5xx | Trip after 5 consecutive 5xx |
| 3 | Logging | Never log `access_token`, `encodedLabel` | Never log `api_key` | Never log `Authorization` header |

---

## 8. Cross-Carrier Normalisation Reference

### Unit & Format Differences

| Attribute | FedEx | ShipHawk | Canada Post | Normalisation Rule |
|-----------|-------|----------|-------------|-------------------|
| Payload format | JSON | JSON | XML | XML ↔ JSON adapter for Canada Post |
| Weight unit | LB or KG | lbs (default) | KG only | Convert to carrier-native unit before dispatch |
| Dimension unit | IN or CM | Inches | CM only | `inches × 2.54 = cm` for Canada Post |
| Currency | USD | USD (cents) | CAD | Store currency code with every monetary field |
| Label delivery | Base64 in body | Download URL | HATEOAS link | Normalise to `LabelArtifact { url?, base64?, mimeType }` |
| Rate ID at booking | Not returned — must re-rate | `rate_id` valid 2 hrs | Not returned — must re-rate | Store ShipHawk `rate_id`; re-request rates for FedEx & CPC |

### Service Code Equivalents

| Speed Tier | FedEx | Canada Post | ShipHawk |
|-----------|-------|-------------|----------|
| Ground / Economy | `FEDEX_GROUND` | `DOM.EP` | FedEx Ground |
| 2-Day | `FEDEX_2_DAY` | `DOM.XP` | FedEx 2Day |
| Overnight / Priority | `PRIORITY_OVERNIGHT` | `DOM.PC` | FedEx Priority Overnight |
| International Economy | `INTERNATIONAL_ECONOMY` | `USA.EP` | FedEx International Economy |

> Maintain a `ServiceCodeRegistry` map in the aggregator: `{ aggregatorCode → { fedex, shiphawk, canadapost } }`

### Normalised Booking Workflow

```
getRates()
  → FedEx:       POST /rate/v1/rates/quotes      (JSON)
  → ShipHawk:    POST /api/v4/rates               (JSON)
  → Canada Post: POST /rs/{cust}/{mobo}/service   (XML)

normaliseRates() → unified rates[] array

createShipment()
  → FedEx:       POST /ship/v1/shipments           re-rate at booking
  → ShipHawk:    POST /api/v4/shipments            pass rate_id
  → Canada Post: POST /rs/{cust}/{mobo}/shipment   re-rate at booking; then call Transmit Shipments

getLabel()
  → FedEx:       decode Base64 encodedLabel
  → ShipHawk:    fetch label_url
  → Canada Post: follow HATEOAS link → GET binary
  → save to own blob storage immediately
```

### Key Carrier-Specific Requirements

| Requirement | FedEx | ShipHawk | Canada Post |
|-------------|-------|----------|-------------|
| Token refresh | Required (hourly) | Not needed | Not needed |
| Manifest before pickup | Optional (Express only) | Auto or manual | **Mandatory** (`Transmit Shipments`) |
| Label certification | **Required before production** | Not required | Not required |
| Webhook tracking | Not available | Available | Not available |
| Multi-package | Supported natively | Supported natively | One shipment per package |
