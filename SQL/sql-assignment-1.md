# SQL Assignment 1



## Questions & Solutions

### 1. New Customers Acquired in June 2023

**Business Problem:**
> The marketing team ran a campaign in June 2023 and wants to see how many new customers signed up during that period.

```sql
SELECT
  p.PARTY_ID AS CUSTOMER_ID,
  p.FIRST_NAME,
  p.LAST_NAME,
  (SELECT cm.INFO_STRING FROM contact_mech cm 
   JOIN party_contact_mech pcm ON p.PARTY_ID = pcm.PARTY_ID AND cm.CONTACT_MECH_TYPE_ID = 'EMAIL_ADDRESS' LIMIT 1) AS EMAIL_ADDRESS,
  (SELECT tn.CONTACT_NUMBER FROM contact_mech cm 
   JOIN party_contact_mech pcm ON p.PARTY_ID = pcm.PARTY_ID AND cm.CONTACT_MECH_TYPE_ID = 'TELECOM_NUMBER'
   JOIN telecom_number tn ON tn.CONTACT_MECH_ID = pcm.CONTACT_MECH_ID LIMIT 1) AS CONTACT_NUMBER,
  p.CREATED_STAMP AS SIGNUP_DATE
FROM 
  person p
JOIN party_role pr ON pr.PARTY_ID = p.PARTY_ID 
  AND p.CREATED_STAMP >= '2023-06-01 00:00:00' 
  AND p.CREATED_STAMP <= '2023-06-30 23:59:59' 
  AND pr.ROLE_TYPE_ID = 'CUSTOMER';
```

---

### 2. List All Active Physical Products

**Business Problem:**
> Merchandising teams often need a list of all physical products to manage logistics, warehousing, and shipping.

```sql
SELECT 
  p.PRODUCT_ID, 
  p.PRODUCT_TYPE_ID, 
  p.INTERNAL_NAME,
  p.STATUS_ID AS STATUS
FROM 
  product p
WHERE 
  p.IS_VARIANT = 'Y';
```

---

### 3. Products Missing NetSuite ID

**Business Problem:**
> A product cannot sync to NetSuite unless it has a valid NetSuite ID. The OMS needs a list of all products that still need to be created or updated in NetSuite.

```sql
SELECT 
  p.PRODUCT_ID, 
  p.INTERNAL_NAME, 
  p.PRODUCT_TYPE_ID, 
  gi.ID_VALUE AS NETSUITE_ID
FROM 
  product p
LEFT JOIN good_identification gi 
  ON p.PRODUCT_ID = gi.PRODUCT_ID 
  AND gi.GOOD_IDENTIFICATION_TYPE_ID = 'ERP_ID' 
WHERE 
  gi.GOOD_IDENTIFICATION_TYPE_ID IS NULL;
```

---

### 4. Product IDs Across Systems

**Business Problem:**
> To sync an order or product across multiple systems (e.g., Shopify, HotWax, ERP/NetSuite), the OMS needs to know each system’s unique identifier for that product. This query retrieves the Shopify ID, HotWax ID, and ERP ID (NetSuite ID) for all products.

```sql
SELECT 
  p.PRODUCT_ID AS PRODUCT_ID, 
  ssp.SHOPIFY_PRODUCT_ID AS SHOPIFY_ID, 
  p.PRODUCT_ID AS HOTWAX_ID, 
  p.PRODUCT_NAME AS PRODUCT_NAME,
  gi.ID_VALUE AS ERP_ID
FROM 
  product p 
LEFT JOIN shopify_shop_product ssp
  ON ssp.PRODUCT_ID = p.PRODUCT_ID
LEFT JOIN good_identification gi 
  ON gi.PRODUCT_ID = p.PRODUCT_ID 
  AND gi.GOOD_IDENTIFICATION_TYPE_ID = 'ERP_ID';
```

---

### 5. Completed Orders in August 2023

**Business Problem:**
> After running similar reports for a previous month, you now need all completed orders in August 2023 for analysis.

```sql
SELECT 
  p.PRODUCT_ID, 
  p.PRODUCT_TYPE_ID, 
  o.PRODUCT_STORE_ID, 
  oi.QUANTITY, 
  oi.ORDER_ID, 
  oi.ORDER_ITEM_SEQ_ID, 
  oisg.FACILITY_ID, 
  oi.EXTERNAL_ID,
  oh.ORDER_HISTORY_ID, 
  oisg.SHIP_GROUP_SEQ_ID  
FROM 
  order_item oi
JOIN order_status os 
  ON os.ORDER_ID = oi.ORDER_ID
  AND os.STATUS_ID = 'ORDER_COMPLETED' 
  AND os.STATUS_DATETIME >= '2023-08-01 00:00:00'
  AND os.STATUS_DATETIME <= '2023-08-31 23:59:59'
JOIN order_item_ship_group oisg 
  ON oi.ORDER_ID = oisg.ORDER_ID 
  AND oi.SHIP_GROUP_SEQ_ID = oisg.SHIP_GROUP_SEQ_ID
JOIN product p 
  ON oi.PRODUCT_ID = p.PRODUCT_ID 
LEFT JOIN order_header o 
  ON oi.ORDER_ID = o.ORDER_ID
JOIN order_history oh 
  ON oi.ORDER_ID = oh.ORDER_ID 
  AND oi.ORDER_ITEM_SEQ_ID = oh.ORDER_ITEM_SEQ_ID;
```

---

### 6. Newly Created Sales Orders and Payment Methods

**Business Problem:**
> Finance teams need to see new orders and their payment methods for reconciliation and fraud checks.

**Fields to Retrieve:**
- `ORDER_ID`
- `TOTAL_AMOUNT`
- `PAYMENT_METHOD`
- `Shopify Order ID (if applicable)`

```sql
SELECT 
  oh.ORDER_ID, 
  oh.GRAND_TOTAL AS TOTAL_AMOUNT, 
  (SELECT opp.PAYMENT_METHOD_TYPE_ID 
   FROM order_payment_preference opp 
   WHERE opp.ORDER_ID = oh.ORDER_ID 
   LIMIT 1) AS PAYMENT_METHOD, 
  oh.EXTERNAL_ID AS SHOPIFY_ORDER_ID
FROM 
  order_header oh;
```

---

### 7. Payment Captured but Not Shipped

**Business Problem:**
> Finance teams want to ensure revenue is recognized properly. If payment is captured but no shipment has occurred, it warrants further review.

```sql
SELECT 
  oh.order_id AS ORDER_ID, 
  oh.status_id AS ORDER_STATUS, 
  opp.status_id AS PAYMENT_STATUS,
  opp.max_amount AS PAYMENT_AMOUNT,
  (SELECT s.status_id 
   FROM shipment s 
   WHERE s.primary_order_id = oh.order_id 
   LIMIT 1) AS SHIPMENT_STATUS
FROM 
  order_header oh
JOIN order_payment_preference opp 
  ON opp.order_id = oh.order_id;
```

---

### 8. Orders Completed Hourly

**Business Problem:**
> Operations teams may want to see how orders complete across the day to schedule staffing.

```sql
SELECT 
  HOUR(os.STATUS_DATETIME) AS hour, 
  COUNT(*) AS total_orders
FROM 
  order_status os
WHERE 
  os.STATUS_ID = 'ORDER_COMPLETED'
  AND os.STATUS_DATETIME >= '2026-05-20 00:00:00' 
  AND os.STATUS_DATETIME < '2026-05-21 00:00:00'
GROUP BY 
  hour;
```

---

### 9. BOPIS Orders Revenue (Last Year)

**Business Problem:**
> BOPIS (Buy Online, Pickup In Store) is a key retail strategy. Finance wants to know the revenue from BOPIS orders for the previous year.

```sql
SELECT 
  COUNT(*) AS TOTAL_ORDERS, 
  SUM(oh.GRAND_TOTAL) AS TOTAL_REVENUE
FROM 
  order_header oh 
JOIN shipment s 
  ON oh.ORDER_ID = s.PRIMARY_ORDER_ID 
  AND s.SHIPMENT_METHOD_TYPE_ID = 'STOREPICKUP'
  AND oh.SALES_CHANNEL_ENUM_ID = 'WEB_SALES_CHANNEL' 
  AND oh.ORDER_DATE >= '2023-01-01 00:00:00' 
  AND oh.ORDER_DATE <= '2023-12-31 00:00:00';
```

---

### 10. Canceled Orders (Last Month)

**Business Problem:**
> The merchandising team needs to know how many orders were canceled in the previous month and their reasons.

```sql
SELECT 
  os.CHANGE_REASON,
  COUNT(*) AS TOTAL_CANCELLED
FROM 
  order_status os 
WHERE 
  os.STATUS_ID = 'ORDER_CANCELLED' 
  AND os.CHANGE_REASON IS NOT NULL
GROUP BY
  os.CHANGE_REASON;
```
