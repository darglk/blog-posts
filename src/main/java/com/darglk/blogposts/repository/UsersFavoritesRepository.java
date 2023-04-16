package com.darglk.blogposts.repository;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UsersFavoritesRepository {
    void insert(String favoriteUserId, String userId);
    void delete(String favoriteUserId, String userId);
    boolean exists(String favoriteUserId, String userId);
    void deleteAll();
}
