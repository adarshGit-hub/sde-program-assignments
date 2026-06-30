# SDE Program Assignments

This repository contains assignment solutions to the [SaaS Tech Academy SDE Program](https://github.com/saastechacademy/foundation/blob/main/sde-program.md). The projects and assignments are completed as part of the SDE (Software Development Engineer) program, with a primary focus on Apache OFBiz, order management systems, and related integrations (e.g., HotWax Commerce, Shopify, NetSuite).

## Repository Structure

### 1. [SQL Assignments](./SQL)
Contains SQL query exercises related to an Order Management System (OMS) database schema (e.g., OFBiz). 
- **[sql-assignment-1.md](./SQL/sql-assignment-1.md)**: Covers business scenarios such as customer acquisition, product management (including NetSuite/Shopify/HotWax IDs), order fulfillment reporting, sales and revenue analysis (BOPIS), and payment reconciliation.
- **[sql-assignment-2.md](./SQL/sql-assignment-2.md)**: Covers business scenarios such as region-specific order analysis (e.g., New York), facility-wise revenue, inventory tracking (lost/damaged, low stock), and sales channel performance.
- **[sql-assignment-3.md](./SQL/sql-assignment-3.md)**: Covers business scenarios such as return and appeasement analysis, logistics tracking (transfer orders, one-day shipping, missing picklists), and detailed inventory-facility relationships.


### 2. [Manager Components](./manager-components)
Custom components built for Apache OFBiz:
- **[CustomerManagement/](./manager-components/CustomerManagement)**: A component designed for customer information, profiles, and relationship management.
- **[ProductManagement/](./manager-components/ProductManagement)**: A component designed for product catalog and features management.
- **[ordermgmtsystem/](./manager-components/ordermgmtsystem)**: A component designed for Order Management System functionalities.
- **[productinformationmgr/](./manager-components/productinformationmgr)**: A component designed for Product Information Management (PIM). Contains standard OFBiz component directories (`entitydef`, `servicedef`, `webapp`, `widget`, etc.).
- **[relationshipmgr/](./manager-components/relationshipmgr)**: A component designed for managing business relationships, customer data, or CRM functionalities.

### 3. [Activity Assignments](./activity-assignments)
Reports and documentation for assignments:
- **[OFBiz_Order_Fulfillment_Report.pdf](./activity-assignments/OFBiz_Order_Fulfillment_Report.pdf)**: A detailed report on order fulfillment processes in Apache OFBiz.
- **[company-store-product-catalog-assignment.pdf](./activity-assignments/company-store-product-catalog-assignment.pdf)**: An assignment focused on creating and managing a product catalog for a company store.
- **[multi-carrier-api-integration-assignment.md](./activity-assignments/multi-carrier-api-integration-assignment.md)**: Technical discovery matrix for integrating FedEx (REST), ShipHawk, and Canada Post (REST) APIs into a unified shipping aggregator.

### 4. [Entity Flows](./entity-flows/order_from_shopify)
Details the database table creation, insertion, and update flows in Apache OFBiz / Moqui during the Shopify order lifecycle:
- **[shopify_order_entity_flow.md](./entity-flows/order_from_shopify/shopify_order_entity_flow.md)**: Details the step-by-step entity creation sequence when importing a Shopify order payload.
- **[order_approval_entity_flow.md](./entity-flows/order_from_shopify/order_approval_entity_flow.md)**: Details the entity flow when a sales order is approved and inventory is allocated and reserved.
- **[picking_fulfillment_entity_flow.md](./entity-flows/order_from_shopify/picking_fulfillment_entity_flow.md)**: Explains the entity flow for picking, wave generation, and shipment creation.
- **[packing_shipping_entity_flow.md](./entity-flows/order_from_shopify/packing_shipping_entity_flow.md)**: Covers entity changes when packing shipments and executing the shipping dispatch.
- **[post_shipping_entity_flow.md](./entity-flows/order_from_shopify/post_shipping_entity_flow.md)**: Triggers payment capture, Shopify fulfillment sync, invoicing, accounting, and return processing.

### 5. [Order Fulfillment Automation](./%20order-fulfillment-automation-assignment)
Postman collection and environment configurations for API-based fulfillment testing:
- **[PICK_PACK_SHIP.postman_collection.json](./%20order-fulfillment-automation-assignment/PICK_PACK_SHIP.postman_collection.json)**: Comprehensive Postman requests for simulating pick, pack, and ship API actions.
- **[nextgen.postman_environment.json](./%20order-fulfillment-automation-assignment/nextgen.postman_environment.json)**: Environment variable parameters for the Postman collection.

### 6. [Simulation Routing (Streaming Analysis)](./sim-routing)
Performance investigation and architectural reports on order routing and database integration:
- **[STREAMING_HYPOTHESIS_REPORT.md](./sim-routing/STREAMING_HYPOTHESIS_REPORT.md)**: Empirical test results refuting the standard Moqui MySQL streaming assumptions and justifying the custom raw JDBC stream engine.


