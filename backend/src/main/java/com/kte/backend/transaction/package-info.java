/**
 * Inventory movements: purchases, sales and returns, and their lifecycle. Depends on
 * {@link com.kte.backend.user} (who performed a transaction) and {@link com.kte.backend.catalog}
 * (which product/supplier is involved).
 */
@org.springframework.modulith.ApplicationModule(displayName = "Transaction")
package com.kte.backend.transaction;
