package com.chhavi.firstjobapp.job;

import com.chhavi.firstjobapp.auth.AdminUtils;
import com.chhavi.firstjobapp.company.Company;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/jobs")
@CrossOrigin(origins = "*")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) { this.jobService = jobService; }

    @GetMapping
    public ResponseEntity<List<Job>> findAll(Authentication authentication) {
        List<Job> jobs = jobService.findAll();
        Long adminCid = AdminUtils.getAdminCompanyId(authentication);
        if (adminCid != null) {
            // Admin only sees their own company's jobs
            jobs = jobs.stream()
                    .filter(j -> j.getCompany() != null
                            && j.getCompany().getId().equals(adminCid))
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable Long id) {
        Job job = jobService.getJobById(id);
        if (job != null) return ResponseEntity.ok(job);
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<String> createJob(@RequestBody Job job,
                                            Authentication authentication) {
        Long adminCid = AdminUtils.getAdminCompanyId(authentication);
        if (adminCid != null) {
            Company c = new Company();
            c.setId(adminCid);
            job.setCompany(c);
        }
        jobService.createJob(job);
        return new ResponseEntity<>("Job added successfully", HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateJob(@PathVariable Long id,
                                            @RequestBody Job updatedJob,
                                            Authentication authentication) {
        // Verify admin owns this job
        Long adminCid = AdminUtils.getAdminCompanyId(authentication);
        if (adminCid != null) {
            Job existing = jobService.getJobById(id);
            if (existing == null || existing.getCompany() == null
                    || !existing.getCompany().getId().equals(adminCid)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only edit your own company's jobs.");
            }
            // Force company to stay the same
            Company c = new Company();
            c.setId(adminCid);
            updatedJob.setCompany(c);
        }
        boolean updated = jobService.updateJob(id, updatedJob);
        if (updated) return ResponseEntity.ok("Updated Successfully");
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteJob(@PathVariable Long id,
                                            Authentication authentication) {
        Long adminCid = AdminUtils.getAdminCompanyId(authentication);
        if (adminCid != null) {
            Job existing = jobService.getJobById(id);
            if (existing == null || existing.getCompany() == null
                    || !existing.getCompany().getId().equals(adminCid)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only delete your own company's jobs.");
            }
        }
        boolean deleted = jobService.deleteJobById(id);
        if (deleted) return ResponseEntity.ok("Deleted Successfully");
        return ResponseEntity.notFound().build();
    }
}