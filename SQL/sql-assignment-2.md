# SQL Assignment 2

## Questions & Solutions

### 5.1 Shipping Addresses for October 2023 Orders

**Business Problem:**
> Customer Service might need to verify addresses for orders placed or completed in October 2023. This helps ensure shipments are delivered correctly and prevents address-related issues.

**Fields to Retrieve:**
- `ORDER_ID`
- `PARTY_ID (Customer ID)`
- `CUSTOMER_NAME (or FIRST_NAME / LAST_NAME)`
- `STREET_ADDRESS`
- `CITY`
- `STATE_PROVINCE`
- `POSTAL_CODE`
- `COUNTRY_CODE`
- `ORDER_STATUS`
- `ORDER_DATE`

```sql
SELECT 
    oh.order_id, 
    or_role.party_id, pa.address1,
    p.first_name,
    pa.address1,
    pa.city,
    pa.postal_code,
    pa.country_geo_id,
    pa.state_province_geo_id,
    oh.status_id,
    oh.order_date
FROM order_header oh 
join order_status os on oh.ORDER_ID = os.ORDER_ID and (os.STATUS_ID = 'ORDER_CREATED' or
os.STATUS_ID = 'ORDER_COMPLETED')
and os.STATUS_DATETIME >= '2023-10-01' and os.STATUS_DATETIME <= '2023-10-30'
JOIN order_contact_mech ocm 
    ON ocm.order_id = oh.order_id 
    AND ocm.contact_mech_purpose_type_id = 'SHIPPING_LOCATION'
LEFT JOIN order_role or_role 
    ON oh.order_id = or_role.order_id 
    AND or_role.role_type_id = 'SHIP_TO_CUSTOMER'
LEFT JOIN person p 
    ON or_role.party_id = p.party_id
LEFT JOIN postal_address pa 
    ON ocm.contact_mech_id = pa.contact_mech_id;
```
---

### 5.2 Orders from New York

**Business Problem:**
> Companies often want region-specific analysis to plan local marketing, staffing, or promotions in certain areas—here, specifically, New York.

**Fields to Retrieve:**
- `ORDER_ID`
- `CUSTOMER_NAME`
- `STREET_ADDRESS (or shipping address detail)`
- `CITY`
- `STATE_PROVINCE`
- `POSTAL_CODE`
- `TOTAL_AMOUNT`
- `ORDER_DATE`
- `ORDER_STATUS`

```sql
SELECT 
    oh.order_id, 
    p.first_name,
    pa.address1,
    pa.city,
    pa.state_province_geo_id,
    pa.postal_code,
    oh.grand_total,
    oh.status_id,
    oh.order_date
FROM order_header oh 
JOIN order_contact_mech ocm 
    ON ocm.order_id = oh.order_id 
    AND ocm.contact_mech_purpose_type_id = 'SHIPPING_LOCATION'
    AND (oh.status_id = "ORDER_COMPLETED")
LEFT JOIN order_role or_role 
    ON oh.order_id = or_role.order_id 
    AND or_role.role_type_id = 'SHIP_TO_CUSTOMER'
LEFT JOIN person p
    ON or_role.party_id = p.party_id
LEFT JOIN postal_address pa 
    ON ocm.contact_mech_id = pa.contact_mech_id 
    where pa.city = "New York";
```
---

### 5.3 Top-Selling Product in New York

**Business Problem:**
> Merchandising teams need to identify the best-selling product(s) in a specific region (New York) for targeted restocking or promotions.

**Fields to Retrieve:**
- `PRODUCT_ID`
- `INTERNAL_NAME`
- `TOTAL_QUANTITY_SOLD`
- `CITY / STATE (within New York region)`
- `REVENUE (optionally, total sales amount)`

```sql
select oi.product_id, 
p.internal_name,
count(*) as TOTAL_QUANTITY_SOLD,
pa.city
from order_item oi 
join product p on oi.product_id = p.product_id
JOIN order_contact_mech ocm 
    ON ocm.order_id = oi.order_id 
    AND ocm.contact_mech_purpose_type_id = 'SHIPPING_LOCATION'
    AND (oi.status_id = "ITEM_COMPLETED")
JOIN postal_address pa 
    ON ocm.contact_mech_id = pa.contact_mech_id 
    and pa.STATE_PROVINCE_GEO_ID = 'NY'
group by oi.product_id,
p.internal_name, 
    pa.city 
order by TOTAL_QUANTITY_SOLD desc;
```
---

### 7.3 Store-Specific (Facility-Wise) Revenue

**Business Problem:**
> Different physical or online stores (facilities) may have varying levels of performance. The business wants to compare revenue across facilities for sales planning and budgeting.

**Fields to Retrieve:**
- `FACILITY_ID`
- `FACILITY_NAME`
- `TOTAL_ORDERS`
- `TOTAL_REVENUE`
- `DATE_RANGE`

```sql
select oisg.facility_id, 
f.facility_name,
count(oi.order_item_seq_id) as TOTAL_ORDERS,
sum(oi.unit_price) as TOTAL_REVENUE_PER_FACILITY,
SUM(SUM(oi.unit_price)) OVER() AS GRAND_TOTAL_REVENUE
from order_item_ship_group oisg
join order_item oi on oisg.ship_group_seq_id = oi.ship_group_seq_id 
and oi.status_id = "ITEM_COMPLETED"
JOIN order_header oh 
    ON oi.order_id = oh.order_id 
    AND oh.order_date >= '2026-01-01 00:00:00'
  AND oh.order_date <= '2026-05-05 00:00:00'
left join facility f on f.facility_id = oisg.facility_id
group by facility_id
```
---

### 8.1 Lost and Damaged Inventory

**Business Problem:**
> Warehouse managers need to track “shrinkage” such as lost or damaged inventory to reconcile physical vs. system counts.

**Fields to Retrieve:**
- `INVENTORY_ITEM_ID`
- `PRODUCT_ID`
- `FACILITY_ID`
- `QUANTITY_LOST_OR_DAMAGED`
- `REASON_CODE (Lost, Damaged, Expired, etc.)`
- `TRANSACTION_DATE`

```sql
SELECT ii.INVENTORY_ITEM_ID,
ii.PRODUCT_ID,
ii.FACILITY_ID,
sum(iiv.Quantity_On_Hand_Var) as QUANTITY_LOST_OR_DAMAGED,
iiv.reason_enum_id as REASON_CODE,
iiv.created_tx_stamp AS TRANSACTION_DATE
from inventory_item ii 
join inventory_item_variance iiv on ii.INVENTORY_ITEM_ID = iiv.INVENTORY_ITEM_ID and iiv.reason_enum_id in ("VAR_DAMAGED", "VAR_STOLEN", "VAR_LOST")
group by ii.inventory_item_id , iiv.reason_enum_id
```
