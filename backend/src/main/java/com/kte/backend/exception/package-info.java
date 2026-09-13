/**
 * Shared exception types and the global exception handler. Declared
 * {@link org.springframework.modulith.ApplicationModule.Type#OPEN} so every module can throw
 * and be handled by these without being flagged as a module dependency violation.
 */
@org.springframework.modulith.ApplicationModule(type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package com.kte.backend.exception;
