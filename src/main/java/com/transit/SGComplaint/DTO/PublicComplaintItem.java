package com.transit.SGComplaint.DTO;

public record PublicComplaintItem(
        Long complaintNo,
        String categoryCode,
        String categoryLabel,
        String statusCode,
        String statusLabel,
        String title,
        String maskedWriterName,
        String registeredDate) {
}
