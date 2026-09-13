/**
 * Cross-cutting AOP concerns (execution-time logging, exception logging) applied across every
 * module's service and controller layers. Declared
 * {@link org.springframework.modulith.ApplicationModule.Type#OPEN}.
 */
@org.springframework.modulith.ApplicationModule(type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package com.kte.backend.aspect;
