package com.darglk.blogposts.repository;

import com.darglk.blogposts.repository.entity.CommentEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CommentsRepository {
    void insert(CommentEntity entity);
    List<CommentEntity> select(String postId);
    Optional<CommentEntity> selectById(String commentId);
    void delete(String commentId);
}
