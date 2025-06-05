package com.satyam.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.satyam.account.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long>{
}
