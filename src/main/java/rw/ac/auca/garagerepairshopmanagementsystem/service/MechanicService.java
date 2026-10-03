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
import rw.ac.auca.garagerepairshopmanagementsystem.security.GarageContext;

import java.util.List;

@Service
public class MechanicService {
    private final MechanicRepository mechanicRepository;
    private final RepairJobAssignmentRepository assignmentRepository;
    private final GarageContext garageContext;

    public MechanicService(MechanicRepository mechanicRepository,
                           RepairJobAssignmentRepository assignmentRepository,
                           GarageContext garageContext) {
        this.mechanicRepository = mechanicRepository;
        this.assignmentRepository = assignmentRepository;
        this.garageContext = garageContext;
    }

    @Transactional
    public MechanicResponse create(MechanicRequest request) {
        Long garageId = garageContext.requireGarageId();
        if (mechanicRepository.existsByEmailAndGarageId(request.getEmail(), garageId)
            || mechanicRepository.existsByPhoneAndGarageId(request.getPhone(), garageId)) {
            throw new BusinessException("A mechanic with this email or phone already exists");
        }
        Mechanic mechanic = new Mechanic();
        apply(mechanic, request, true);
        mechanic.setGarage(garageContext.requireGarage());
        return toResponse(mechanicRepository.save(mechanic));
    }

    @Transactional(readOnly = true)
    public List<MechanicResponse> findAll() {
        return mechanicRepository.findAllByGarageId(garageContext.requireGarageId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MechanicResponse findById(Long id) {
        return toResponse(getMechanic(id));
    }

    @Transactional
    public MechanicResponse update(Long id, MechanicRequest request) {
        Long garageId = garageContext.requireGarageId();
        Mechanic mechanic = getMechanic(id);
        if (mechanicRepository.existsByEmailAndIdNotAndGarageId(request.getEmail(), id, garageId)
            || mechanicRepository.existsByPhoneAndIdNotAndGarageId(request.getPhone(), id, garageId)) {
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
        return mechanicRepository.findByIdAndGarageId(id, garageContext.requireGarageId()).orElseThrow(() ->
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