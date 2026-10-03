package rw.ac.auca.garagerepairshopmanagementsystem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.MechanicRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.MechanicResponse;
import rw.ac.auca.garagerepairshopmanagementsystem.exception.BusinessException;
import rw.ac.auca.garagerepairshopmanagementsystem.exception.ResourceNotFoundException;
import rw.ac.auca.garagerepairshopmanagementsystem.model.Mechanic;
import rw.ac.auca.garagerepairshopmanagementsystem.repository.MechanicRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.repository.RepairJobAssignmentRepository;

import java.util.List;

@Service
public class MechanicService {
    private final MechanicRepository mechanicRepository;
    private final RepairJobAssignmentRepository assignmentRepository;

    public MechanicService(MechanicRepository mechanicRepository,
                           RepairJobAssignmentRepository assignmentRepository) {
        this.mechanicRepository = mechanicRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional
    public MechanicResponse create(MechanicRequest request) {
        if (mechanicRepository.existsByEmail(request.getEmail())
                || mechanicRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("A mechanic with this email or phone already exists");
        }
        Mechanic mechanic = new Mechanic();
        apply(mechanic, request, true);
        return toResponse(mechanicRepository.save(mechanic));
    }

    @Transactional(readOnly = true)
    public List<MechanicResponse> findAll() {
        return mechanicRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MechanicResponse findById(Long id) {
        return toResponse(getMechanic(id));
    }

    @Transactional
    public MechanicResponse update(Long id, MechanicRequest request) {
        Mechanic mechanic = getMechanic(id);
        if (mechanicRepository.existsByEmailAndIdNot(request.getEmail(), id)
                || mechanicRepository.existsByPhoneAndIdNot(request.getPhone(), id)) {
            throw new BusinessException("A mechanic with this email or phone already exists");
        }
        apply(mechanic, request, false);
        return toResponse(mechanicRepository.save(mechanic));
    }

    @Transactional
    public void delete(Long id) {
        Mechanic mechanic = getMechanic(id);
        if (assignmentRepository.existsByMechanicId(id)) {
            throw new BusinessException("Remove this mechanic from repair jobs before deleting them");
        }
        mechanicRepository.delete(mechanic);
    }

    private Mechanic getMechanic(Long id) {
        return mechanicRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Mechanic not found with ID: " + id));
    }

    private void apply(Mechanic mechanic, MechanicRequest request, boolean creating) {
        mechanic.setFullName(request.getFullName());
        mechanic.setPhone(request.getPhone());
        mechanic.setEmail(request.getEmail());
        mechanic.setSpecialization(request.getSpecialization());
        if (request.getActive() != null) {
            mechanic.setActive(request.getActive());
        } else if (creating) {
            mechanic.setActive(true);
        }
    }

    private MechanicResponse toResponse(Mechanic mechanic) {
        return new MechanicResponse(mechanic.getId(), mechanic.getUuid(), mechanic.getFullName(),
                mechanic.getPhone(), mechanic.getEmail(), mechanic.getSpecialization(), mechanic.isActive());
    }
}