
package com.SaveSmart.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.SaveSmart.user.entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
