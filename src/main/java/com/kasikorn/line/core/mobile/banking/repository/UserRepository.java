package com.kasikorn.line.core.mobile.banking.repository;

import com.kasikorn.line.core.mobile.banking.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

}