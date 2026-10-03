package rw.ac.auca.garagerepairshopmanagementsystem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.RepairJobAssignmentResponse;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.RepairJobPartRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.RepairJobPartResponse;
import rw.ac.auca.garagerepairshopmanagementsystem.exception.BusinessException;
import rw.ac.auca.garagerepairshopmanagementsystem.exception.ResourceNotFoundException;
import rw.ac.auca.garagerepairshopmanagementsystem.model.*;
import rw.ac.auca.garagerepairshopmanagementsystem.repository.*;
import rw.ac.auca.garagerepairshopmanagementsystem.security.GarageContext;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RepairJobWorkflowService {
    private final RepairJobRepository repairJobRepository;
    private final MechanicRepository mechanicRepository;
    private final RepairJobAssignmentRepository assignmentRepository;
    private final SparePartRepository sparePartRepository;
    private final RepairJobPartRepository repairJobPartRepository;
    private final InvoiceRepository invoiceRepository;
    private final GarageContext garageContext;

    public RepairJobWorkflowService(RepairJobRepository repairJobRepository,
                                    MechanicRepository mechanicRepository,
                                    RepairJobAssignmentRepository assignmentRepository,
                                    SparePartRepository sparePartRepository,
                                    RepairJobPartRepository repairJobPartRepository,
                                    InvoiceRepository invoiceRepository,
                                    GarageContext garageContext) {
        this.repairJobRepository = repairJobRepository;
        this.mechanicRepository = mechanicRepository;
        this.assignmentRepository = assignmentRepository;
        this.sparePartRepository = sparePartRepository;
        this.repairJobPartRepository = repairJobPartRepository;
        this.invoiceRepository = invoiceRepository;
        this.garageContext = garageContext;
    }

    @Transactional
    public RepairJobAssignmentResponse assignMechanic(Long jobId, Long mechanicId) {
        RepairJob job = getJob(jobId);
        Mechanic mechanic = mechanicRepository.findByIdAndGarageId(mechanicId, garageContext.requireGarageId()).orElseThrow(() ->
                new ResourceNotFoundException("Mechanic not found with ID: " + mechanicId));
        if (!mechanic.isActive()) {
            throw new BusinessException("Inactive mechanics cannot be assigned to repair jobs");
        }
        if (assignmentRepository.findByRepairJobIdAndMechanicId(jobId, mechanicId).isPresent()) {
            throw new BusinessException("This mechanic is already assigned to the repair job");
        }
        RepairJobAssignment assignment = new RepairJobAssignment();
        assignment.setRepairJob(job);
        assignment.setMechanic(mechanic);
        assignment = assignmentRepository.save(assignment);
        return toResponse(assignment);
    }

    @Transactional(readOnly = true)
    public List<RepairJobAssignmentResponse> findMechanics(Long jobId) {
        getJob(jobId);
        return assignmentRepository.findByRepairJobId(jobId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void unassignMechanic(Long jobId, Long mechanicId) {
        getJob(jobId);
        RepairJobAssignment assignment = assignmentRepository.findByRepairJobIdAndMechanicId(jobId, mechanicId)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic assignment not found"));
        assignmentRepository.delete(assignment);
    }

    @Transactional
    public RepairJobPartResponse addPart(Long jobId, RepairJobPartRequest request) {
        RepairJob job = getJob(jobId);
        Long garageId = garageContext.requireGarageId();
        if (invoiceRepository.existsByRepairJobIdAndRepairJobVehicleGarageId(jobId, garageId)) {
            throw new BusinessException("Parts cannot be changed after the repair job has been invoiced");
        }
        SparePart part = sparePartRepository.findByIdForUpdateAndGarageId(request.getSparePartId(), garageId).orElseThrow(() ->
                new ResourceNotFoundException("Spare part not found with ID: " + request.getSparePartId()));
        if (part.getStockQuantity() < request.getQuantity()) {
            throw new BusinessException("Insufficient stock for spare part " + part.getSku());
        }
        RepairJobPart usage = repairJobPartRepository.findByRepairJobIdAndSparePartId(jobId, part.getId())
            .orElse(null);
        if (usage == null) {
            usage = new RepairJobPart();
            usage.setRepairJob(job);
            usage.setSparePart(part);
            usage.setQuantity(request.getQuantity());
            usage.setUnitPrice(part.getUnitPrice());
        } else {
            usage.setQuantity(usage.getQuantity() + request.getQuantity());
        }
        part.setStockQuantity(part.getStockQuantity() - request.getQuantity());
        sparePartRepository.save(part);
        return toResponse(repairJobPartRepository.save(usage));
    }

    @Transactional(readOnly = true)
    public List<RepairJobPartResponse> findParts(Long jobId) {
        getJob(jobId);
        return repairJobPartRepository.findByRepairJobId(jobId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void removePart(Long jobId, Long usageId) {
        getJob(jobId);
        if (invoiceRepository.existsByRepairJobIdAndRepairJobVehicleGarageId(jobId, garageContext.requireGarageId())) {
            throw new BusinessException("Parts cannot be changed after the repair job has been invoiced");
        }
        RepairJobPart usage = repairJobPartRepository.findById(usageId).orElseThrow(() ->
                new ResourceNotFoundException("Repair job part not found with ID: " + usageId));
        if (!usage.getRepairJob().getId().equals(jobId)) {
            throw new ResourceNotFoundException("Part usage not found for repair job: " + jobId);
        }
        SparePart part = usage.getSparePart();
        part.setStockQuantity(part.getStockQuantity() + usage.getQuantity());
        sparePartRepository.save(part);
        repairJobPartRepository.delete(usage);
    }

    private RepairJob getJob(Long id) {
        return repairJobRepository.findByIdAndVehicleGarageId(id, garageContext.requireGarageId()).orElseThrow(() ->
                new ResourceNotFoundException("Repair job not found with ID: " + id));
    }

    private RepairJobAssignmentResponse toResponse(RepairJobAssignment assignment) {
        Mechanic mechanic = assignment.getMechanic();
        return new RepairJobAssignmentResponse(mechanic.getId(), mechanic.getFullName(),
                mechanic.getSpecialization(), assignment.getAssignedAt());
    }

    private RepairJobPartResponse toResponse(RepairJobPart usage) {
        SparePart part = usage.getSparePart();
        BigDecimal lineTotal = usage.getUnitPrice().multiply(BigDecimal.valueOf(usage.getQuantity()));
        return new RepairJobPartResponse(usage.getId(), part.getId(), part.getSku(), part.getName(),
                usage.getQuantity(), usage.getUnitPrice(), lineTotal);
    }
}