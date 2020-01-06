package com.grademe.grademe.repository;

import com.grademe.grademe.beans.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer> {
}
