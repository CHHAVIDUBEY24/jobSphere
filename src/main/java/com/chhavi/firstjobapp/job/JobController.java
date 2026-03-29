package com.chhavi.firstjobapp.job;

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

    private JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public ResponseEntity<List<Job>> findAll(Authentication authentication) {
        List<Job> jobs = jobService.findAll();
        // If admin, only return jobs belonging to their company
        if (authentication != null) {
            Object details = authentication.getDetails();
            if (details instanceof Long companyId) {
                jobs = jobs.stream()
                        .filter(j -> j.getCompany() != null && j.getCompany().getId().equals(companyId))
                        .collect(Collectors.toList());
            }
        }
        return ResponseEntity.ok(jobs);
    }

    @PostMapping
    public ResponseEntity<String> createJob(@RequestBody Job job, Authentication authentication) {
        // Force job to belong to admin's company
        Object details = authentication.getDetails();
        if (details instanceof Long companyId) {
            com.chhavi.firstjobapp.company.Company c = new com.chhavi.firstjobapp.company.Company();
            c.setId(companyId);
            job.setCompany(c);
        }
        jobService.createJob(job);
        return new ResponseEntity<>("Job added successfully", HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable Long id) {
        Job job = jobService.getJobById(id);
        if (job != null) return new ResponseEntity<>(job, HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteJob(@PathVariable Long id) {
        boolean deleted = jobService.deleteJobById(id);
        if (deleted) return new ResponseEntity<>("Deleted Successfully", HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateJob(@PathVariable Long id, @RequestBody Job updatedJob) {
        boolean updated = jobService.updateJob(id, updatedJob);
        if (updated) return new ResponseEntity<>("Updated Successfully", HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}