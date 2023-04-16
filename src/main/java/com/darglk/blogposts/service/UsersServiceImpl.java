package com.darglk.blogposts.service;

import com.darglk.blogcommons.events.Subjects;
import com.darglk.blogcommons.events.model.UserCreatedEvent;
import com.darglk.blogcommons.exception.ErrorResponse;
import com.darglk.blogcommons.exception.NotFoundException;
import com.darglk.blogcommons.exception.ValidationException;
import com.darglk.blogcommons.model.UserPrincipal;
import com.darglk.blogposts.repository.UsersBlacklistRepository;
import com.darglk.blogposts.repository.UsersFavoritesRepository;
import com.darglk.blogposts.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
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
        if (!usersBlacklistRepository.exists(userId, blockingUser)) {
            usersFavoritesRepository.delete(userId, blockingUser);
            usersBlacklistRepository.insert(userId, blockingUser);
        } else {
            usersBlacklistRepository.delete(userId, blockingUser);
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
        if (usersFavoritesRepository.exists(userId, favoriteUserId)) {
            usersFavoritesRepository.delete(userId, favoriteUserId);
        } else {
            usersBlacklistRepository.delete(userId, favoriteUserId);
            usersFavoritesRepository.insert(userId, favoriteUserId);
        }
    }

    @RabbitListener(queues = { Subjects.USER_CREATED_QUEUE })
    @Transactional
    public void createUser(UserCreatedEvent event) {
        if (usersRepository.exists(event.getUserId())) {
            usersRepository.insert(event.getUserId(), event.getEmail());
        }
    }
}
