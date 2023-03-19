package com.darglk.blogposts.service;

import com.darglk.blogposts.repository.PostsRepository;
import com.darglk.blogposts.rest.model.PostResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostsServiceImpl implements PostsService {

    private final PostsRepository postsRepository;

    @Override
    public List<PostResponse> getPosts() {
        return postsRepository.select().stream()
                .map(p -> new PostResponse(p.getId(), p.getContent()))
                .collect(Collectors.toList());
    }
}
