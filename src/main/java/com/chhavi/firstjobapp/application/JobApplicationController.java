package com.chhavi.firstjobapp.application;

import com.chhavi.firstjobapp.auth.AdminUtils;
import com.chhavi.firstjobapp.job.Job;
import com.chhavi.firstjobapp.job.JobRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/applications")
@CrossOrigin(origins = "*")
public class JobApplicationController {

    private final JobApplicationRepository applicationRepository;
    private final JobRepository jobRepository;

    private static final String UPLOAD_DIR =
            System.getProperty("user.dir") + "/uploads/resumes/";

    public JobApplicationController(JobApplicationRepository applicationRepository,
                                    JobRepository jobRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
    }

    // ── USER: Apply ──
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
        if (applicationRepository.existsByJobIdAndAppliedByUsername(jobId, username))
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Already applied.");

        Job job = jobRepository.findById(jobId).orElse(null);
        if (job == null) return ResponseEntity.notFound().build();

        String resumeFileName = null, resumeFilePath = null;
        if (!resume.isEmpty()) {
            try {
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
                String original   = resume.getOriginalFilename();
                String sanitized  = original != null ? original.replaceAll("[^a-zA-Z0-9._-]", "_") : "resume";
                String uniqueName = System.currentTimeMillis() + "_" + sanitized;
                Path filePath     = uploadPath.resolve(uniqueName).normalize();
                if (!filePath.startsWith(uploadPath))
                    return ResponseEntity.badRequest().body("Invalid file path.");
                Files.copy(resume.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                resumeFileName = original;
                resumeFilePath = filePath.toAbsolutePath().toString();
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to upload resume: " + e.getMessage());
            }
        }

        JobApplication app = new JobApplication();
        app.setJob(job);
        app.setApplicantName(applicantName);
        app.setApplicantEmail(applicantEmail);
        app.setApplicantPhone(applicantPhone);
        app.setCoverLetter(coverLetter);
        app.setResumeFileName(resumeFileName);
        app.setResumeFilePath(resumeFilePath);
        app.setAppliedByUsername(username);
        applicationRepository.save(app);

        return ResponseEntity.status(HttpStatus.CREATED).body("Application submitted successfully!");
    }

    // ── ADMIN: All applications — filtered to own company ──
    @GetMapping
    public ResponseEntity<List<JobApplication>> getAllApplications(Authentication authentication) {
        Long adminCid = AdminUtils.getAdminCompanyId(authentication);
        List<JobApplication> all = applicationRepository.findAll();
        if (adminCid != null) {
            all = all.stream()
                    .filter(a -> a.getJob() != null
                            && a.getJob().getCompany() != null
                            && a.getJob().getCompany().getId().equals(adminCid))
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(all);
    }

    // ── ADMIN: Applications for a specific job — must belong to admin's company ──
    @GetMapping("/job/{jobId}")
    public ResponseEntity<?> getApplicationsForJob(@PathVariable Long jobId,
                                                   Authentication authentication) {
        Long adminCid = AdminUtils.getAdminCompanyId(authentication);
        if (adminCid != null) {
            Job job = jobRepository.findById(jobId).orElse(null);
            if (job == null) return ResponseEntity.notFound().build();
            if (job.getCompany() == null || !job.getCompany().getId().equals(adminCid))
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("This job does not belong to your company.");
        }
        return ResponseEntity.ok(applicationRepository.findByJobId(jobId));
    }

    // ── USER: My applications ──
    @GetMapping("/my")
    public ResponseEntity<List<JobApplication>> getMyApplications(Authentication authentication) {
        return ResponseEntity.ok(
                applicationRepository.findByAppliedByUsername(authentication.getName()));
    }

    // ── Check applied ──
    @GetMapping("/check/{jobId}")
    public ResponseEntity<Boolean> checkApplied(@PathVariable Long jobId,
                                                Authentication authentication) {
        return ResponseEntity.ok(
                applicationRepository.existsByJobIdAndAppliedByUsername(
                        jobId, authentication.getName()));
    }

    // ── ADMIN: Download resume — must belong to admin's company ──
    @GetMapping("/resume/{applicationId}")
    public ResponseEntity<Resource> downloadResume(@PathVariable Long applicationId,
                                                   Authentication authentication) {
        JobApplication app = applicationRepository.findById(applicationId).orElse(null);
        if (app == null) return ResponseEntity.notFound().build();

        Long adminCid = AdminUtils.getAdminCompanyId(authentication);
        if (adminCid != null && (app.getJob() == null
                || app.getJob().getCompany() == null
                || !app.getJob().getCompany().getId().equals(adminCid)))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        if (app.getResumeFilePath() == null) return ResponseEntity.notFound().build();

        try {
            Path filePath   = Paths.get(app.getResumeFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

            String filename    = app.getResumeFileName() != null ? app.getResumeFileName() : "resume";
            String contentType = "application/octet-stream";
            if (filename.endsWith(".pdf"))  contentType = "application/pdf";
            else if (filename.endsWith(".doc"))  contentType = "application/msword";
            else if (filename.endsWith(".docx")) contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ── ADMIN: Delete application ──
    @DeleteMapping("/{applicationId}")
    public ResponseEntity<String> deleteApplication(@PathVariable Long applicationId) {
        JobApplication app = applicationRepository.findById(applicationId).orElse(null);
        if (app != null && app.getResumeFilePath() != null) {
            try { Files.deleteIfExists(Paths.get(app.getResumeFilePath())); } catch (IOException ignored) {}
        }
        applicationRepository.deleteById(applicationId);
        return ResponseEntity.ok("Application deleted.");
    }
}