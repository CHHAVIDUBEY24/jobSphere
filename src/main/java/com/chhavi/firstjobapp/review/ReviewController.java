package com.chhavi.firstjobapp.review;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies/{companyId}/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    private ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<?> getAllReviews(@PathVariable Long companyId,
                                           Authentication authentication) {
        // Admin can only see reviews of their own company
        if (authentication != null) {
            Object details = authentication.getDetails();
            if (details instanceof Long adminCompanyId) {
                if (!adminCompanyId.equals(companyId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("You can only view reviews for your own company.");
                }
            }
        }
        return new ResponseEntity<>(reviewService.getAllReviews(companyId), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<String> addReview(@PathVariable Long companyId,
                                            @RequestBody Review review) {
        boolean saved = reviewService.addReview(companyId, review);
        if (saved) return new ResponseEntity<>("Review Added Successfully", HttpStatus.OK);
        return new ResponseEntity<>("Review Not Saved", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<Review> getReview(@PathVariable Long companyId,
                                            @PathVariable Long reviewId) {
        Review review = reviewService.getReview(companyId, reviewId);
        if (review != null) return new ResponseEntity<>(review, HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<String> updateReview(@PathVariable Long companyId,
                                               @PathVariable Long reviewId,
                                               @RequestBody Review review) {
        boolean updated = reviewService.updateReview(companyId, reviewId, review);
        if (updated) return new ResponseEntity<>("Review Updated Successfully", HttpStatus.OK);
        return new ResponseEntity<>("Review not Updated", HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable Long companyId,
                                               @PathVariable Long reviewId) {
        boolean deleted = reviewService.deleteReview(companyId, reviewId);
        if (deleted) return new ResponseEntity<>("Review Deleted Successfully", HttpStatus.OK);
        return new ResponseEntity<>("Review not Deleted", HttpStatus.NOT_FOUND);
    }
}