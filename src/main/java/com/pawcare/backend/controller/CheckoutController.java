package com.pawcare.backend.controller;

import com.pawcare.backend.dto.CheckoutRequest;
import com.pawcare.backend.entity.Checkout;
import com.pawcare.backend.exception.CheckoutException;
import com.pawcare.backend.service.CheckoutService;

import jakarta.validation.Valid;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checkout")
@CrossOrigin(origins = "http://localhost:5173")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(
            CheckoutService checkoutService
    ) {
        this.checkoutService = checkoutService;
    }


    @PostMapping
    public ResponseEntity<byte[]> checkout(
            @Valid @RequestBody CheckoutRequest request
    ) {

        try {

            byte[] pdf =
                    checkoutService.processCheckout(request);

            String fileName =
                    "PawCare-Invoice-"
                            + System.currentTimeMillis()
                            + ".pdf";

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setContentType(
                    MediaType.APPLICATION_PDF
            );

            headers.setContentDisposition(
                    ContentDisposition
                            .attachment()
                            .filename(fileName)
                            .build()
            );

            headers.setContentLength(
                    pdf.length
            );

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(pdf);

        } catch (CheckoutException e) {

            return ResponseEntity
                    .badRequest()
                    .contentType(
                            MediaType.TEXT_PLAIN
                    )
                    .body(
                            e.getMessage()
                                    .getBytes()
                    );
        }
    }


    @GetMapping("/sales")
    public ResponseEntity<List<Checkout>> getAllSales() {

        List<Checkout> sales =
                checkoutService.getAllSales();

        return ResponseEntity.ok(sales);
    }
}