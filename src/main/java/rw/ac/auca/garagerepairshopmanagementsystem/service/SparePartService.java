package rw.ac.auca.garagerepairshopmanagementsystem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.SparePartRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.SparePartResponse;
import rw.ac.auca.garagerepairshopmanagementsystem.exception.BusinessException;
import rw.ac.auca.garagerepairshopmanagementsystem.exception.ResourceNotFoundException;
import rw.ac.auca.garagerepairshopmanagementsystem.model.SparePart;
import rw.ac.auca.garagerepairshopmanagementsystem.repository.RepairJobPartRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.repository.SparePartRepository;
import rw.ac.auca.garagerepairshopmanagementsystem.security.GarageContext;

import java.util.List;

@Service
public class SparePartService {
    private final SparePartRepository sparePartRepository;
    private final RepairJobPartRepository repairJobPartRepository;
    private final GarageContext garageContext;

    public SparePartService(SparePartRepository sparePartRepository,
                            RepairJobPartRepository repairJobPartRepository,
                            GarageContext garageContext) {
        this.sparePartRepository = sparePartRepository;
        this.repairJobPartRepository = repairJobPartRepository;
        this.garageContext = garageContext;
    }

    @Transactional
    public SparePartResponse create(SparePartRequest request) {
        Long garageId = garageContext.requireGarageId();
        if (sparePartRepository.existsBySkuAndGarageId(request.getSku(), garageId)) {
            throw new BusinessException("A spare part with this SKU already exists");
        }
        SparePart part = new SparePart();
        apply(part, request);
        part.setGarage(garageContext.requireGarage());
        return toResponse(sparePartRepository.save(part));
    }

    @Transactional(readOnly = true)
    public List<SparePartResponse> findAll() {
        return sparePartRepository.findAllByGarageId(garageContext.requireGarageId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SparePartResponse findById(Long id) {
        return toResponse(getPart(id));
    }

    @Transactional
    public SparePartResponse update(Long id, SparePartRequest request) {
        SparePart part = getPart(id);
        if (sparePartRepository.existsBySkuAndIdNotAndGarageId(request.getSku(), id, garageContext.requireGarageId())) {
            throw new BusinessException("A spare part with this SKU already exists");
        }
        apply(part, request);
        return toResponse(sparePartRepository.save(part));
    }

    @Transactional
    public void delete(Long id) {
        SparePart part = getPart(id);
        if (repairJobPartRepository.existsBySparePartId(id)) {
            throw new BusinessException("A spare part used on a repair job cannot be deleted");
        }
        sparePartRepository.delete(part);
    }

    private SparePart getPart(Long id) {
        return sparePartRepository.findByIdAndGarageId(id, garageContext.requireGarageId()).orElseThrow(() ->
                new ResourceNotFoundException("Spare part not found with ID: " + id));
    }

    private void apply(SparePart part, SparePartRequest request) {
        part.setSku(request.getSku());
        part.setName(request.getName());
        part.setDescription(request.getDescription());
        part.setUnitPrice(request.getUnitPrice());
        part.setStockQuantity(request.getStockQuantity());
        part.setReorderLevel(request.getReorderLevel() == null ? 0 : request.getReorderLevel());
    }

    private SparePartResponse toResponse(SparePart part) {
        return new SparePartResponse(part.getId(), part.getUuid(), part.getSku(), part.getName(),
                part.getDescription(), part.getUnitPrice(), part.getStockQuantity(), part.getReorderLevel(),
                part.getStockQuantity() <= part.getReorderLevel());
    }
}