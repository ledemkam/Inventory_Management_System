/**
 * Service contracts are the {@code user} module's public behavioural API. Declared as a
 * {@link org.springframework.modulith.NamedInterface} so other modules can depend on
 * {@link com.kte.backend.user.services.UserService} (the implementation, in
 * {@code user.internal.services.impl}, stays hidden).
 */
@org.springframework.modulith.NamedInterface
package com.kte.backend.user.services;
