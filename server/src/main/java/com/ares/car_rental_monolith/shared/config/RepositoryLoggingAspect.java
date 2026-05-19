package com.ares.car_rental_monolith.shared.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RepositoryLoggingAspect {

    // Intercepts all Spring beans whose name ends with "Repository".
    // Each matched bean is a Spring Data JPA proxy; getThis() returns the proxy
    // object whose interfaces include the concrete repository interface (e.g. VehicleJpaRepository).
    @Around("bean(*Repository)")
    public Object trackRepository(ProceedingJoinPoint joinPoint) throws Throwable {
        RepositoryQueryContext.set(resolveRepositoryName(joinPoint));
        try {
            return joinPoint.proceed();
        } finally {
            RepositoryQueryContext.clear();
        }
    }

    private String resolveRepositoryName(ProceedingJoinPoint joinPoint) {
        // The proxy's own interfaces include the concrete repository interface.
        // We pick the one declared in our own codebase to avoid returning
        // JpaRepository / CrudRepository / etc. from the Spring framework.
        for (Class<?> iface : joinPoint.getThis().getClass().getInterfaces()) {
            if (iface.getName().startsWith("com.ares")
                    && iface.getSimpleName().endsWith("Repository")) {
                return iface.getSimpleName();
            }
        }
        // Fallback: use the declaring type of the called method
        return joinPoint.getSignature().getDeclaringType().getSimpleName();
    }
}
