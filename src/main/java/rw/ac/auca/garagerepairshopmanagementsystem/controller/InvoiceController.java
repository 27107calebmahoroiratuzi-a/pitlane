package rw.ac.auca.garagerepairshopmanagementsystem.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.InvoiceResponse;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.PaymentRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.service.InvoiceService;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {
    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public List<InvoiceResponse> findAll() { return invoiceService.findAll(); }

    @GetMapping("/{id}")
    public InvoiceResponse findById(@PathVariable Long id) { return invoiceService.findById(id); }

    @PostMapping("/{id}/payments")
    public ResponseEntity<InvoiceResponse> recordPayment(@PathVariable Long id,
                                                          @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.recordPayment(id, request));
    }
}