package com.darglk.blogposts.repository;

import com.darglk.blogposts.repository.entity.PostUpvoteEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PostUpvotesRepository {
    boolean exists(String postId, String userId);
    void insert(PostUpvoteEntity entity);
    void delete(String postId, String userId);

    List<PostUpvoteEntity> select(String postId);
}
