package com.fundoo.notes.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around(
            "execution(* com.fundoo.notes.service..*(..))"
    )
    public Object logServiceExecution(
            ProceedingJoinPoint joinPoint)
            throws Throwable {

        String methodName =
                joinPoint.getSignature()
                        .toShortString();

        long startTime =
                System.currentTimeMillis();

        log.debug(
                "Entering method: {}",
                methodName
        );

        try {

            Object result =
                    joinPoint.proceed();

            long executionTime =
                    System.currentTimeMillis()
                            - startTime;

            log.debug(
                    "Exiting method: {} | executionTime={}ms",
                    methodName,
                    executionTime
            );

            return result;

        } catch (Exception exception) {

            log.error(
                    "Exception in method: {} | message={}",
                    methodName,
                    exception.getMessage()
            );

            throw exception;
        }
    }
}