package com.teleticket.auth.repository;

import com.teleticket.auth.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    /**
     * Fetches the current role names for a user directly from the DB using a
     * native SQL query so that Hibernate's entity cache never serves a stale
     * role row after an out-of-band UPDATE on user_roles.role_id.
     */
    @Transactional(readOnly = true)
    @Query(value = "SELECT r.name FROM user_roles ur JOIN roles r ON ur.role_id = r.id WHERE ur.user_id = :userId",
           nativeQuery = true)
    List<String> findRoleNamesByUserId(@Param("userId") Long userId);
}
