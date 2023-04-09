package com.darglk.blogposts.repository;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentsFavoritesRepository {
    void insert(String commentId, String userId);
    void delete(String commentId, String userId);
    boolean exists(String commentId, String userId);
}
