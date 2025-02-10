package com.kasikorn.line.core.mobile.banking.repository;

import com.kasikorn.line.core.mobile.banking.entity.BannersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BannersRepository extends JpaRepository<BannersEntity, String> {
    Optional<List<BannersEntity>> findByUserId(String userId);
}