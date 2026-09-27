package com.ioc.microservice.identityservice.repository;

import com.ioc.microservice.identityservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Yêu cầu phải là 1 interface, không phải là class
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}