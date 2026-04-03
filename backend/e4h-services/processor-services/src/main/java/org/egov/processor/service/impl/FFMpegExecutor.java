package org.egov.processor.service.impl;


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
        log.trace("Method invoked: executeCommand");
        log.debug("Executing FFmpeg command");
        Process process = null;
        try {
            log.trace("Starting process execution");
            process = Runtime.getRuntime().exec(command);

            StringBuilder output = new StringBuilder();
            StringBuilder error = new StringBuilder();

            // Create readers for stdout and stderr
            try (BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

                log.trace("Reading process output streams");
                String line;
                while ((line = stdInput.readLine()) != null) {
                    output.append(line).append("\n");
                }

                while ((line = stdError.readLine()) != null) {
                    error.append(line).append("\n");
                }
            }

            // Wait for the command to finish and capture the exit code
            log.trace("Waiting for process to complete");
            int exitCode = process.waitFor();
            log.debug("Process completed with exit code: {}", exitCode);

            // Log the output and error
            if (output.length() > 0) {
                log.debug("Command output length: {} characters", output.length());
            }
            if (error.length() > 0) {
                log.warn("Command error output length: {} characters", error.length());
            }

            if (exitCode != 0) {
                log.error("Command failed with exit code: {}", exitCode);
                log.debug("Error output: {}", error.toString());
                throw new CustomException("Command failed:",
                        String.format("Command failed with exit code: %d. Error details: %s", exitCode, error));
            }

            log.debug("Command completed successfully");
        } catch (IOException e) {
            log.error("IOException while executing command", e);
            throw new CustomException(String.format("IOException while executing command: %s", command), e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread was interrupted while executing command", e);
            throw new CustomException(String.format("Thread was interrupted while executing command: %s", command), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error executing command", e);
            throw new CustomException(String.format("Error executing command: %s", command), e.getMessage());
        } finally {
            if (process != null) {
                log.trace("Destroying process");
                process.destroy();
            }
        }
    }

}

