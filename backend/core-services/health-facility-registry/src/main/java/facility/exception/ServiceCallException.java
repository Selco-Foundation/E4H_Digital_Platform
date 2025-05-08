package facility.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class ServiceCallException extends RuntimeException {
    private final Map<String, String> errors;

    public ServiceCallException(Map<String, String> errors) {
        super("Custom Exception occurred");
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}