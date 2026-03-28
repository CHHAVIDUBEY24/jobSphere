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

    // ✅ Use absolute path based on user home or project root
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/resumes/";

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

        if (applicationRepository.existsByJobIdAndAppliedByUsername(jobId, username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("You have already applied for this job.");
        }

        Job job = jobRepository.findById(jobId).orElse(null);
        if (job == null) return ResponseEntity.notFound().build();

        String resumeFileName = null;
        String resumeFilePath = null;

        if (!resume.isEmpty()) {
            try {
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                // ✅ Sanitize filename to avoid path traversal
                String original = resume.getOriginalFilename();
                String sanitized = original != null ? original.replaceAll("[^a-zA-Z0-9._-]", "_") : "resume";
                String uniqueName = System.currentTimeMillis() + "_" + sanitized;
                Path filePath = uploadPath.resolve(uniqueName).normalize();

                // ✅ Security check — ensure file stays within upload dir
                if (!filePath.startsWith(uploadPath)) {
                    return ResponseEntity.badRequest().body("Invalid file path.");
                }

                Files.copy(resume.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                resumeFileName = original;
                resumeFilePath = filePath.toAbsolutePath().toString();

            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to upload resume: " + e.getMessage());
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

        if (app == null) {
            return ResponseEntity.notFound().build();
        }
        if (app.getResumeFilePath() == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path filePath = Paths.get(app.getResumeFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // ✅ Detect content type
            String contentType = "application/octet-stream";
            String filename = app.getResumeFileName() != null
                    ? app.getResumeFileName() : "resume";
            if (filename.endsWith(".pdf")) {
                contentType = "application/pdf";
            } else if (filename.endsWith(".doc")) {
                contentType = "application/msword";
            } else if (filename.endsWith(".docx")) {
                contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ── ADMIN: Delete application ──
    @DeleteMapping("/{applicationId}")
    public ResponseEntity<String> deleteApplication(@PathVariable Long applicationId) {
        // Also delete the file from disk
        JobApplication app = applicationRepository.findById(applicationId).orElse(null);
        if (app != null && app.getResumeFilePath() != null) {
            try {
                Files.deleteIfExists(Paths.get(app.getResumeFilePath()));
            } catch (IOException ignored) {}
        }
        applicationRepository.deleteById(applicationId);
        return ResponseEntity.ok("Application deleted.");
    }
}