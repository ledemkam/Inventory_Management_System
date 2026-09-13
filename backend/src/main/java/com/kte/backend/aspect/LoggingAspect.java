package com.kte.backend.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Centralises two cross-cutting concerns for the application:
 * <ul>
 *     <li>execution-time measurement of the service layer (and anything annotated
 *         with {@link com.kte.backend.annotation.LogExecutionTime});</li>
 *     <li>uniform error logging for exceptions thrown by controllers and services.</li>
 * </ul>
 * This keeps timing / logging boilerplate out of the business code.
 * <p>
 * Note: being proxy-based (Spring AOP), advice only applies to calls that go through
 * the Spring bean proxy, not to self-invocations within the same class.
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("within(com.kte.backend..services..*)")
    public void serviceLayer() {
    }

    @Pointcut("within(com.kte.backend..controllers..*)")
    public void controllerLayer() {
    }

    @Pointcut("@annotation(com.kte.backend.annotation.LogExecutionTime)"
            + " || @within(com.kte.backend.annotation.LogExecutionTime)")
    public void logExecutionTimeAnnotated() {
    }

    @Around("serviceLayer() || logExecutionTimeAnnotated()")
    public Object measureExecutionTime(final ProceedingJoinPoint joinPoint) throws Throwable {
        final String signature = joinPoint.getSignature().toShortString();
        final long startNanos = System.nanoTime();
        try {
            final Object result = joinPoint.proceed();
            log.debug("{} executed in {} ms", signature, elapsedMillis(startNanos));
            return result;
        } catch (final Throwable ex) {
            log.debug("{} failed after {} ms", signature, elapsedMillis(startNanos));
            throw ex;
        }
    }

    @AfterThrowing(pointcut = "serviceLayer() || controllerLayer()", throwing = "ex")
    public void logException(final JoinPoint joinPoint, final Throwable ex) {
        log.error("Exception in {} -> {}: {}",
                joinPoint.getSignature().toShortString(),
                ex.getClass().getSimpleName(),
                ex.getMessage());
    }

    private static long elapsedMillis(final long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
