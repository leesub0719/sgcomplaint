package com.transit.SGComplaint.DTO;

import java.util.List;

public class ComplaintListItem {

    private final Long complaintNo;
    private final String title;
    private final String content;
    private final String statusCode;
    private final String statusLabel;
    private final String statusCssClass;
    private final String registeredDate;
    private final String registeredDateTime;
    private final List<ComplaintFileItem> answerAttachments;

    public ComplaintListItem(
            Long complaintNo,
            String title,
            String content,
            String statusCode,
            String statusLabel,
            String statusCssClass,
            String registeredDate,
            String registeredDateTime,
            List<ComplaintFileItem> answerAttachments) {
        this.complaintNo = complaintNo;
        this.title = title;
        this.content = content;
        this.statusCode = statusCode;
        this.statusLabel = statusLabel;
        this.statusCssClass = statusCssClass;
        this.registeredDate = registeredDate;
        this.registeredDateTime = registeredDateTime;
        this.answerAttachments = List.copyOf(answerAttachments);
    }

    public Long getComplaintNo() { return complaintNo; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getStatusCode() { return statusCode; }
    public String getStatusLabel() { return statusLabel; }
    public String getStatusCssClass() { return statusCssClass; }
    public String getRegisteredDate() { return registeredDate; }
    public String getRegisteredDateTime() { return registeredDateTime; }
    public List<ComplaintFileItem> getAnswerAttachments() { return answerAttachments; }
}
