package com.flash.sanitization.sanitizer.metrics.annotation;

import com.flash.sanitization.sanitizer.metrics.SanitizerMetricBundle;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Aspect which contains the AOP code {@link SanitizerMeasure}.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class SanitizerMeasureAspect {

    /**
     * measure used for creating / recording durations.
     */
    private final SanitizerMetricBundle sanitizerMetricBundle;

    /**
     * Will record/create a measure for how long a method annotated with {@link SanitizerMeasure} took to execute
     * and how may times it executed.
     */
    @Around(value = "@annotation(restSanitizerMeasureAnnotation)")
    public Object timeMeasures(ProceedingJoinPoint joinPoint, SanitizerMeasure restSanitizerMeasureAnnotation) throws Throwable {

        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        Object[] methodArgs = joinPoint.getArgs();

        Timer.Sample timer = Timer.start();

        try {
            Object returnValue = joinPoint.proceed();

            sanitizerMetricBundle.recordSuccess(className, methodName, methodArgs);
            return returnValue;
        }
        catch(Throwable throwable) {
            sanitizerMetricBundle.recordError(className, methodName, throwable.getClass().getSimpleName(), methodArgs);
            throw throwable;
        }
        finally
        {
            sanitizerMetricBundle.recordTime(className, methodName, timer);
        }
    }
}
