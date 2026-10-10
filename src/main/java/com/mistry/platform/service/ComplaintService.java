package com.mistry.platform.service;

import com.mistry.platform.dto.ComplaintRequest;
import com.mistry.platform.dto.ComplaintResponse;
import com.mistry.platform.entity.Complaint;
import com.mistry.platform.entity.ComplaintStatus;
import com.mistry.platform.entity.Customer;
import com.mistry.platform.exception.ComplaintNotFoundException;
import com.mistry.platform.repository.ComplaintRepository;
import com.mistry.platform.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final CustomerRepository customerRepository;

    private Customer findCustomer(String identifier) {
        return customerRepository.findByEmail(identifier)
                .or(() -> customerRepository.findByPhone(identifier))
                .orElseThrow(() -> new BadCredentialsException("Customer account not found"));
    }

    private ComplaintResponse toResponse(Complaint c) {
        return new ComplaintResponse(
                c.getId(),
                c.getCustomerId(),
                c.getBookingId(),
                c.getCategory(),
                c.getDescription(),
                c.getStatus(),
                c.getCreatedAt(),
                c.getUpdatedAt());
    }

    public ComplaintResponse submit(String loggedInIdentifier, ComplaintRequest request) {
        Customer customer = findCustomer(loggedInIdentifier);

        Complaint complaint = new Complaint();
        complaint.setCustomerId(customer.getId());
        complaint.setBookingId(request.getBookingId().trim());
        complaint.setCategory(request.getCategory());
        complaint.setDescription(request.getDescription().trim());
        complaint.setStatus(ComplaintStatus.SUBMITTED);
        complaint.setCreatedAt(LocalDateTime.now());
        complaint.setUpdatedAt(LocalDateTime.now());

        return toResponse(complaintRepository.save(complaint));
    }

    public List<ComplaintResponse> getMyComplaints(String loggedInIdentifier) {
        Customer customer = findCustomer(loggedInIdentifier);
        return complaintRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ComplaintResponse getMyComplaint(String loggedInIdentifier, Long complaintId) {
        Customer customer = findCustomer(loggedInIdentifier);
        Complaint complaint = complaintRepository.findByIdAndCustomerId(complaintId, customer.getId())
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found"));
        return toResponse(complaint);
    }

    public List<ComplaintResponse> getComplaintsForAdmin(ComplaintStatus status) {
        List<Complaint> complaints = (status == null)
                ? complaintRepository.findAll(Sort.by(Sort.Direction.ASC, "createdAt"))
                : complaintRepository.findByStatusOrderByCreatedAtAsc(status);
        return complaints.stream().map(this::toResponse).toList();
    }
}
