package com.chhavi.firstjobapp.review;

import com.chhavi.firstjobapp.auth.AdminUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies/{companyId}/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews(@PathVariable Long companyId,
                                                      Authentication authentication) {
        Long adminCid = AdminUtils.getAdminCompanyId(authentication);
        // Admin requesting another company's reviews → return empty
        if (adminCid != null && !adminCid.equals(companyId)) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(reviewService.getAllReviews(companyId));
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<Review> getReview(@PathVariable Long companyId,
                                            @PathVariable Long reviewId) {
        Review review = reviewService.getReview(companyId, reviewId);
        if (review != null) return ResponseEntity.ok(review);
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<String> addReview(@PathVariable Long companyId,
                                            @RequestBody Review review) {
        boolean saved = reviewService.addReview(companyId, review);
        if (saved) return ResponseEntity.ok("Review Added Successfully");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Review Not Saved");
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<String> updateReview(@PathVariable Long companyId,
                                               @PathVariable Long reviewId,
                                               @RequestBody Review review) {
        boolean updated = reviewService.updateReview(companyId, reviewId, review);
        if (updated) return ResponseEntity.ok("Review Updated Successfully");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Review not Updated");
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable Long companyId,
                                               @PathVariable Long reviewId) {
        boolean deleted = reviewService.deleteReview(companyId, reviewId);
        if (deleted) return ResponseEntity.ok("Review Deleted Successfully");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Review not Deleted");
    }
}