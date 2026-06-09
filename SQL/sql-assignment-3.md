# SQL Assignment 3

## Questions & Solutions

### 1. Completed Sales Orders (Physical Items)

**Business Problem:**
> Merchants need to track only physical items (requiring shipping and fulfillment) for logistics and shipping-cost analysis.

**Fields to Retrieve:**
- `ORDER_ID`
- `ORDER_ITEM_SEQ_ID`
- `PRODUCT_ID`
- `PRODUCT_TYPE_ID`
- `SALES_CHANNEL_ENUM_ID`
- `ORDER_DATE`
- `ENTRY_DATE`
- `STATUS_ID`
- `STATUS_DATETIME`
- `ORDER_TYPE_ID`
- `PRODUCT_STORE_ID`

```sql
select 
oh.ORDER_ID,
oi.ORDER_ITEM_SEQ_ID,
oi.PRODUCT_ID,
p.PRODUCT_TYPE_ID,
oh.SALES_CHANNEL_ENUM_ID,
oh.ORDER_DATE,
oh.ENTRY_DATE,
oh.STATUS_ID,
os.STATUS_DATETIME,
oh.ORDER_TYPE_ID,
oh.PRODUCT_STORE_ID
from order_header oh
join order_item oi on oi.order_id = oh.order_id 
join product p on p.product_id=oi.product_id
join (select STATUS_DATETIME, order_id from order_status  where status_id = "ORDER_COMPLETED") os on oh.order_id= os.order_id
where oh.order_type_id = "SALES_ORDER"
```
---

### 2. Completed Return Items

**Business Problem:**
> Customer service and finance often need insights into returned items to manage refunds, replacements, and inventory restocking.

**Fields to Retrieve:**
- `RETURN_ID`
- `ORDER_ID`
- `PRODUCT_STORE_ID`
- `STATUS_DATETIME`
- `ORDER_NAME`
- `FROM_PARTY_ID`
- `RETURN_DATE`
- `ENTRY_DATE`
- `RETURN_CHANNEL_ENUM_ID`

```sql
select
rh.RETURN_ID,
ri.ORDER_ID,
oh.PRODUCT_STORE_ID,
rs.STATUS_DATETIME,
oh.ORDER_NAME,
rh.FROM_PARTY_ID,
rh.RETURN_DATE,
rh.ENTRY_DATE,
rh.RETURN_CHANNEL_ENUM_ID
from return_header rh 
join return_item ri on rh.return_id = ri.return_id
join order_header oh on oh.order_id = ri.order_id
join return_status rs on ri.return_item_seq_id = rs.return_item_seq_id and rs.status_id = "RETURN_COMPLETED"
```
---

### 3. Single-Return Orders (Last Month)

**Business Problem:**
> The mechandising team needs a list of orders that only have one return.

**Fields to Retrieve:**
- `PARTY_ID`
- `FIRST_NAME`

```sql
with single_item_returns as(
select rh.return_id, ri.order_id, count(*)
from return_header rh 
join return_item ri on ri.return_id = rh.return_id
group by ri.order_id
having count(*) = 1
)
select p.party_id, p.first_name
from order_role odr 
join single_item_returns sir on sir.order_id = odr.order_id
join person p on odr.role_type_id="PLACING_CUSTOMER" and odr.party_id = p.party_id
```
---

### 4. Returns and Appeasements

**Business Problem:**
> The retailer needs the total amount of items, were returned as well as how many appeasements were issued.

**Fields to Retrieve:**
- `TOTAL RETURNS`
- `RETURN $ TOTAL`
- `TOTAL APPEASEMENTS`
- `APPEASEMENTS $ TOTAL`

```sql
SELECT
    SUM(ri.return_quantity) AS TOTAL_RETURNS,
    SUM(ri.return_price * ri.return_quantity) AS RETURN_$_TOTAL,

    (
        SELECT COUNT(*)
        FROM return_adjustment ra
        WHERE ra.return_adjustment_type_id = 'APPEASEMENT'
    ) AS TOTAL_APPEASEMENTS,
    (
        SELECT SUM(ra.amount)
        FROM return_adjustment ra
        WHERE ra.return_adjustment_type_id = 'APPEASEMENT'
    ) AS APPEASEMENTS_$_TOTAL

FROM return_item ri
JOIN return_header rh 
    ON rh.return_id = ri.return_id
WHERE rh.status_id NOT IN ('RETURN_CANCELLED')
```
---

### 5. Detailed Return Information

**Business Problem:**
> Certain teams need granular return data (reason, date, refund amount) for analyzing return rates, identifying recurring issues, or updating policies.

**Fields to Retrieve:**
- `RETURN_ID`
- `ENTRY_DATE`
- `RETURN_ADJUSTMENT_TYPE_ID (refund type, store credit, etc.)`
- `AMOUNT`
- `COMMENTS`
- `ORDER_ID`
- `ORDER_DATE`
- `RETURN_DATE`
- `PRODUCT_STORE_ID`

```sql
SELECT
rh.RETURN_ID,
rh.ENTRY_DATE,
ra.RETURN_ADJUSTMENT_TYPE_ID, 
ra.AMOUNT,
ra.COMMENTS,
ra.ORDER_ID,
oh.ORDER_DATE,
rh.RETURN_DATE,
oh.PRODUCT_STORE_ID
FROM return_header rh
left JOIN return_adjustment ra ON rh.return_id = ra.return_id
LEFT join order_header oh on oh.order_id = ra.order_id
```
---

### 6. Orders with Multiple Returns

**Business Problem:**
> Analyzing orders with multiple returns can identify potential fraud, chronic issues with certain items, or inconsistent shipping processes.

**Fields to Retrieve:**
- `ORDER_ID`
- `RETURN_ID`
- `RETURN_DATE`
- `RETURN_REASON`
- `RETURN_QUANTITY`

```sql
select 
ri.ORDER_ID,
rh.RETURN_ID,
rh.RETURN_DATE,
ri.RETURN_REASON_id AS RETURN_REASON,
sum(ri.return_quantity) as RETURN_QUANTITY
from return_header rh 
join return_item ri on ri.return_id = rh.return_id
group by ri.order_id, rh.return_id, rh.return_date, ri.return_reason_id
having RETURN_QUANTITY > 1
```
---

### 7. Store with Most One-Day Shipped Orders (Last Month)

**Business Problem:**
> Identify which facility (store) handled the highest volume of “one-day shipping” orders in the previous month, useful for operational benchmarking.

**Fields to Retrieve:**
- `FACILITY_ID`
- `FACILITY_NAME`
- `TOTAL_ONE_DAY_SHIP_ORDERS`
- `REPORTING_PERIOD`

```sql
select
s.origin_FACILITY_ID AS FACILITY_ID,
f.FACILITY_NAME,
count(distinct s.primary_order_id) as TOTAL_ONE_DAY_SHIP_ORDERS,
CURDATE() as REPORTING_PERIOD
from shipment s 
join facility f on f.facility_id = s.origin_FACILITY_ID
join shipment_route_segment srs on s.shipment_id = srs.shipment_id and srs.shipment_method_type_id = "NEXT_DAY"
group by s.origin_FACILITY_ID, f.FACILITY_NAME
```
---

### 8. List of Warehouse Pickers

**Business Problem:**
> Warehouse managers need a list of employees responsible for picking and packing orders to manage shifts, productivity, and training needs.

**Fields to Retrieve:**
- `PARTY_ID (or Employee ID)`
- `NAME (First/Last)`
- `ROLE_TYPE_ID (e.g., “WAREHOUSE_PICKER”)`
- `FACILITY_ID (assigned warehouse)`
- `STATUS (active or inactive employee)`

```sql
select 
fp.PARTY_ID,
p.first_name,
p.last_name,
fp.ROLE_TYPE_ID,
fp.FACILITY_ID,
case when fp.thru_date < curDate() then "Inactive" else "Active" end as STATUS
from facility_party fp
join person p on p.party_id = fp.party_id and fp.role_type_id = "WAREHOUSE_PICKER"
```
---

### 9. Total Facilities That Sell the Product

**Business Problem:**
> Retailers want to see how many (and which) facilities (stores, warehouses, virtual sites) currently offer a product for sale.

**Fields to Retrieve:**
- `PRODUCT_ID`
- `PRODUCT_NAME (or INTERNAL_NAME)`
- `FACILITY_COUNT (number of facilities selling the product)`
- `(Optionally) a list of FACILITY_IDs if more detail is needed`

```sql
select
pf.PRODUCT_ID,
p.PRODUCT_NAME,
COUNT(pf.facility_id) OVER(PARTITION BY pf.product_id) as FACILITY_COUNT,
pf.facility_id,
f.Facility_Type_Id
from product_facility pf 
join product p on pf.product_id = p.product_id
join facility f on f.facility_id = pf.facility_id
```
---

### 10. Total Items in Various Virtual Facilities

**Business Problem:**
> Retailers need to study the relation of inventory levels of products to the type of facility it's stored at. Retrieve all inventory levels for products at locations and include the facility type Id. Do not retrieve facilities that are of type Virtual.

**Fields to Retrieve:**
- `PRODUCT_ID`
- `FACILITY_ID`
- `FACILITY_TYPE_ID`
- `QOH (Quantity on Hand)`
- `ATP (Available to Promise)`

```sql
select 
ii.product_id,
ii.facility_id,
f.facility_type_id,
ii.Available_To_Promise_Total as ATP,
ii.Quantity_On_Hand_Total as QOH
from inventory_item ii 
join facility f on f.facility_id = ii.facility_id 
```
---

### 11. Transfer Orders Without Inventory Reservation

**Business Problem:**
> When transferring stock between facilities, the system should reserve inventory. If it isn’t reserved, the transfer may fail or oversell.

**Fields to Retrieve:**
- `TRANSFER_ORDER_ID`
- `FROM_FACILITY_ID`
- `TO_FACILITY_ID`
- `PRODUCT_ID`
- `REQUESTED_QUANTITY`
- `RESERVED_QUANTITY`
- `TRANSFER_DATE`
- `STATUS`

```sql
select 
oh.ORDER_ID AS TRANSFER_ORDER_ID,
s.origin_FACILITY_ID AS FROM_FACILITY_ID,
s.destination_FACILITY_ID AS TO_FACILITY_ID,
oi.PRODUCT_ID,
oi.QUANTITY AS REQUESTED_QUANTITY,
oisgir.quantity as RESERVED_QUANTITY,
s.Estimated_Ship_Date as TRANSFER_DATE,
oh.STATUS_id AS STATUS
from order_header oh 
join order_item oi on oi.order_id = oh.order_id
left join order_item_ship_grp_inv_res oisgir on oisgir.order_id = oi.order_id and oisgir.order_item_seq_id = oi.order_item_seq_id
join shipment s on s.primary_order_id = oh.order_id
where
(oh.status_id not in ('ORDER_CANCELLED', 'ORDER_COMPLETED')) and 
(oh.Order_Type_Id = "TRANSFER_ORDER")
```
---

### 12. Orders Without Picklist

**Business Problem:**
> A picklist is necessary for warehouse staff to gather items. Orders missing a picklist might be delayed and need attention.

**Fields to Retrieve:**
- `ORDER_ID`
- `ORDER_DATE`
- `ORDER_STATUS`
- `FACILITY_ID`
- `DURATION (How long has the order been assigned at the facility)`

```sql
select 
s.primary_ORDER_ID AS ORDER_ID,
oh.Order_Date as ORDER_DATE,
oh.status_id as ORDER_STATUS,
s.origin_FACILITY_ID AS FACILITY_ID,
ps.picklist_id,
TIMESTAMPDIFF(HOUR, oh.Order_Date, NOW()) AS DURATION
from shipment s
join order_header oh on oh.order_id = s.primary_order_id
left join picklist_shipment ps on ps.shipment_id = s.shipment_id 
JOIN FACILITY F ON F.FACILITY_ID = s.origin_FACILITY_ID
WHERE
   OH.STATUS_ID = 'ORDER_APPROVED'
  AND F.FACILITY_TYPE_ID NOT IN (
      SELECT FACILITY_TYPE_ID
      FROM FACILITY_TYPE
      WHERE PARENT_TYPE_ID = 'VIRTUAL_FACILITY'
  )
and ps.picklist_id is NULL
```
