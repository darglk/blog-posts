package com.darglk.blogposts.service;

public interface TagsService {
    void blacklistTag(String tag);

    void toggleFavorite(String tag);
}
