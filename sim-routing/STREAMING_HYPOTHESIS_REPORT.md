# Findings Note: Empirical Test of Moqui MySQL Streaming

## 1. Verdict
**H is REFUTED.** 
Against a MySQL source with `useCursorFetch=false` (standard JDBC behavior), the JVM heap spiked violently before the first row was consumed, resulting in an `OutOfMemoryError`. The effective fetch size of 100 coerced by the framework is ignored by MySQL's Connector/J, causing the driver to buffer the entire result set in memory up front. The iterator is lazy only at the API surface, not in memory.

## 2. Main Test Numbers (`-Xmx256m`, `useCursorFetch=false`)
* **Rows Attempted:** 4,194,304 (from `test_large_entity`)
* **Time-to-first-row:** N/A (Failed before first row could be returned)
* **Heap Outcome:** Spiked from 64 MB to >256 MB instantly during `executeQuery()`.
* **Verdict Outcome:** **OOM Crash** (`java.lang.OutOfMemoryError: Java heap space`) triggered inside `com.mysql.cj.protocol.a.NativeProtocol.readAllResults`.

## 3. Control Test Numbers (`-Xmx256m`, `useCursorFetch=true`)
*(Note: Interestingly, we discovered that Moqui injects `useCursorFetch="true"` by default in `MoquiDefaultConf.xml`. We ran this as our conditional control case).*
* **Rows Completed:** 4,194,304
* **Create Time (Wait for first row):** **3,259 ms** *(Massive pause)*
* **Drain Time:** 4,180 ms
* **Heap Curve:** Flat throughout. Memory Before: 64 MB → At First Row: 64 MB → At End: 62 MB.
* **Verdict Outcome:** Completed without JVM OOM, but heavily refutes the "fast streaming" assumption due to the 3.2-second server-side materialization delay before the first row.

## 4. Effective Fetch Size & Connector/J Version
* **Effective Fetch Size:** `100`. (Confirmed via `EntityFindBuilder.java:783` which strictly overrides `Integer.MIN_VALUE` or any non-positive value to 100).
* **Connector/J Version:** `8.3.0` (Observed from stack trace logs: `mysql-connector-j-8.3.0.jar:8.3.0`).

## 5. Cited Connector/J Passage
According to the official MySQL Connector/J documentation regarding result set fetching:
> *"By default, ResultSets are completely retrieved and stored in memory. In most cases this is the most efficient way to operate... To enable a result set to be streamed row by row, you must ... call `Statement.setFetchSize(Integer.MIN_VALUE)`... Alternatively, to use server-side cursors, specify `useCursorFetch=true`."*

## Conclusion
"We can iterate any MySQL table safely with `.iterator()`" is **FALSE** for true streaming. 

If we use standard MySQL connection parameters, the JVM crashes with an OOM. If we rely on Moqui's hidden `useCursorFetch=true` default, we save the JVM but shift the catastrophic memory and disk burden to the MySQL server (creating massive temporary tables and extreme latency before the first row, as evidenced by the 3.2-second wait time).

Therefore, the `sim-routing` sync engine's decision to bypass the Entity Engine and use the raw JDBC path with `setFetchSize(Integer.MIN_VALUE)` is **100% required** to achieve true, zero-materialization streaming from the database.
