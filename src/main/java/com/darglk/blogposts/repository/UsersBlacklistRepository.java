package com.darglk.blogposts.repository;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UsersBlacklistRepository {
    void insert(String blacklistedUser, String userId);
    void delete(String blacklistedUser, String userId);
    boolean exists(String blacklistedUser, String userId);
}
