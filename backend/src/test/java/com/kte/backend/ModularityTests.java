package com.kte.backend;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Verifies that the application's module structure (user / catalog / transaction, plus the
 * open shared modules) respects Spring Modulith's constraints - no cycles between modules,
 * and no module reaching into another module's internals.
 */
class ModularityTests {

    private static final ApplicationModules modules = ApplicationModules.of(BackendApplication.class);

    @Test
    void verifyModuleStructure() {
        modules.verify();
    }

    @Test
    void printModuleStructure() {
        modules.forEach(System.out::println);
    }
}
