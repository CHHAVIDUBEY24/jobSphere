package com.chhavi.firstjobapp.application;

import com.chhavi.firstjobapp.job.Job;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_application")
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String applicantName;
    private String applicantEmail;
    private String applicantPhone;

    @Column(length = 2000)
    private String coverLetter;

    private String resumeFileName;
    private String resumeFilePath;
    private LocalDateTime appliedAt;

    @Column(nullable = false)
    private String appliedByUsername;

    @ManyToOne
    @JoinColumn(name = "job_id")
    @JsonIgnoreProperties({"applications", "company"})
    private Job job;

    @PrePersist
    public void prePersist() {
        this.appliedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String n) { this.applicantName = n; }
    public String getApplicantEmail() { return applicantEmail; }
    public void setApplicantEmail(String e) { this.applicantEmail = e; }
    public String getApplicantPhone() { return applicantPhone; }
    public void setApplicantPhone(String p) { this.applicantPhone = p; }
    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String c) { this.coverLetter = c; }
    public String getResumeFileName() { return resumeFileName; }
    public void setResumeFileName(String f) { this.resumeFileName = f; }
    public String getResumeFilePath() { return resumeFilePath; }
    public void setResumeFilePath(String p) { this.resumeFilePath = p; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime t) { this.appliedAt = t; }
    public String getAppliedByUsername() { return appliedByUsername; }
    public void setAppliedByUsername(String u) { this.appliedByUsername = u; }
    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }
}