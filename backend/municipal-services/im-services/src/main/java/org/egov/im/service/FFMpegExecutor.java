package org.egov.im.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
@Service
public class FFMpegExecutor {

    public void executeCommand(String command) {
        log.debug("Executing command: {}", command);
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command);

            StringBuilder output = new StringBuilder();
            StringBuilder error = new StringBuilder();

            // Create readers for stdout and stderr
            try (BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

                String line;
                while ((line = stdInput.readLine()) != null) {
                    output.append(line).append("\n");
                }

                while ((line = stdError.readLine()) != null) {
                    error.append(line).append("\n");
                }
            }

            // Wait for the command to finish and capture the exit code
            int exitCode = process.waitFor();

            // Log the output and error
            if (output.length() > 0) {
                log.debug("Command output: {}", output.toString());
            }
            if (error.length() > 0) {
                log.error("Command error output: {}", error.toString());
            }

            if (exitCode != 0) {
                log.error("Command failed with exit code: {}. Command: {}", exitCode, command);
                log.error("Error details: {}", error.toString());
                throw new CustomException("Command failed:",
                        String.format("Command failed with exit code: %d. Error details: %s", exitCode, error));
            }

            log.debug("Command completed successfully with exit code: {}", exitCode);
        } catch (IOException e) {
            log.error("IOException while executing command: {}. Error: {}", command, e.getMessage(), e);
            throw new CustomException(String.format("IOException while executing command: %s", command), e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread was interrupted while executing command: {}", command, e);
            throw new CustomException(String.format("Thread was interrupted while executing command: %s", command), e.getMessage());
        } catch (Exception e) {
            log.error("Error executing command: {}. Error: {}", command, e.getMessage(), e);
            throw new CustomException(String.format("Error executing command: %s", command), e.getMessage());
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

}
