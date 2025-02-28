package org.egov.im.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FFMpegExecutor {

    public void executeCommand(String command) {
        log.debug("Executing command: {}", command);
        try {
            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("Command failed with exit code: {}. Command: {}", exitCode, command);
                throw new CustomException("Command failed with exit code: %s",  String.valueOf(exitCode));
            }
            log.debug("Command completed successfully with exit code: {}", exitCode);
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt(); // Preserve the interruption status
                log.error("Thread was interrupted while executing command: {}", command, e);
                throw new CustomException(String.format("Thread was interrupted while executing command: %s", command), e.getMessage());
            }
            log.error("Error executing command: {}. Error: {}", command, e.getMessage(), e);
            throw new CustomException(String.format("Error executing command: %s", command), e.getMessage());
        }
    }
}
