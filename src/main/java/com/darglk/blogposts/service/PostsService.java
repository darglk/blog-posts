package com.darglk.blogposts.service;

import com.darglk.blogposts.rest.model.PostRequest;
import com.darglk.blogposts.rest.model.PostResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostsService {
    List<PostResponse> getPosts();
    PostResponse createPost(String userId, PostRequest request, List<MultipartFile> files);
    void deletePost(String postId);

    void toggleUpvote(String postId);

    void toggleFavorite(String postId);

    void addAttachment(String postId, MultipartFile file);

    void removeAttachment(String postId, String attachmentId);

    PostResponse updatePost(String postId, PostRequest postRequest);
}
