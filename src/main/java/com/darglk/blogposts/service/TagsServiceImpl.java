package com.darglk.blogposts.service;

import com.darglk.blogcommons.exception.NotFoundException;
import com.darglk.blogcommons.model.UserPrincipal;
import com.darglk.blogposts.repository.TagsBlacklistRepository;
import com.darglk.blogposts.repository.TagsFavoritesRepository;
import com.darglk.blogposts.repository.TagsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TagsServiceImpl implements TagsService {

    private final TagsBlacklistRepository tagsBlacklistRepository;
    private final TagsFavoritesRepository tagsFavoritesRepository;
    private final TagsRepository tagsRepository;

    @Override
    public void toggleBlacklistTag(String tag) {
        if (!tagsRepository.exists(tag)) {
            throw new NotFoundException("Not found tag with name: " + tag);
        }
        var userId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        if (tagsBlacklistRepository.exists(tag, userId)) {
            tagsBlacklistRepository.delete(tag, userId);
        } else {
            tagsFavoritesRepository.delete(tag, userId);
            tagsBlacklistRepository.insert(tag, userId);
        }
    }

    @Override
    @Transactional
    public void toggleFavorite(String tag) {
        if (!tagsRepository.exists(tag)) {
            throw new NotFoundException("Not found tag with name: " + tag);
        }
        var userId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        if (tagsFavoritesRepository.exists(tag, userId)) {
            tagsFavoritesRepository.delete(tag, userId);
        } else {
            tagsBlacklistRepository.delete(tag, userId);
            tagsFavoritesRepository.insert(tag, userId);
        }
    }
}
