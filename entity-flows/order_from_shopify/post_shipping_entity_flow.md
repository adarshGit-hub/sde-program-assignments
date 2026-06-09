# Post-Shipping Processes: Entity Data Flow

This document details the sequential flow of data updates and entity creation that occur *after* an order has been successfully shipped (marked as `SHIPMENT_SHIPPED` and `ORDER_COMPLETED`) in HotWax Commerce (Moqui/OFBiz-based OMS).

---

## High-Level Execution Sequence
Once shipment dispatch is complete, the order enters its post-fulfillment lifecycle:
1. **Payment Capture & Settlement**: Triggers payment capture tags/notes for Shopify, capturing customer authorizations.
2. **Shopify Fulfillment Sync**: Uploads shipping carrier and tracking details to Shopify to mark the order as fulfilled on the e-commerce store.
3. **Invoicing & Accounting Ledger**: Generates sales invoices, records received payments, and reconciles the accounting books.
4. **Returns & Refunds (Reverse Logistics)**: Manages customer returns, inventory restocking, credit memos, and payment refunds.

---

## Sequential Entity Data Flow
Below is the precise sequence of database table operations for each post-fulfillment phase.

### 1. Payment Capture Triggering
When order items transition to `ITEM_COMPLETED`, payment capture is initiated:
* **`org.apache.ofbiz.common.note.NoteData`** *(Insert)*
  * Creates a note containing the custom capture payment tag (e.g. `CAPTURE_PAYMENT`).
* **`org.apache.ofbiz.order.order.OrderHeaderNote`** *(Insert)*
  * Associates the payment capture note with the `OrderHeader`.
  * *Note: The tag is synced to Shopify to automatically trigger the payment capture webhook/API on Shopify's gateway.*

### 2. Shopify Fulfillment Sync (Fulfillment Integration Feed)
Fulfillment details (tracking numbers, carriers) are sent back to Shopify:
* **`moqui.service.message.SystemMessage`** *(Insert)*
  * Creates a queued system message containing the formatted Shopify fulfillment payload.
* **`org.apache.ofbiz.shipment.shipment.Shipment`** *(Update)*
  * Once the fulfillment sync is acknowledged by Shopify, the shipment's `externalId` is updated with Shopify's remote fulfillment ID.

### 3. Sales Invoicing & Payment Settlement
* **`org.apache.ofbiz.accounting.invoice.Invoice`** *(Insert)*
  * Creates a sales invoice header with `invoiceTypeId='SALES_INVOICE'` and status `INVOICE_READY`.
* **`org.apache.ofbiz.accounting.invoice.InvoiceItem`** *(Insert)*
  * Populates individual invoice items representing the shipped products, shipping charges, and sales taxes.
* **`org.apache.ofbiz.order.order.OrderItemBilling`** *(Insert)*
  * Creates a cross-reference record linking the `OrderItem` directly to its corresponding `InvoiceItem`.
* **`org.apache.ofbiz.accounting.payment.Payment`** *(Insert)*
  * Creates a payment record with status `PMNT_RECEIVED` upon receiving the capture settlement confirmation from the gateway.
* **`org.apache.ofbiz.accounting.payment.PaymentApplication`** *(Insert)*
  * Applies the payment to the sales invoice, closing the invoice ledger balance.
* **`org.apache.ofbiz.order.order.OrderPaymentPreference`** *(Update)*
  * Updates the payment preference `statusId` to `PAYMENT_SETTLED`.

### 4. Returns & Refunds (Reverse Logistics)
If a customer returns items post-fulfillment:
* **`org.apache.ofbiz.order.return.ReturnHeader`** *(Insert)*
  * Creates the master return authorization record.
* **`org.apache.ofbiz.order.return.ReturnItem`** *(Insert)*
  * Lists return items, quantities, refund amounts, and return reasons.
* **`org.apache.ofbiz.shipment.receipt.ShipmentReceipt`** *(Insert)*
  * Logged when the returned items physically arrive at the warehouse.
* **`co.hotwax.oms.product.InventoryItemDetail`** *(Insert)*
  * Records positive adjustments to ATP and QOH (`availableToPromiseDiff` and `quantityOnHandDiff`) to return items to inventory stock.
* **`org.apache.ofbiz.accounting.invoice.Invoice`** *(Insert)*
  * Creates a customer return invoice (Credit Memo) of type `CUSTOMER_RETURN`.
* **`org.apache.ofbiz.accounting.payment.Payment`** *(Insert)*
  * Generates a refund payment of type `CUSTOMER_REFUND` with status `PMNT_SENT`.
