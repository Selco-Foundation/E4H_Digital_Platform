package org.egov.im.web.controllers;

import lombok.extern.slf4j.Slf4j;
import org.egov.im.service.UserService;
import org.egov.im.web.models.UserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/user/login")
public class UserLoginReportController {

    @Autowired
    private UserService userService;

    @PostMapping(value = "/_report")
    @ResponseBody
    public ResponseEntity<?> loginReport(@RequestBody @Valid UserRequest userRequest) {
        log.trace("UserLoginReportController::loginReport method invoked");
        String username = userRequest.getUser() != null ? userRequest.getUser().getUserName() : "";
        log.info("Received login report request for user: {}", username);
        try {
            userService.loginReport(userRequest);
            log.info("Login report completed successfully for user: {}", username);
            return ResponseEntity.ok("Login report completed successfully");
        } catch (Exception e) {
            log.error("Error during login report for user: {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login report failed. Please try again.");
        }
    }

}
