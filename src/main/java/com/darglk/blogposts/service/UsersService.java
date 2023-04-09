package com.darglk.blogposts.service;

public interface UsersService {
    void blacklistUser(String userId);

    void toggleFavorite(String userId);
}
