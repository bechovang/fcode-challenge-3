package com.gameaccountshop.repository;

import com.gameaccountshop.entity.User;
import com.gameaccountshop.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    long count();

    /**
     * Find users by role
     * @param role Role to filter by
     * @return List of users with the specified role
     */
    List<User> findByRole(Role role);
}
