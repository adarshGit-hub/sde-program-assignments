# Packing & Shipping: Entity Data Flow

This document details the sequential flow of data updates and entity creation when a shipment is packed and shipped (dispatched) in HotWax Commerce (Moqui/OFBiz-based OMS).

---

## High-Level Execution Sequence
The packing and shipping process transitions the order from a staging/picking state to physical delivery:
1. **Packing**: Updates shipment packages, records tracking codes, and sets the shipment status to packed.
2. **Shipping (Dispatched)**: Issues inventory from the facility stock, updates the shipment status to shipped, and updates the inventory ledger.
3. **Order Completion & Invoicing**: Triggers item completion, deletes temporary reservations, and completes the order.

---

## Sequential Entity Data Flow
Below is the precise sequence of database table insertions and updates.

### Phase 1: Packing the Shipment
When a user finishes packing the items and prints labels/manifests:
* **`org.apache.ofbiz.shipment.shipment.ShipmentPackage`** *(Update)*
  * Recalculates and updates package weight, box types, or packaging dimensions.
* **`org.apache.ofbiz.shipment.shipment.ShipmentRouteSegment`** *(Update)*
  * Updates `trackingIdNumber` and `referenceNumber` with the carrier tracking ID.
  * Sets `carrierServiceStatusId` to `SHRSCS_ACCEPTED`.
* **`org.apache.ofbiz.shipment.shipment.ShipmentPackageRouteSeg`** *(Update)*
  * Updates the `trackingCode` linked to the package route segment.
* **`org.apache.ofbiz.shipment.shipment.Shipment`** *(Update)*
  * Updates `statusId` to `SHIPMENT_PACKED`.
* **`org.apache.ofbiz.shipment.shipment.ShipmentStatus`** *(Insert)*
  * Logs the status history change to `SHIPMENT_PACKED`.

---

### Phase 2: Shipping (Dispatched)
When the shipment is handed to the carrier (dispatched):
* **`org.apache.ofbiz.shipment.issuance.ItemIssuance`** *(Insert)*
  * For each shipped item, creates an issuance record matching the shipment, order item, ship group, and the specific `inventoryItemId` from which the stock is drawn.
* **`co.hotwax.oms.product.InventoryItemDetail`** *(Insert)*
  * Logs a ledger detail record decrementing the physical Quantity-on-Hand (`quantityOnHandDiff = -quantity`). 
  * *Note: Available-to-Promise (ATP) is NOT decremented here because it was already decremented during the Reservation phase.*
* **`org.apache.ofbiz.shipment.shipment.Shipment`** *(Update)*
  * Updates `statusId` to `SHIPMENT_SHIPPED`.
* **`org.apache.ofbiz.shipment.shipment.ShipmentStatus`** *(Insert)*
  * Logs the status history change to `SHIPMENT_SHIPPED`.

---

### Phase 3: Post-Shipping Order Updates (Status & Cleanup)
Upon successful shipment, automatic hooks and services update the parent order status:
* **`org.apache.ofbiz.order.order.OrderItem`** *(Update)*
  * Updates the item `statusId` to `ITEM_COMPLETED`.
* **`org.apache.ofbiz.order.order.OrderStatus`** *(Insert)*
  * Creates an item-level status history log for `ITEM_COMPLETED`.
* **`org.apache.ofbiz.order.order.OrderItemShipGrpInvRes`** *(Delete)*
  * Deletes the reservation record since the inventory has been physically issued.
* **`org.apache.ofbiz.order.order.OrderHeader`** *(Update)*
  * If all items under the order are now completed, updates the master order `statusId` to `ORDER_COMPLETED`.
* **`org.apache.ofbiz.order.order.OrderStatus`** *(Insert)*
  * Creates an order-level status history log for `ORDER_COMPLETED`.
