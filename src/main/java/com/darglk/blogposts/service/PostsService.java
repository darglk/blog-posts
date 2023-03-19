package com.darglk.blogposts.service;

import com.darglk.blogposts.rest.model.PostResponse;

import java.util.List;

public interface PostsService {
    List<PostResponse> getPosts();
}
