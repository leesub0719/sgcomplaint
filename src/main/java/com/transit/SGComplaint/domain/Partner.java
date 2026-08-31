package com.transit.SGComplaint.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "sgtransit_partner")
public class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "partner_no")
    private Long partnerNo;

    @Column(name = "partner_name", nullable = false, length = 100)
    private String name;

    @Column(name = "partner_phone", nullable = false, length = 30)
    private String phone;

    @Column(name = "partner_site", nullable = false, length = 500)
    private String site;

    @Column(name = "partner_notes", nullable = false, columnDefinition = "TEXT")
    private String notes;

    @Column(name = "partner_status", nullable = false, length = 1)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Partner() {
    }

    public static Partner create(String name, String phone, String site, String notes) {
        Partner partner = new Partner();
        partner.changeInformation(name, phone, site, notes);
        partner.status = "Y";
        return partner;
    }

    public void changeInformation(String name, String phone, String site, String notes) {
        this.name = name.trim();
        this.phone = phone.trim();
        this.site = site;
        this.notes = notes;
    }

    public void deactivate() {
        this.status = "N";
    }

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    public Long getPartnerNo() { return partnerNo; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getSite() { return site; }
    public String getNotes() { return notes; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
