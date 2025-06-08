package com.flash.sanitization.db.entity;

import com.flash.sanitization.db.mapper.ConfigDbMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * A given input type can have multiple sanitizers for example a comment on a website
 * can have a html sanitizer (to prevent html injection) but also a profanity sanitizer.
 * <br />
 */
@Getter
@Setter
@Entity
@Table(name = "tbl_sensitizer_config")
@NoArgsConstructor
@AllArgsConstructor
public class ConfigEntity {

    @Id
    @Column(name = "sanitizer_name", nullable = false)
    private String sanitizer;

    @Column(name = "sanitizer_factory_name")
    private String factory;

    @Column(name = "sanitizer_config")
    @Convert(converter = ConfigDbMapper.class)
    private Map<String, String> config;
}
