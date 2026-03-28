package com.chhavi.firstjobapp.application;

import com.chhavi.firstjobapp.job.Job;
import com.chhavi.firstjobapp.job.JobRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;

@RestController
@RequestMapping("/applications")
@CrossOrigin(origins = "*")
public class JobApplicationController {

    private final JobApplicationRepository applicationRepository;
    private final JobRepository jobRepository;

    private static final String UPLOAD_DIR = "uploads/resumes/";

    public JobApplicationController(JobApplicationRepository applicationRepository,
                                    JobRepository jobRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
    }

    // ── USER: Apply for a job ──
    @PostMapping(value = "/apply/{jobId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> applyForJob(
            @PathVariable Long jobId,
            @RequestParam("applicantName")  String applicantName,
            @RequestParam("applicantEmail") String applicantEmail,
            @RequestParam("applicantPhone") String applicantPhone,
            @RequestParam(value = "coverLetter", required = false) String coverLetter,
            @RequestParam("resume") MultipartFile resume,
            Authentication authentication) {

        String username = authentication.getName();

        // Check already applied
        if (applicationRepository.existsByJobIdAndAppliedByUsername(jobId, username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("You have already applied for this job.");
        }

        Job job = jobRepository.findById(jobId).orElse(null);
        if (job == null) return ResponseEntity.notFound().build();

        // Save resume file
        String resumeFileName = null;
        String resumeFilePath = null;
        if (!resume.isEmpty()) {
            try {
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
                String uniqueName = System.currentTimeMillis() + "_" + resume.getOriginalFilename();
                Path filePath = uploadPath.resolve(uniqueName);
                Files.copy(resume.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                resumeFileName = resume.getOriginalFilename();
                resumeFilePath = filePath.toString();
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to upload resume.");
            }
        }

        JobApplication application = new JobApplication();
        application.setJob(job);
        application.setApplicantName(applicantName);
        application.setApplicantEmail(applicantEmail);
        application.setApplicantPhone(applicantPhone);
        application.setCoverLetter(coverLetter);
        application.setResumeFileName(resumeFileName);
        application.setResumeFilePath(resumeFilePath);
        application.setAppliedByUsername(username);
        applicationRepository.save(application);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Application submitted successfully!");
    }

    // ── ADMIN: Get all applications for a job ──
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<JobApplication>> getApplicationsForJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(applicationRepository.findByJobId(jobId));
    }

    // ── ADMIN: Get all applications ──
    @GetMapping
    public ResponseEntity<List<JobApplication>> getAllApplications() {
        return ResponseEntity.ok(applicationRepository.findAll());
    }

    // ── USER: Get my applications ──
    @GetMapping("/my")
    public ResponseEntity<List<JobApplication>> getMyApplications(Authentication authentication) {
        return ResponseEntity.ok(
                applicationRepository.findByAppliedByUsername(authentication.getName()));
    }

    // ── Check if already applied ──
    @GetMapping("/check/{jobId}")
    public ResponseEntity<Boolean> checkApplied(@PathVariable Long jobId,
                                                Authentication authentication) {
        return ResponseEntity.ok(
                applicationRepository.existsByJobIdAndAppliedByUsername(
                        jobId, authentication.getName()));
    }

    // ── ADMIN: Download resume ──
    @GetMapping("/resume/{applicationId}")
    public ResponseEntity<Resource> downloadResume(@PathVariable Long applicationId) {
        JobApplication app = applicationRepository.findById(applicationId).orElse(null);
        if (app == null || app.getResumeFilePath() == null)
            return ResponseEntity.notFound().build();
        try {
            Path filePath = Paths.get(app.getResumeFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) return ResponseEntity.notFound().build();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + app.getResumeFileName() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ── ADMIN: Delete application ──
    @DeleteMapping("/{applicationId}")
    public ResponseEntity<String> deleteApplication(@PathVariable Long applicationId) {
        applicationRepository.deleteById(applicationId);
        return ResponseEntity.ok("Application deleted.");
    }
}