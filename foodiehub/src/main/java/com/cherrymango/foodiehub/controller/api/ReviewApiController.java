package com.cherrymango.foodiehub.controller.api;

import com.cherrymango.foodiehub.dto.review.AddReviewRequestDto;
import com.cherrymango.foodiehub.dto.review.MyPageReviewResponseDto;
import com.cherrymango.foodiehub.dto.review.StoreReviewResponseDto;
import com.cherrymango.foodiehub.dto.review.UpdateReviewRequestDto;
import com.cherrymango.foodiehub.dto.review.PagedResponseDto;
import com.cherrymango.foodiehub.file.FileStore;
import com.cherrymango.foodiehub.service.ReviewService;
import com.cherrymango.foodiehub.util.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewApiController {
    private final ReviewService reviewService;
    private final FileStore fileStore;
    private final TokenUtil tokenUtil;

    @GetMapping("/image/{filename}")
    public Resource downloadReviewImage(@PathVariable("filename") String filename) throws MalformedURLException {
        return new UrlResource("file:" + fileStore.getFullPath(filename));
    }

    @PostMapping("/{storeId}")
    public ResponseEntity<Long> addReview(@PathVariable("storeId") Long storeId,
                                          @ModelAttribute @Valid AddReviewRequestDto addReviewRequestDto,
                                          Principal principal, HttpServletRequest request) {
        Long userId = tokenUtil.getSiteUserIdOrThrow(principal, request);
        Long reviewId = reviewService.save(userId, storeId, addReviewRequestDto);
        return ResponseEntity.ok(reviewId);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<Void> updateReview(@PathVariable("reviewId") Long reviewId,
                                             @ModelAttribute @Valid UpdateReviewRequestDto updateReviewRequestDto) {
        System.out.println("updateReviewRequestDto = " + updateReviewRequestDto);
        reviewService.updateReview(reviewId, updateReviewRequestDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Boolean> deleteReview(@PathVariable("reviewId") Long reviewId) {
        boolean isDeleted = reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(isDeleted);
    }

    // 리뷰 상세 정보 조회
    @GetMapping("/{reviewId}")
    public ResponseEntity<MyPageReviewResponseDto> findReview(@PathVariable("reviewId") Long reviewId) {
        MyPageReviewResponseDto review = reviewService.findReviewById(reviewId);
        return ResponseEntity.ok(review);
    }

    // 내 가게 리뷰 목록
    @GetMapping("/mystore/{storeId}")
    public ResponseEntity<List<StoreReviewResponseDto>> findStoreReviews(@PathVariable("storeId") Long storeId) {
        List<StoreReviewResponseDto> storeReviews = reviewService.getStoreReviews(storeId);
        return ResponseEntity.ok(storeReviews);
    }

    // 가게 상세 리뷰 목록
    @GetMapping("/store/{storeId}")
    public ResponseEntity<PagedResponseDto<StoreReviewResponseDto>> findPagedStoreReviews(@PathVariable("storeId") Long storeId,
                                                                                          @RequestParam(value = "page", defaultValue = "0") int page, Principal principal, HttpServletRequest request) {
        Long userId = tokenUtil.getSiteUserIdOrNull(principal, request);
        PagedResponseDto<StoreReviewResponseDto> storeReviews = reviewService.getPagedStoreReviews(storeId, page, userId);
        return ResponseEntity.ok(storeReviews);
    }

    // 마이페이지 리뷰 목록
    @GetMapping("/user")
    public ResponseEntity<List<MyPageReviewResponseDto>> findAllMyPageReviews(Principal principal, HttpServletRequest request) {
        Long userId = tokenUtil.getSiteUserIdOrThrow(principal, request);
        List<MyPageReviewResponseDto> userReviews = reviewService.findReviewsByUser(userId);
        return ResponseEntity.ok(userReviews);
    }

}
