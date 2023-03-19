package com.darglk.blogposts.rest;

import com.darglk.blogposts.rest.model.PostResponse;
import com.darglk.blogposts.service.PostsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostsController {

    private final PostsService postsService;

    @GetMapping
    public List<PostResponse> getPosts() {
        return postsService.getPosts();
    }
}
