# Picking & Wave Generation: Entity Data Flow

This document details the sequential flow of data creation and updates across entities when orders are selected for picking and grouped into a Picklist (Fulfillment Wave) in the HotWax Commerce fulfillment app.

---

## High-Level Execution Sequence
When orders are picked:
1. **Shipment Generation**: If a shipment does not exist for an order's ship group, the system creates a shipment hierarchy (Shipment, ShipmentItems, ShipmentPackages, RouteSegments).
2. **Picklist Grouping**: A master Picklist record is created, associating the shipments and assigning pickers to the wave.
3. **Fulfillment Status Sync**: Order items are marked as `InProgress` in the Solr search index.

---

## Sequential Entity Data Flow
Below is the precise sequence of database table insertions and relationships.

### 1. Shipment Hierarchy Creation (Per Order/Ship Group)
For each order item group in the wave:
* **`org.apache.ofbiz.shipment.shipment.Shipment`** *(Insert)*
  * Creates the master shipment record (e.g. `shipmentId`, type `SALES_SHIPMENT`, status `SHIPMENT_APPROVED`, linking origin/destination facilities and contact mechs).
* **`org.apache.ofbiz.shipment.shipment.ShipmentItem`** *(Insert)*
  * Registers each item/product and its quantity under the shipment.
* **`org.apache.ofbiz.order.order.OrderShipment`** *(Insert)*
  * Creates the cross-reference mapping between the order item (`orderId`, `orderItemSeqId`, `shipGroupSeqId`) and the shipment item.
* **`org.apache.ofbiz.shipment.shipment.ShipmentPackage`** *(Insert)*
  * Creates packaging info, storing dimensions, boxing type (e.g., `YOURPACKNG`), and calculated total weight.
* **`org.apache.ofbiz.shipment.shipment.ShipmentPackageContent`** *(Insert)*
  * Associates the shipment items and their quantities with the shipment package.
* **`org.apache.ofbiz.shipment.shipment.ShipmentRouteSegment`** *(Insert)*
  * Creates routing details including carrier, shipment method, tracking requirements, weight, and delivery contact mechanisms.
* **`org.apache.ofbiz.shipment.shipment.ShipmentPackageRouteSeg`** *(Insert)*
  * Connects the shipment package to the route segment.
* **`org.apache.ofbiz.shipment.shipment.ShipmentStatus`** *(Insert)*
  * Logs the initial status history entry (`SHIPMENT_APPROVED`).

### 2. Picklist Wave Generation
* **`org.apache.ofbiz.shipment.picklist.Picklist`** *(Insert)*
  * Creates the master picklist record containing `picklistId`, `facilityId`, `shipmentMethodTypeId`, `statusId='PICKLIST_INPUT'`, and `picklistDate`.
* **`org.apache.ofbiz.shipment.picklist.PicklistRole`** *(Insert)*
  * Assigns picker user accounts/parties to the picklist (maps `partyId` under a picker role type).
* **`org.apache.ofbiz.shipment.picklist.PicklistShipment`** *(Insert)*
  * Maps each of the created shipments to this picklist.
