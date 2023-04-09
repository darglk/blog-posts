package com.darglk.blogposts.repository;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TagsRepository {
    boolean exists(String name);
    void insert(String name);
}
