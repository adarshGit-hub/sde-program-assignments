# Order Approval & Allocation: Entity Data Flow

This document details the sequential flow of data updates and entity creation when a sales order is approved and allocated/reserved in HotWax Commerce (Moqui/OFBiz-based OMS).

---

## High-Level Execution Sequence
When an order is approved (either automatically upon creation via SECA rules, or manually/externally):
1. **Order Approval Status Update**: Status fields are updated on headers and items.
2. **Facility Allocation**: Order items are assigned to fulfillment facilities, potentially generating new ship groups.
3. **Inventory Reservation**: Physical stock is reserved at the allocated facility, decrementing the Available-To-Promise (ATP) inventory.

---

## Sequential Entity Data Flow
Below is the precise order of operations, including updates, insertions, and deletions on database entities.

### 1. Order Status Updates
* **`org.apache.ofbiz.order.order.OrderHeader`** *(Update)*
  * The `statusId` is updated to `ORDER_APPROVED`.
* **`org.apache.ofbiz.order.order.OrderStatus`** *(Insert)*
  * A new status history record is created for the order (e.g. `orderId`, `statusId='ORDER_APPROVED'`, `statusDatetime`).
* **`org.apache.ofbiz.order.order.OrderItem`** *(Update)*
  * The `statusId` for each line item is updated to `ITEM_APPROVED`.
* **`org.apache.ofbiz.order.order.OrderStatus`** *(Insert)*
  * A new status history record is created for each individual order item (e.g. `orderId`, `orderItemSeqId`, `statusId='ITEM_APPROVED'`, `statusDatetime`).

### 2. Facility Allocation & Ship Group Re-assignment
If the item is being assigned or routed to a specific fulfillment facility:
* **`org.apache.ofbiz.order.order.OrderItemShipGroup`** *(Insert)*
  * If a matching ship group does not exist for the target facility and shipping method, a new ship group record is created.
* **`org.apache.ofbiz.order.order.OrderAdjustment`** *(Update)*
  * Any item-level adjustments (such as tax or promo adjustments) are updated to reference the new ship group (`shipGroupSeqId`).
* **`org.apache.ofbiz.order.order.OrderItem`** *(Update)*
  * The `shipGroupSeqId` is updated to assign the item to the correct ship group.
  * The `autoCancelDate` is updated or cleared if applicable.
* **`co.hotwax.facility.OrderFacilityChange`** *(Insert)*
  * Logs the history of the routing allocation change, including the source and target facilities, routing rules, routing runs, change user, and datetime.
* **`co.hotwax.facility.FacilityOrderCount`** *(Insert or Update)*
  * Increments/updates the overall daily order count metrics for the target facility on the current date.

### 3. Inventory Reservation
For each allocated physical (non-virtual) facility, the system reserves stock:
* **`org.apache.ofbiz.product.inventory.InventoryItem`** *(Insert or Retrieve)*
  * Creates or retrieves a facility inventory item record mapping the product to the target facility.
* **`org.apache.ofbiz.order.order.OrderItemShipGrpInvRes`** *(Insert)*
  * Creates an item level inventory reservation record mapping the `orderId`, `orderItemSeqId`, `shipGroupSeqId`, and `inventoryItemId` with the reserved quantity.
* **`co.hotwax.oms.product.InventoryItemDetail`** *(Insert)*
  * Adds an inventory ledger entry recording a negative offset against the Available-To-Promise (ATP) stock (e.g. `availableToPromiseDiff = -quantity`).
