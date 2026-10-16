package com.mistry.platform.service;

import com.mistry.platform.dto.*;
import com.mistry.platform.entity.*;
import com.mistry.platform.exception.ComplaintNotFoundException;
import com.mistry.platform.exception.InvalidComplaintActionException;
import com.mistry.platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final CustomerRepository customerRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final RefundRepository refundRepository;

    private Customer findCustomer(String identifier) {
        return customerRepository.findByEmail(identifier)
                .or(() -> customerRepository.findByPhone(identifier))
                .orElseThrow(() -> new BadCredentialsException("Customer account not found"));
    }

    private Complaint findComplaint(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found"));
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
                c.getUpdatedAt(),
                c.getResolutionDecision(),
                c.getResolutionNote(),
                c.getRefundAmount(),
                c.getResolvedAt());
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

    // ---------- Admin: dispute detail, review, resolution ----------

    private Long parseBookingId(String raw) {
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public AdminComplaintDetailResponse getComplaintDetailForAdmin(Long complaintId) {
        Complaint complaint = findComplaint(complaintId);

        BookingInfoResponse booking = null;
        ReviewResponse review = null;
        Long bookingId = parseBookingId(complaint.getBookingId());
        if (bookingId != null) {
            booking = null;
            review = reviewRepository.findByBookingId(bookingId)
                    .map(ReviewResponse::from).orElse(null);
        }

        return new AdminComplaintDetailResponse(
                toResponse(complaint), booking, review, complaint.getResolvedBy());
    }

    public ComplaintResponse startReview(Long complaintId) {
        Complaint complaint = findComplaint(complaintId);
        if (complaint.getStatus() != ComplaintStatus.SUBMITTED) {
            throw new InvalidComplaintActionException("Only submitted complaints can be moved to review");
        }
        complaint.setStatus(ComplaintStatus.UNDER_REVIEW);
        complaint.setUpdatedAt(LocalDateTime.now());
        return toResponse(complaintRepository.save(complaint));
    }

    @Transactional
    public ComplaintResponse resolve(String adminIdentifier, Long complaintId, ResolveComplaintRequest request) {
        Complaint complaint = findComplaint(complaintId);

        if (complaint.getStatus() == ComplaintStatus.RESOLVED
                || complaint.getStatus() == ComplaintStatus.CLOSED) {
            throw new InvalidComplaintActionException("Complaint is already resolved");
        }

        if (request.getDecision() == ResolutionDecision.REFUND_ISSUED) {
            BigDecimal amount = request.getRefundAmount();
            if (amount == null || amount.signum() <= 0) {
                throw new InvalidComplaintActionException(
                        "Refund amount is required for a refund decision");
            }
            if (refundRepository.existsByComplaintId(complaint.getId())) {
                throw new InvalidComplaintActionException("Refund already processed for this complaint");
            }

            Refund refund = new Refund();
            refund.setComplaintId(complaint.getId());
            refund.setBookingId(complaint.getBookingId());
            refund.setCustomerId(complaint.getCustomerId());
            refund.setAmount(amount);
            refund.setStatus("PROCESSED");
            refund.setProcessedBy(adminIdentifier);
            refund.setProcessedAt(LocalDateTime.now());
            refundRepository.save(refund);

            complaint.setRefundAmount(amount);
        } else if (request.getRefundAmount() != null) {
            throw new InvalidComplaintActionException(
                    "Refund amount is only allowed with REFUND_ISSUED decision");
        }

        complaint.setResolutionDecision(request.getDecision());
        complaint.setResolutionNote(request.getNote().trim());
        complaint.setResolvedBy(adminIdentifier);
        complaint.setResolvedAt(LocalDateTime.now());
        complaint.setStatus(ComplaintStatus.RESOLVED);
        complaint.setUpdatedAt(LocalDateTime.now());

        return toResponse(complaintRepository.save(complaint));
    }
}