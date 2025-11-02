package org.justdoit.blog.controller.jpa;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.config.auth.SessionUser;

import org.justdoit.blog.entity.cafe.CafePosting;
import org.justdoit.blog.entity.cafe.CafePostingRepository;
import org.justdoit.blog.service.s3.S3Service;
import org.justdoit.blog.template.S3Prefix;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/cafe")
public class PostJpaController {
    private final CafePostingRepository cafePostingRepository;

    private final HttpSession httpSession;
    private final S3Service s3Service;

    // 글 삭제
    @PostMapping("/post/delete/{id}")
    public ResponseEntity<String> deletePost(@PathVariable Long id) {
        SessionUser sessionUser = getSessionUser(httpSession);
        Optional<CafePosting> cafePostOpt = cafePostingRepository.findById(id);
        if (cafePostOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("F");
        }

        CafePosting cafePosting = cafePostOpt.get();
        List<String> fileNames = Arrays.stream(cafePosting.getImgList().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (!fileNames.isEmpty()) {
            s3Service.deleteImages(sessionUser, fileNames, S3Prefix.POST.getPrefix());
        }
        cafePostingRepository.delete(cafePosting);

        return ResponseEntity.ok("T");
    }


    private SessionUser getSessionUser(HttpSession session) {
        return Optional
                .ofNullable((SessionUser) session.getAttribute("basicUser"))
                .orElse((SessionUser) session.getAttribute("user"));
    }
}