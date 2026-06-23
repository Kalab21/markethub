package com.markethub.app.service.imp;

import com.markethub.app.exception.ResourceNotFoundException;
import com.markethub.app.model.Review;
import com.markethub.app.repository.ReviewRepository;
import com.markethub.app.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Review getReviewById(long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + id));
    }

    @Override
    @Transactional
    public Review saveReview(Review review) {
        return reviewRepository.save(review);
    }

    @Override
    @Transactional
    public Review updateReview(long id, Review review) {
        review.setId(id);
        return reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void deleteReviewById(long id) {
        reviewRepository.deleteById(id);
    }
}
