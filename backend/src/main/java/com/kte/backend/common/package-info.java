/**
 * Shared kernel: generic building blocks (paging, CRUD contract, base entity) used by every
 * business module. Declared as an {@link org.springframework.modulith.ApplicationModule.Type#OPEN}
 * module so any module can depend on it freely and it stays out of the cycle-detection checks.
 */
@org.springframework.modulith.ApplicationModule(type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package com.kte.backend.common;
