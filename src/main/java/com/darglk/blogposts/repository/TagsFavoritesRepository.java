package com.darglk.blogposts.repository;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TagsFavoritesRepository {
    void insert(String tagName, String userId);
    void delete(String tagName, String userId);
    boolean exists(String tagName, String userId);
    void deleteAll();
}
