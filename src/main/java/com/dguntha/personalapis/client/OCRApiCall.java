package com.dguntha.personalapis.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="ocrAPICall", url = "${ocrapi.url}")
public interface OCRApiCall {

    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<String> fetchTaskId(@RequestParam("code") String code, @RequestParam("transactionId") String transactionId);
}
