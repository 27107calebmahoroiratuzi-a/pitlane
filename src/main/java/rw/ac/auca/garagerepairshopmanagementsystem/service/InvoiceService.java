package rw.ac.auca.garagerepairshopmanagementsystem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.InvoiceResponse;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.PaymentRequest;
import rw.ac.auca.garagerepairshopmanagementsystem.dto.PaymentResponse;
import rw.ac.auca.garagerepairshopmanagementsystem.exception.BusinessException;
import rw.ac.auca.garagerepairshopmanagementsystem.exception.ResourceNotFoundException;
import rw.ac.auca.garagerepairshopmanagementsystem.model.*;
import rw.ac.auca.garagerepairshopmanagementsystem.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final RepairJobRepository repairJobRepository;
    private final RepairJobPartRepository repairJobPartRepository;
    private final PaymentRepository paymentRepository;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          RepairJobRepository repairJobRepository,
                          RepairJobPartRepository repairJobPartRepository,
                          PaymentRepository paymentRepository) {
        this.invoiceRepository = invoiceRepository;
        this.repairJobRepository = repairJobRepository;
        this.repairJobPartRepository = repairJobPartRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public InvoiceResponse issueForRepairJob(Long repairJobId) {
        RepairJob job = repairJobRepository.findById(repairJobId).orElseThrow(() ->
                new ResourceNotFoundException("Repair job not found with ID: " + repairJobId));
        if (job.getStatus() != RepairJobStatus.COMPLETED) {
            throw new BusinessException("Only completed repair jobs can be invoiced");
        }
        if (invoiceRepository.existsByRepairJobId(repairJobId)) {
            throw new BusinessException("An invoice already exists for this repair job");
        }
        BigDecimal partsAmount = repairJobPartRepository.findByRepairJobId(repairJobId).stream()
                .map(usage -> usage.getUnitPrice().multiply(BigDecimal.valueOf(usage.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal laborAmount = job.getCost();
        Invoice invoice = new Invoice();
        invoice.setRepairJob(job);
        invoice.setLaborAmount(laborAmount);
        invoice.setPartsAmount(partsAmount);
        invoice.setTotalAmount(laborAmount.add(partsAmount));
        invoice.setDueAt(LocalDateTime.now().plusDays(30));
        return toResponse(invoiceRepository.save(invoice));
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> findAll() {
        return invoiceRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public InvoiceResponse findById(Long id) {
        return toResponse(getInvoice(id));
    }

    @Transactional
    public InvoiceResponse recordPayment(Long invoiceId, PaymentRequest request) {
        Invoice invoice = invoiceRepository.findByIdForUpdate(invoiceId).orElseThrow(() ->
            new ResourceNotFoundException("Invoice not found with ID: " + invoiceId));
        BigDecimal alreadyPaid = paymentRepository.sumAmountByInvoiceId(invoiceId);
        if (request.getAmount().compareTo(invoice.getTotalAmount().subtract(alreadyPaid)) > 0) {
            throw new BusinessException("Payment exceeds the outstanding invoice balance");
        }
        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setReference(request.getReference());
        paymentRepository.save(payment);
        BigDecimal totalPaid = alreadyPaid.add(request.getAmount());
        invoice.setStatus(totalPaid.compareTo(invoice.getTotalAmount()) == 0
                ? InvoiceStatus.PAID : InvoiceStatus.PARTIALLY_PAID);
        invoiceRepository.save(invoice);
        return toResponse(invoice);
    }

    private Invoice getInvoice(Long id) {
        return invoiceRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Invoice not found with ID: " + id));
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        List<Payment> payments = paymentRepository.findByInvoiceIdOrderByPaidAtAsc(invoice.getId());
        BigDecimal amountPaid = payments.stream().map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        RepairJob job = invoice.getRepairJob();
        Vehicle vehicle = job.getVehicle();
        Customer customer = vehicle.getCustomer();
        List<PaymentResponse> paymentResponses = payments.stream().map(payment ->
                new PaymentResponse(payment.getId(), payment.getUuid(), payment.getAmount(),
                        payment.getPaymentMethod(), payment.getReference(), payment.getPaidAt())).toList();
        return new InvoiceResponse(invoice.getId(), invoice.getUuid(), invoice.getInvoiceNumber(),
                job.getId(), vehicle.getPlateNumber(), customer == null ? null : customer.getFullName(),
                customer == null ? null : customer.getEmail(), invoice.getLaborAmount(), invoice.getPartsAmount(),
                invoice.getTotalAmount(), amountPaid, invoice.getTotalAmount().subtract(amountPaid),
                invoice.getStatus(), invoice.getIssuedAt(), invoice.getDueAt(), paymentResponses);
    }
}