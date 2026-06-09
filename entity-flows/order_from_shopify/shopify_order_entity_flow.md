# Shopify Order Import: Entity Data Creation Flow

This document details the sequential flow of data creation across entities when a Shopify order is imported into HotWax Commerce (Moqui/OFBiz-based OMS). 

---

## High-Level Execution Sequence
The import process is handled by a combination of Shopify bridge services and core OMS order creation services:
1. **Payload Ingestion**: A Shopify order webhook or SQS message triggers the sync.
2. **Payload Preparation**: `prepare#TransformedShopifyOrderPayload` normalizes the payload.
3. **Core Order Service**: `co.hotwax.oms.order.OrderServices.create#SalesOrder` handles master-detail persistence.
4. **Post-Creation Sync**: Shopify-specific tables (like payment preferences and shop-order mappings) are written.

---

## Sequential Entity Data Creation Flow
Below is the precise order of operations and entity creation in the database during order import.

### 1. Ingestion Logging & Staging
* **`moqui.service.message.SystemMessage`** (if webhook/event is received from SQS and logged/audited).
* **`co.hotwax.data.DataManagerFile`** (when bulk files are processed).

### 2. Contact Mechanisms (Addresses & Phones)
Before the customer party or order header is created, all contact mechanisms must be persisted to generate `contactMechId` references:
* **`org.apache.ofbiz.party.contact.ContactMech`** 
  * Created for the shipping postal address (Type: `POSTAL_ADDRESS`).
  * Created for the shipping email (Type: `EMAIL_ADDRESS`), if present.
  * Created for the shipping phone number (Type: `TELECOM_NUMBER`), if present.
* **`org.apache.ofbiz.party.contact.PostalAddress`** (holds details of the shipping location).
* **`org.apache.ofbiz.party.contact.TelecomNumber`** (holds details of the shipping phone number).
* **`org.apache.ofbiz.party.contact.ContactMech`**
  * Created for the billing postal address (Type: `POSTAL_ADDRESS`), if present.
  * Created for the billing email (Type: `EMAIL_ADDRESS`), if present.
  * Created for the billing phone number (Type: `TELECOM_NUMBER`), if present.
* **`org.apache.ofbiz.party.contact.PostalAddress`** (holds details of the billing location).
* **`org.apache.ofbiz.party.contact.TelecomNumber`** (holds details of the billing phone number).

### 3. Placing Customer Party Creation
If the customer is new (i.e. not matched by email or external ID):
* **`org.apache.ofbiz.party.contact.ContactMech`** (Created for the customer's primary email/phone).
* **`org.apache.ofbiz.party.contact.TelecomNumber`** (holds primary customer phone details).
* **`org.apache.ofbiz.party.party.Party`** (Created with `partyTypeId='PERSON'` and `statusId='PARTY_ENABLED'`).
* **`org.apache.ofbiz.party.party.Person`** (stores `firstName` and `lastName`).
* **`org.apache.ofbiz.party.party.PartyRole`** (associates customer party with standard roles: `CUSTOMER`, `PLACING_CUSTOMER`, `END_USER_CUSTOMER`, `SHIP_TO_CUSTOMER`, `BILL_TO_CUSTOMER`).
* **`org.apache.ofbiz.party.party.PartyIdentification`** (maps the Shopify customer ID e.g., `SHOPIFY_CUST_ID` to the Moqui party).
* **`org.apache.ofbiz.party.contact.PartyContactMech`** / **`org.apache.ofbiz.party.contact.PartyContactMechPurpose`** (associates the created primary email/phone contact mechs to the party under purposes `PRIMARY_EMAIL` and `PRIMARY_PHONE`).

### 4. Order Header Creation
The master order header is persisted:
* **`org.apache.ofbiz.order.order.OrderHeader`** (holds the main order record with `orderId`, status `ORDER_CREATED`, type `SALES_ORDER`, currencies, and store IDs).

### 5. Order Relationships (Master-Detail Persistence)
Using Moqui's nested entity engine, the direct relationships of the order are created:
* **`org.apache.ofbiz.order.order.OrderRole`** (maps the roles to this specific order, e.g., `PLACING_CUSTOMER`, `BILL_TO_CUSTOMER`, `BILL_FROM_VENDOR`, `SHIP_FROM_VENDOR`).
* **`org.apache.ofbiz.order.order.OrderContactMech`** (associates contact mechanisms specifically to the order, such as `ORDER_EMAIL`, `SHIPPING_LOCATION`, `BILLING_LOCATION`).
* **`co.hotwax.order.OrderIdentification`** (stores external Shopify order reference tags).
* **`org.apache.ofbiz.order.order.OrderAdjustment`** (stores order-level charges/promotions, e.g. shipping adjustments, overall discounts).
* **`org.apache.ofbiz.order.order.OrderAttribute`** (stores general order-level metadata).
* **`org.apache.ofbiz.order.order.OrderStatus`** (logs the initial status of the order, `ORDER_CREATED`).

### 6. Order Tags & Internal Notes
* **`org.apache.ofbiz.common.note.NoteData`** (contains the tag text or note content).
* **`org.apache.ofbiz.order.order.OrderHeaderNote`** (links the `NoteData` to `OrderHeader`).
* **`org.apache.ofbiz.party.communication.CommunicationEvent`** / **`org.apache.ofbiz.order.order.CommunicationEventOrder`** (if custom customer notes or staff communications are present).

### 7. Order Item Ship Group
* **`org.apache.ofbiz.order.order.OrderItemShipGroup`** (stores fulfillment configuration: carrier, shipping method type, and delivery contact mechanisms).

### 8. Order Items & Line-Item Details
Created nested under their respective ship group:
* **`org.apache.ofbiz.order.order.OrderItem`** (persists product ID, SKU, quantity, prices, description, and status `ITEM_CREATED`).
* **`org.apache.ofbiz.order.order.OrderAdjustment`** (item-specific adjustments, such as item level taxes `SALES_TAX` or promotions `EXT_PROMO_ADJUSTMENT`).
* **`org.apache.ofbiz.order.order.OrderItemAttribute`** (stores Shopify line-item properties and custom metadata).
* **`org.apache.ofbiz.order.order.OrderStatus`** (logs the initial status of the item, `ITEM_CREATED`).
* **`org.apache.ofbiz.order.order.OrderItemAssoc`** (if the line item is linked to another order/item).

### 9. Payment Preferences, History, & Bridge Mappings
* **`org.apache.ofbiz.order.order.OrderPaymentPreference`** (payment preference records mapping amount, payment method type, and authorization/settlement status).
* **`co.hotwax.shopify.ShopifyTransactionHistory`** (captures external transaction details from Shopify: gateway ID, payment status, transaction kind, e.g., authorization, capture).
* **`co.hotwax.shopify.ShopifyShopOrder`** (the bridge entity mapping the local `orderId` to Shopify's external `shopifyOrderId` and store `shopId`).
* **`org.apache.ofbiz.party.party.PartyClassification`** (adds customer classifications based on Shopify tags).
* **`org.apache.ofbiz.order.order.OrderTerm`** (if payment terms or outstanding terms are mapped).
