package com.darglk.blogposts.repository;

import com.darglk.blogposts.repository.entity.CommentAttachmentEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CommentAttachmentsRepository {
    List<CommentAttachmentEntity> select(String commentId);
    void insert(CommentAttachmentEntity entity);

    Optional<CommentAttachmentEntity> selectByUrl(String commentId, String url);

    void delete(String commentId, String url);
}
