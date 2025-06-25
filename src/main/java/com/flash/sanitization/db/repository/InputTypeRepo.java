package com.flash.sanitization.db.repository;

import com.flash.sanitization.db.entity.ConfigEntity;
import com.flash.sanitization.db.entity.InputTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Repository
public interface InputTypeRepo extends JpaRepository<InputTypeEntity, String>  {

    @Query("""
        SELECT c.sanitizer
        FROM InputTypeEntity i
        JOIN i.configs c
        WHERE i.type = :type
    """)
    List<String> findSanitizerNamesByInputType(@Param("type") String type);

    @Query(
        """
        SELECT i.configs
        FROM InputTypeEntity i
        WHERE i.type = :type
    """
    )
    List<ConfigEntity> findConfigByInputType(@Param("type") String type);

    boolean existsByType(@Param("type") String inputTypeName);
}
