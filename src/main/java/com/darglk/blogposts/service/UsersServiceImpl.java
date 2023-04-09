package com.darglk.blogposts.service;

import com.darglk.blogcommons.exception.ErrorResponse;
import com.darglk.blogcommons.exception.NotFoundException;
import com.darglk.blogcommons.exception.ValidationException;
import com.darglk.blogcommons.model.UserPrincipal;
import com.darglk.blogposts.repository.UsersBlacklistRepository;
import com.darglk.blogposts.repository.UsersFavoritesRepository;
import com.darglk.blogposts.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    private final UsersBlacklistRepository usersBlacklistRepository;
    private final UsersFavoritesRepository usersFavoritesRepository;
    private final UsersRepository usersRepository;

    @Override
    @Transactional
    public void blacklistUser(String userId) {
        if (!usersRepository.exists(userId)) {
            throw new NotFoundException("Not found user with id: " + userId);
        }
        var blockingUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        if (blockingUser.equals(userId)) {
            throw new ValidationException(List.of(new ErrorResponse("You cannot blacklist yourself", "userId")));
        }
        if (!usersBlacklistRepository.exists(blockingUser, userId)) {
            usersFavoritesRepository.delete(blockingUser, userId);
            usersBlacklistRepository.insert(blockingUser, userId);
        } else {
            usersBlacklistRepository.delete(blockingUser, userId);
        }
    }

    @Override
    @Transactional
    public void toggleFavorite(String userId) {
        if (!usersRepository.exists(userId)) {
            throw new NotFoundException("Not found user with id: " + userId);
        }
        var favoriteUserId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        if (favoriteUserId.equals(userId)) {
            throw new ValidationException(List.of(new ErrorResponse("You cannot favorite yourself", "userId")));
        }
        if (usersFavoritesRepository.exists(favoriteUserId, userId)) {
            usersFavoritesRepository.delete(favoriteUserId, userId);
        } else {
            usersBlacklistRepository.delete(favoriteUserId, userId);
            usersFavoritesRepository.insert(favoriteUserId, userId);
        }
    }
}
