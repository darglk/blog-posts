package com.darglk.blogposts.rest;

import com.darglk.blogcommons.exception.ValidationException;
import com.darglk.blogcommons.model.UserPrincipal;
import com.darglk.blogposts.rest.model.CommentRequest;
import com.darglk.blogposts.rest.model.CommentResponse;
import com.darglk.blogposts.service.CommentsService;
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
@RequestMapping("/api/v1/posts/comments")
@RequiredArgsConstructor
public class CommentsController {

    private final CommentsService commentsService;

    @PostMapping(path = "/post/{postId}", consumes = { APPLICATION_JSON_VALUE, MULTIPART_FORM_DATA_VALUE})
    public CommentResponse createComment(
            @PathVariable("postId") String postId,
            @Valid @RequestPart("commentRequest") CommentRequest commentRequest,
            @RequestPart(value = "file", required = false) List<MultipartFile> files, Errors errors) {
        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        }
        var userId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return commentsService.createComment(userId, postId, commentRequest, (files != null) ? files : Collections.emptyList());
    }

    @PostMapping("/{commentId}/upvote")
    public void upvotePost(@PathVariable("commentId") String commentId) {
        commentsService.upvoteComment(commentId);
    }

    @DeleteMapping("/{commentId}")
    public void deletePost(@PathVariable("commentId") String commentId) {
        commentsService.deleteComment(commentId);
    }

    @PostMapping("/{commentId}/favorite")
    public void toggleFavorite(@PathVariable("commentId") String commentId) {
        commentsService.toggleFavorite(commentId);
    }
}
