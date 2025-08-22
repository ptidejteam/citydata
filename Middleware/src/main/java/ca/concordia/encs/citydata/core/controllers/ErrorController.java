package ca.concordia.encs.citydata.core.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Show default message in case of error, or when you call "/" directly from the browser.
 * @author Gabriel C. Ullmann
 * @since 2025-08-22
 */
@RestController
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    private final String defaultMessage = "Welcome to CITYdata! For more info on how to use our API, please visit https://github.com/ptidejteam/citydata/blob/master/Middleware/README.md";

    @RequestMapping("/")
    public ResponseEntity<Map<String, String>> showWelcomeMessage() {
        Map<String, String> welcomeMessage = new HashMap<>();
        welcomeMessage.put("message", defaultMessage);
        return new ResponseEntity<>(welcomeMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @RequestMapping("/error")
    public ResponseEntity<Map<String, String>> handleError() {
        Map<String, String> error = new HashMap<>();
        error.put("message", defaultMessage);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}