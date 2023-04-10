package com.darglk.blogposts.rest;

import com.darglk.blogcommons.exception.ValidationException;
import com.darglk.blogcommons.model.UserPrincipal;
import com.darglk.blogposts.rest.model.PostRequest;
import com.darglk.blogposts.rest.model.PostResponse;
import com.darglk.blogposts.service.PostsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostsController {

    private final PostsService postsService;

    @GetMapping
    public List<PostResponse> getPosts() {
        return postsService.getPosts();
    }

    @PostMapping(consumes = { APPLICATION_JSON_VALUE, MULTIPART_FORM_DATA_VALUE})
    public PostResponse createPost(
            @Valid @RequestPart("postRequest") PostRequest postRequest,
            @RequestPart(value = "file", required = false) List<MultipartFile> files, Errors errors) {
        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        }
        var userId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return postsService.createPost(userId, postRequest, (files != null) ? files : Collections.emptyList());
    }

    @DeleteMapping("/{postId}")
    public void deletePost(@PathVariable("postId") String postId) {
        postsService.deletePost(postId);
    }

    @PostMapping("/{postId}/upvote")
    public void upvotePost(@PathVariable("postId") String postId) {
        postsService.upvotePost(postId);
    }

    @PostMapping("/{postId}/favorite")
    public void toggleFavorite(@PathVariable("postId") String postId) {
        postsService.toggleFavorite(postId);
    }

    @PostMapping(path = "/{postId}/attachment", consumes = { APPLICATION_JSON_VALUE, MULTIPART_FORM_DATA_VALUE})
    public void addAttachment(
            @PathVariable("postId") String postId,
            @RequestPart(value = "file") MultipartFile file) {
        postsService.addAttachment(postId, file);
    }

    @DeleteMapping("/{postId}/attachment/{attachmentId}")
    public void removeAttachment(
            @PathVariable("postId") String postId,
            @PathVariable("attachmentId") String attachmentId
    ) {
        postsService.removeAttachment(postId, attachmentId);
    }

    @PutMapping("/{postId}")
    public PostResponse updatePost(
            @PathVariable("postId") String postId,
            @Valid @RequestBody PostRequest postRequest,
            Errors errors
    ) {
        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        }
        return postsService.updatePost(postId, postRequest);
    }
}
