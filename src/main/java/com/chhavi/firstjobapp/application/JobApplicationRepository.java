package com.chhavi.firstjobapp.application;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByJobId(Long jobId);
    List<JobApplication> findByAppliedByUsername(String username);
    boolean existsByJobIdAndAppliedByUsername(Long jobId, String username);
}