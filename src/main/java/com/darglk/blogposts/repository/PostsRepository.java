package com.darglk.blogposts.repository;

import com.darglk.blogposts.repository.entity.PostEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PostsRepository {
    List<PostEntity> select();

    void insert(String id, String userId, String content);

    Optional<PostEntity> selectById(String postId);

    void delete(String postId);
}
