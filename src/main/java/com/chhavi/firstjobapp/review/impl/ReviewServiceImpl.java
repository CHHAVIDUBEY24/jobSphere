package com.chhavi.firstjobapp.review.impl;

import com.chhavi.firstjobapp.company.Company;
import com.chhavi.firstjobapp.company.CompanyService;
import com.chhavi.firstjobapp.review.Review;
import com.chhavi.firstjobapp.review.ReviewRepository;
import com.chhavi.firstjobapp.review.ReviewService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final CompanyService companyService;

    public ReviewServiceImpl(ReviewRepository reviewRepository,CompanyService companyService) {
        this.reviewRepository = reviewRepository;
        this.companyService=companyService;
    }

    @Override
    public List<Review> getAllReviews(Long companyId) {
        return reviewRepository.findByCompanyId(companyId);
    }

    @Override
    public boolean addReview(Long companyId, Review review) {
        Company company=companyService.CompanyById(companyId);
        if(company!=null)
        {
            review.setCompany(company);
            reviewRepository.save(review);
            return true;
        }
        return false;
    }

    @Override
    public Review getReview(Long companyId, Long reviewId) {
        List<Review>reviews=reviewRepository.findByCompanyId(companyId);
        return reviews.stream().filter(review -> review.getId().equals(reviewId)).findFirst().orElse(null);
    }

    @Override
    public boolean updateReview(Long companyId, Long reviewId, Review upadtedReview) {
        if(companyService.CompanyById(companyId)!=null)
        {
            upadtedReview.setCompany(companyService.CompanyById(companyId));
            upadtedReview.setId(reviewId);
            reviewRepository.save(upadtedReview);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteReview(Long companyId, Long reviewId) {
        if(companyService.CompanyById(companyId)!=null && reviewRepository.existsById(reviewId))
        {
            Review review=reviewRepository.findById(reviewId).orElse(null);
            Company company=review.getCompany();
            company.getReviews().remove(review);
            companyService.updateCompany(company,companyId);
            review.setCompany(null);
            reviewRepository.deleteById(reviewId);
            return true;
        }

        return false;
    }
}
