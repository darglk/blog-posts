package com.darglk.blogposts.repository;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UsersRepository {

    boolean exists(String userId);
    void insert(String id, String name);
}
