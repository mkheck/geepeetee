package com.thehecklers.geepeeteeweb;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class GeePeeTeeController {
    private final Log log = LogFactory.getLog(GeePeeTeeController.class);

    private final GeePeeTeeService service;

    public GeePeeTeeController(GeePeeTeeService service) {
        this.service = service;
    }

    @GetMapping("/")
    public Flux<String> root(@RequestParam(required = false) String prompt) {
        try {
            return service.getData(prompt);
        } catch (JsonProcessingException e) {
            log.error("Error processing JSON: ", e);
            return Flux.empty();
        }
    }

    @GetMapping("/ping")
    public String pingpong() {
        return "pong";
    }
}
