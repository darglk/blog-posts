package com.darglk.blogposts.repository;

import com.darglk.blogposts.repository.entity.CommentUpvoteEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentUpvotesRepository {
    boolean exists(String commentId, String userId);
    void insert(CommentUpvoteEntity entity);
    void delete(String commentId, String userId);
}
