package rw.ac.auca.garagerepairshopmanagementsystem.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.*;
import rw.ac.auca.garagerepairshopmanagementsystem.service.InvoiceService;
import rw.ac.auca.garagerepairshopmanagementsystem.service.RepairJobWorkflowService;

import java.util.List;

@RestController
@RequestMapping("/api/repair-jobs/{repairJobId}")
public class RepairJobWorkflowController {
    private final RepairJobWorkflowService workflowService;
    private final InvoiceService invoiceService;

    public RepairJobWorkflowController(RepairJobWorkflowService workflowService, InvoiceService invoiceService) {
        this.workflowService = workflowService;
        this.invoiceService = invoiceService;
    }

    @PostMapping("/mechanics/{mechanicId}")
    public ResponseEntity<RepairJobAssignmentResponse> assignMechanic(
            @PathVariable Long repairJobId, @PathVariable Long mechanicId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workflowService.assignMechanic(repairJobId, mechanicId));
    }

    @GetMapping("/mechanics")
    public List<RepairJobAssignmentResponse> findMechanics(@PathVariable Long repairJobId) {
        return workflowService.findMechanics(repairJobId);
    }

    @DeleteMapping("/mechanics/{mechanicId}")
    public ResponseEntity<Void> unassignMechanic(@PathVariable Long repairJobId, @PathVariable Long mechanicId) {
        workflowService.unassignMechanic(repairJobId, mechanicId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/parts")
    public ResponseEntity<RepairJobPartResponse> addPart(@PathVariable Long repairJobId,
                                                          @Valid @RequestBody RepairJobPartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workflowService.addPart(repairJobId, request));
    }

    @GetMapping("/parts")
    public List<RepairJobPartResponse> findParts(@PathVariable Long repairJobId) {
        return workflowService.findParts(repairJobId);
    }

    @DeleteMapping("/parts/{usageId}")
    public ResponseEntity<Void> removePart(@PathVariable Long repairJobId, @PathVariable Long usageId) {
        workflowService.removePart(repairJobId, usageId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/invoice")
    public ResponseEntity<InvoiceResponse> issueInvoice(@PathVariable Long repairJobId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.issueForRepairJob(repairJobId));
    }
}