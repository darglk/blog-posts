package com.darglk.blogposts.repository;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostsFavoritesRepository {
    void insert(String postId, String userId);
    void delete(String postId, String userId);
    boolean exists(String postId, String userId);
}
