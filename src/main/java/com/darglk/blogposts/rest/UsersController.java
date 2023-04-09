package com.darglk.blogposts.rest;

import com.darglk.blogposts.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts/users")
@RequiredArgsConstructor
public class UsersController {

    private final UsersService usersService;

    @PostMapping("/blacklist/{userId}")
    public void blacklistUser(@PathVariable("userId") String userId) {
        usersService.blacklistUser(userId);
    }

    @PostMapping("/{userId}/favorite")
    public void toggleFavorite(@PathVariable("userId") String userId) {
        usersService.toggleFavorite(userId);
    }
}
