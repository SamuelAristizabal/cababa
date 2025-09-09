package com.basketball.referee.repository;

import com.basketball.referee.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Role findByName(String name);
    
    boolean existsByName(String name);
}
