/**
 * Cross-cutting application configuration (security, CORS, caching, JPA auditing, OpenAPI).
 * Declared {@link org.springframework.modulith.ApplicationModule.Type#OPEN} since it wires
 * beans from several business modules (e.g. security) and isn't itself a business capability.
 */
@org.springframework.modulith.ApplicationModule(type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package com.kte.backend.config;
