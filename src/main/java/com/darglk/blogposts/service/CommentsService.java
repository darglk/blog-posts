package com.darglk.blogposts.service;

import com.darglk.blogposts.rest.model.CommentRequest;
import com.darglk.blogposts.rest.model.CommentResponse;
import com.darglk.blogposts.rest.model.PostResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CommentsService {
    CommentResponse createComment(String userId, String postId, CommentRequest request, List<MultipartFile> files);
    void upvoteComment(String commentId);
    void deleteComment(String commentId);

    void toggleFavorite(String commentId);

    void addAttachment(String commentId, MultipartFile file);

    void removeAttachment(String commentId, String attachmentId);

    CommentResponse updateComment(String commentId, CommentRequest commentRequest);
}
