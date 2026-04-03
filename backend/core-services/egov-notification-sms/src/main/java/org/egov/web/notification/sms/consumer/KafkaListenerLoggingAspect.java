package org.egov.web.notification.sms.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//@Aspect
//@Component
@Slf4j
public class KafkaListenerLoggingAspect {

    private static final String RECEIVED_MESSAGE = "Received message from topics: {} with body {}";
    private static final String PROCESSED_SUCCESS_MESSAGE = "Processed message successfully";
    private static final String EXCEPTION_MESSAGE = "Exception processing message";
    private static final String PROPERTY_PLACEHOLDER_PREFIX = "${";
    private static final String PROPERTY_PLACEHOLDER_SUFFIX = "}";
    private static final String REPLACEMENT_STRING = "";
    private static final String JOIN_DELIMITER = ", ";
    private Environment environment;
    private ObjectMapper objectMapper;

    public KafkaListenerLoggingAspect(Environment environment) {
        this.environment = environment;
        this.objectMapper = new ObjectMapper();
    }

    @Pointcut(value = " within(org.egov..*) && @annotation(org.springframework.kafka.annotation.KafkaListener)")
    public void anyKafkaConsumer() {
    }

    @Around("anyKafkaConsumer() ")
    public Object logAction(ProceedingJoinPoint pjp) throws Throwable {
        log.trace("logAction aspect method invoked for Kafka listener");
        final Object[] args = pjp.getArgs();
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        KafkaListener myAnnotation = method.getAnnotation(KafkaListener.class);
        try {
            if (log.isInfoEnabled()) {
                final String topics = getListeningTopics(myAnnotation);
                final String messageBodyAsString = getMessageBodyAsString(args);
                log.info(RECEIVED_MESSAGE, topics, messageBodyAsString);
                log.debug("Kafka listener method: {}, topics: {}", method.getName(), topics);
            }
            final Object result = pjp.proceed();
            log.info(PROCESSED_SUCCESS_MESSAGE);
            log.debug("Kafka message processing completed successfully for method: {}", method.getName());
            return result;
        } catch (Exception e) {
            log.error(EXCEPTION_MESSAGE, e);
            log.debug("Exception occurred in Kafka listener method: {}", method.getName());
            throw e;
        }
    }

    private String getMessageBodyAsString(Object[] args) throws JsonProcessingException {
        log.trace("getMessageBodyAsString method invoked");
        String result = objectMapper.writeValueAsString(getMessageBody(args));
        log.debug("Message body serialized to string, length: {}", result != null ? result.length() : 0);
        return result;
    }

    private Object getMessageBody(Object[] args) {
        log.trace("getMessageBody method invoked");
        Object result = Stream.of(args)
                .filter(parameterObject -> isNotAcknowledgmentParameter(parameterObject) && isNotString(parameterObject))
                .findFirst()
                .map(parameterObject -> parameterObject)
                .orElse(null);
        log.debug("Message body extracted, type: {}", result != null ? result.getClass().getSimpleName() : "null");
        return result;
    }

    private boolean isNotString(Object o) {
        return !(o instanceof String);
    }

    private boolean isNotAcknowledgmentParameter(Object o) {
        return !(o instanceof Acknowledgment);
    }

    private String getPropertyName(String propertyPlaceholder) {
        final String propertyName = propertyPlaceholder
                .replace(PROPERTY_PLACEHOLDER_PREFIX, REPLACEMENT_STRING)
                .replace(PROPERTY_PLACEHOLDER_SUFFIX, REPLACEMENT_STRING);
        return this.environment.getProperty(propertyName);
    }

    private String getListeningTopics(KafkaListener myAnnotation) {
        return Stream.of(myAnnotation.topics()).map(this::getPropertyName).collect(Collectors.joining(JOIN_DELIMITER));
    }
}
