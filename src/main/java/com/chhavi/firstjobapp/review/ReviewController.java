package com.chhavi.firstjobapp.review;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies/{companyId}")
//@CrossOrigin(origins = "*")
public class ReviewController {
    private ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }
    @GetMapping("/reviews")
    public ResponseEntity<List<Review>> getAllReviews(@PathVariable Long companyId)
    {
        return new ResponseEntity<>(reviewService.getAllReviews(companyId), HttpStatus.OK);
    }

    @PostMapping("/reviews")
    public ResponseEntity<String> addReview(@PathVariable Long companyId, @RequestBody Review review)
    {
        boolean isReviewSaved= reviewService.addReview(companyId,review);
        if(isReviewSaved)
        return new ResponseEntity<>("Review Added Successfully",HttpStatus.OK);
        else
            return new ResponseEntity<>("Review Not Saved",HttpStatus.NOT_FOUND);
    }
    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<Review> getReview(@PathVariable Long companyId,@PathVariable Long reviewId)
    {
        return new ResponseEntity<>(reviewService.getReview(companyId,reviewId),HttpStatus.OK);
    }

    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<String> updateReview(@PathVariable Long companyId,@PathVariable Long reviewId,@RequestBody Review review)
    {
        boolean isReviewUpdated= reviewService.updateReview(companyId,reviewId,review);
        if(isReviewUpdated)
        return new ResponseEntity<>("Review Updated Successfully",HttpStatus.OK);

        else
        return new ResponseEntity<>("Review not Uodated",HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable Long companyId,@PathVariable Long reviewId )
    {
        boolean isDeleted=reviewService.deleteReview(companyId,reviewId);
        if(isDeleted)
        return new ResponseEntity<>("Review Deleted Successfully",HttpStatus.OK);

        else
            return new ResponseEntity<>("Review not Deleted",HttpStatus.NOT_FOUND);
    }
}
