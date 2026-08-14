package com.kte.backend.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Aspect transversal qui journalise l'entrée, la sortie et les exceptions
 * des méthodes de plusieurs packages applicatifs (controllers, service, repository, mapper, security).
 * <p>
 * Chaque couche est définie par une expression {@link Pointcut} dédiée,
 * puis combinée dans {@link #appLayers()} afin de centraliser la gestion
 * du logging par expressions plutôt que par duplication de code.
 */
@Aspect
@Component
@Order(1)
@Slf4j
public class LoggingAspect {

    @Pointcut("execution(* com.kte.backend.controllers..*(..))")
    public void controllerLayer() {
    }

    @Pointcut("execution(* com.kte.backend.service..*(..))")
    public void serviceLayer() {
    }

    @Pointcut("execution(* com.kte.backend.repository..*(..))")
    public void repositoryLayer() {
    }

    @Pointcut("execution(* com.kte.backend.security..*(..))")
    public void securityLayer() {
    }

    @Pointcut("execution(* com.kte.backend.mapper..*(..))")
    public void mapperLayer() {
    }

    /**
     * Combine toutes les couches applicatives en une seule expression réutilisable.
     */
    @Pointcut("controllerLayer() || serviceLayer() || repositoryLayer() || securityLayer() || mapperLayer()")
    public void appLayers() {
    }

    @Around("appLayers()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String layer = resolveLayer(joinPoint);
        String signature = joinPoint.getSignature().toShortString();
        long start = System.currentTimeMillis();

        log.info("[{}] --> {} args={}", layer, signature, Arrays.toString(joinPoint.getArgs()));

        try {
            Object result = joinPoint.proceed();
            long elapsedMs = System.currentTimeMillis() - start;
            log.info("[{}] <-- {} ({} ms) result={}", layer, signature, elapsedMs, result);
            return result;
        } catch (Throwable ex) {
            long elapsedMs = System.currentTimeMillis() - start;
            log.error("[{}] !! {} ({} ms) threw {}: {}",
                    layer, signature, elapsedMs, ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }
    }

    /**
     * Détermine la couche applicative à partir du package de la classe cible,
     * pour préfixer les logs et faciliter le filtrage multi-package.
     */
    private String resolveLayer(JoinPoint joinPoint) {
        String declaringType = joinPoint.getSignature().getDeclaringTypeName();
        if (declaringType.startsWith("com.kte.backend.controllers")) {
            return "CONTROLLER";
        }
        if (declaringType.startsWith("com.kte.backend.service")) {
            return "SERVICE";
        }
        if (declaringType.startsWith("com.kte.backend.repository")) {
            return "REPOSITORY";
        }
        if (declaringType.startsWith("com.kte.backend.security")) {
            return "SECURITY";
        }
        if (declaringType.startsWith("com.kte.backend.mapper")) {
            return "MAPPER";
        }
        return "APP";
    }
}