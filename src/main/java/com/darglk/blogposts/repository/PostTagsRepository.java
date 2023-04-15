package com.darglk.blogposts.repository;

import com.darglk.blogposts.repository.entity.PostTagEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PostTagsRepository {
    void insert(PostTagEntity entity);
    void delete(String postId);

    List<PostTagEntity> select(String postId);
}
