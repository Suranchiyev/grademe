package com.grademe.grademe.service;

import com.grademe.grademe.beans.User;

public interface UserService {
    void save(User user);

    User findByUsername(String username);
}
