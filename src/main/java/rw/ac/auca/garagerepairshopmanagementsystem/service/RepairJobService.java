package rw.ac.auca.garagerepairshopmanagementsystem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.RepairJobRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.RepairJobResponse;
import rw.ac.auca.garagerepairshopmanagementsystem.exception.BusinessException;
import rw.ac.auca.garagerepairshopmanagementsystem.exception.ResourceNotFoundException;
import rw.ac.auca.garagerepairshopmanagementsystem.model.*;
import rw.ac.auca.garagerepairshopmanagementsystem.repository.RepairJobRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.repository.InvoiceRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.repository.VehicleRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RepairJobService {

    private final RepairJobRepository repairJobRepository;
    private final VehicleRepository vehicleRepository;
        private final InvoiceRepository invoiceRepository;

    public RepairJobService(
            RepairJobRepository repairJobRepository,
                        VehicleRepository vehicleRepository,
                        InvoiceRepository invoiceRepository
    ) {
        this.repairJobRepository = repairJobRepository;
        this.vehicleRepository = vehicleRepository;
                this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public RepairJobResponse create(RepairJobRequest request) {

        Vehicle vehicle = vehicleRepository.findById(
                request.getVehicleId()
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Vehicle not found with ID: "
                                + request.getVehicleId()
                ));

        validateBusinessRules(request);

        RepairJob job = new RepairJob();

        job.setComplaint(request.getComplaint());
        job.setDiagnosis(request.getDiagnosis());
        job.setRepairDescription(request.getRepairDescription());
        job.setExpectedCompletionDate(
                request.getExpectedCompletionDate()
        );
        job.setActualCompletionDate(
                request.getActualCompletionDate()
        );
        job.setCost(request.getCost());
        job.setStatus(
                request.getStatus() == null
                        ? RepairJobStatus.PENDING
                        : request.getStatus()
        );
        job.setVehicle(vehicle);

        updateVehicleStatus(vehicle, job.getStatus());

        return toResponse(repairJobRepository.save(job));
    }

    public List<RepairJobResponse> findAll() {

        return repairJobRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public RepairJobResponse findById(Long id) {

        RepairJob job = repairJobRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Repair job not found with ID: " + id
                        ));

        return toResponse(job);
    }

    @Transactional
    public RepairJobResponse update(
            Long id,
            RepairJobRequest request
    ) {

                if (invoiceRepository.existsByRepairJobId(id)) {
                        throw new BusinessException("An invoiced repair job cannot be changed");
                }

        RepairJob job = repairJobRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Repair job not found with ID: " + id
                        ));

        Vehicle vehicle = vehicleRepository.findById(
                request.getVehicleId()
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Vehicle not found with ID: "
                                + request.getVehicleId()
                ));

        validateBusinessRules(request);

        RepairJobStatus status =
                request.getStatus() == null
                        ? job.getStatus()
                        : request.getStatus();

        job.setComplaint(request.getComplaint());
        job.setDiagnosis(request.getDiagnosis());
        job.setRepairDescription(request.getRepairDescription());
        job.setExpectedCompletionDate(
                request.getExpectedCompletionDate()
        );
        job.setActualCompletionDate(
                request.getActualCompletionDate()
        );
        job.setCost(request.getCost());
        job.setStatus(status);
        job.setVehicle(vehicle);

        if (status == RepairJobStatus.COMPLETED
                && job.getActualCompletionDate() == null) {

            job.setActualCompletionDate(LocalDateTime.now());
        }

        updateVehicleStatus(vehicle, status);

        return toResponse(repairJobRepository.save(job));
    }

    @Transactional
    public void delete(Long id) {

                if (invoiceRepository.existsByRepairJobId(id)) {
                        throw new BusinessException("An invoiced repair job cannot be deleted");
                }

        RepairJob job = repairJobRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Repair job not found with ID: " + id
                        ));

        Vehicle vehicle = job.getVehicle();

        repairJobRepository.delete(job);

        if (vehicle != null) {
            vehicle.setStatus(VehicleStatus.AVAILABLE);
            vehicleRepository.save(vehicle);
        }
    }

    private void validateBusinessRules(
            RepairJobRequest request
    ) {

        if (request.getCost() != null
                && request.getCost().signum() < 0) {

            throw new BusinessException(
                    "Repair cost cannot be negative"
            );
        }

        if (request.getStatus() == RepairJobStatus.COMPLETED
                && request.getActualCompletionDate() == null) {

            // We allow the service to automatically assign the
            // completion date during update/create.
        }

        if (request.getStatus() == RepairJobStatus.CANCELLED
                && request.getActualCompletionDate() != null) {

            throw new BusinessException(
                    "Cancelled repair cannot have an actual completion date"
            );
        }
    }

    private void updateVehicleStatus(
            Vehicle vehicle,
            RepairJobStatus status
    ) {

        switch (status) {

            case COMPLETED:
                vehicle.setStatus(
                        VehicleStatus.READY_FOR_PICKUP
                );
                break;

            case WAITING_FOR_PARTS:
                vehicle.setStatus(
                        VehicleStatus.WAITING_FOR_PARTS
                );
                break;

            case CANCELLED:
                vehicle.setStatus(
                        VehicleStatus.AVAILABLE
                );
                break;

            default:
                vehicle.setStatus(
                        VehicleStatus.IN_REPAIR
                );
        }

        vehicleRepository.save(vehicle);
    }

    private RepairJobResponse toResponse(
            RepairJob job
    ) {

        return new RepairJobResponse(
                job.getId(),
                job.getUuid(),
                job.getComplaint(),
                job.getDiagnosis(),
                job.getRepairDescription(),
                job.getDateReceived(),
                job.getExpectedCompletionDate(),
                job.getActualCompletionDate(),
                job.getCost(),
                job.getStatus(),
                job.getVehicle().getId(),
                job.getVehicle().getPlateNumber()
        );
    }
}