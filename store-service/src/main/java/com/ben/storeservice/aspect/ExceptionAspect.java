package com.ben.storeservice.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ExceptionAspect {

    @Around("execution(* com.ben.quizappmvn.service.*.*(..))")
    public Object handleServiceExceptions(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            log.error("Exception in {}.{}",
                    joinPoint.getSignature().getDeclaringTypeName(),joinPoint.getSignature().getName(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}