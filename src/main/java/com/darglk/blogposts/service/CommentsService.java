package com.darglk.blogposts.service;

import com.darglk.blogposts.rest.model.CommentRequest;
import com.darglk.blogposts.rest.model.CommentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CommentsService {
    CommentResponse createComment(String userId, String postId, CommentRequest request, List<MultipartFile> files);
    void upvoteComment(String commentId);
    void deleteComment(String commentId);

    void toggleFavorite(String commentId);
}
