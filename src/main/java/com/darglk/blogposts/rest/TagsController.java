package com.darglk.blogposts.rest;

import com.darglk.blogposts.service.TagsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts/tags")
@RequiredArgsConstructor
public class TagsController {

    private final TagsService tagsService;

    @PostMapping("/blacklist/{tag}")
    public void blacklistTag(@PathVariable("tag") String tag) {
        tagsService.blacklistTag(tag);
    }

    @PostMapping("/{tag}/favorite")
    public void toggleFavorite(@PathVariable("tag") String tag) {
        tagsService.toggleFavorite(tag);
    }
}
