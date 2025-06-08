package com.flash.sanitization.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "tbl_input_types")
@NoArgsConstructor
@AllArgsConstructor
public class InputTypeEntity {

    @Id
    @Column(name = "sansitizer_input_type", nullable = false)
    private String type;

    @ManyToMany
    @JoinTable(
        name = "tbl_input_type_config",
        joinColumns = @JoinColumn(name = "sansitizer_input_type", referencedColumnName = "sansitizer_input_type"),
        inverseJoinColumns = {
            @JoinColumn(name = "sanitizer_name", referencedColumnName = "sanitizer_name")
        }
    )
    private Set<ConfigEntity> configs = new HashSet<>();

    public InputTypeEntity addConfig(ConfigEntity config) {

        if(Objects.isNull(config)) {
            configs = new HashSet<>();
        }

        this.configs.add(config);
        return this;
    }

    public InputTypeEntity addConfig(List<ConfigEntity> config) {

        if(CollectionUtils.isEmpty(config)) {
            return this;
        }
        config.forEach(this::addConfig);
        return this;
    }

}
