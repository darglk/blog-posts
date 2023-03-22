package com.darglk.blogposts.repository;

import com.darglk.blogposts.repository.entity.PostEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PostsRepository {
    List<PostEntity> select();

    void insert(String id, String userId, String content);
}
