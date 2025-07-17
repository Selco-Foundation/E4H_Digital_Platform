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
        try {
            userService.loginReport(userRequest);
            return ResponseEntity.ok("Login report completed successfully");
        } catch (Exception e) {
            String username = userRequest.getUser() != null ? userRequest.getUser().getUserName() : "";
            log.error("Error during login report for user: {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login report failed. Please try again.");
        }
    }

}
