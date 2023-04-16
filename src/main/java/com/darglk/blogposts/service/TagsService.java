package com.darglk.blogposts.service;

public interface TagsService {
    void toggleBlacklistTag(String tag);

    void toggleFavorite(String tag);
}
