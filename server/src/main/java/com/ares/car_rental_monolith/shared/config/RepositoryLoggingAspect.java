package com.ares.car_rental_monolith.shared.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

// Spring Boot 4 BOM không cung cấp spring-boot-starter-aop, mà aspectjweaver
// alone không tự bật @Aspect — phải khai báo @EnableAspectJAutoProxy thủ công
// ở 1 config bean để Spring tạo BeanPostProcessor weave proxy.
@EnableAspectJAutoProxy
@Aspect
@Component
public class RepositoryLoggingAspect {

    // Chỉ scope vào persistence layer trong codebase của mình. Tránh dùng
    // bean(*Adapter) vì khớp luôn cả Spring MVC infra (RequestMappingHandlerAdapter,
    // HttpRequestHandlerAdapter, …) → CGLIB warn loạt method final và có nguy cơ
    // phá dispatcher. Package modules.<X>.adapter.out.persistence cover cả
    // Load/Write/Page/Search/Stats adapter + JpaRepository interface.
    @Around("execution(* com.ares.car_rental_monolith.modules..adapter.out.persistence..*(..))")
    public Object trackPersistenceCaller(ProceedingJoinPoint joinPoint) throws Throwable {
        // Không ghi đè khi đã có context: service → Adapter → Repository giữ tên
        // Adapter (lớp ngoài cùng có ý nghĩa nghiệp vụ hơn JpaRepository sinh tự
        // động). Self-invocation trong cùng 1 adapter cũng tự nhiên rơi vào nhánh
        // này nên SQL vẫn được tag theo public entry method.
        String previous = RepositoryQueryContext.get();
        if (previous != null) {
            return joinPoint.proceed();
        }
        RepositoryQueryContext.set(resolveCallerLabel(joinPoint));
        try {
            return joinPoint.proceed();
        } finally {
            RepositoryQueryContext.clear();
        }
    }

    private String resolveCallerLabel(ProceedingJoinPoint joinPoint) {
        return resolveTypeName(joinPoint) + "#" + joinPoint.getSignature().getName();
    }

    private String resolveTypeName(ProceedingJoinPoint joinPoint) {
        Object target = joinPoint.getThis();
        if (target != null) {
            // Spring Data JPA repo: target là proxy, lấy interface khai báo trong
            // codebase của mình để tránh trả về JpaRepository/CrudRepository.
            for (Class<?> iface : target.getClass().getInterfaces()) {
                if (iface.getName().startsWith("com.ares")
                        && (iface.getSimpleName().endsWith("Repository")
                                || iface.getSimpleName().endsWith("Adapter"))) {
                    return iface.getSimpleName();
                }
            }
            // Adapter concrete (không qua interface trùng tên): strip CGLIB suffix.
            String simple = target.getClass().getSimpleName();
            int cglibIdx = simple.indexOf("$$");
            if (cglibIdx > 0) {
                simple = simple.substring(0, cglibIdx);
            }
            if (!simple.isEmpty()) {
                return simple;
            }
        }
        return joinPoint.getSignature().getDeclaringType().getSimpleName();
    }
}
