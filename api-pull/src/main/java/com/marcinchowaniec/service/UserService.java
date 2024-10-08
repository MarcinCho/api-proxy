package com.marcinchowaniec.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marcinchowaniec.entity.User;
import com.marcinchowaniec.exceptions.UserNotFoundException;
import com.marcinchowaniec.httpClient.GithubHttpClient;
import com.marcinchowaniec.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepo;

    @Inject
    GithubHttpClient githubHttpClient;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Transactional
    public void saveUser(User user) {
        logger.info("Saving " + user.login + " to internal DB.");
        userRepo.persist(user);
    }

    @Transactional
    public Optional<User> getUserByLogin(String login) throws NotFoundException {
        Optional<User> user = userRepo.findByLogin(login);
        if (user.isPresent()) {
            return user;
        }
        try {
            logger.info("Pulling -> " + login + " from Github");
            user = githubHttpClient.getGithubUser(login);
            saveUser(user.get());
            return user;
        } catch (UserNotFoundException e) {
            throw new NotFoundException();
        }

    }

    public boolean checkUserByLogin(String login) {
        logger.info("Checkin if -> " + login + " exist in DB");
        return userRepo.findByLogin(login).isPresent();
    }

    public long deleteUser(String login) {
        return userRepo.deleteByLogin(login);
    }
}
