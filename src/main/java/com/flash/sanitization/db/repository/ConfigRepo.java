package com.flash.sanitization.db.repository;

import com.flash.sanitization.db.entity.ConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public interface ConfigRepo extends JpaRepository<ConfigEntity, String> {
}
