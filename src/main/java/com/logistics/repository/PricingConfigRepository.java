package com.logistics.repository;

import com.logistics.model.entity.PricingConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for PricingConfig entity.
 *
 * Only one pricing configuration should be active at a time.
 */
@Repository
public interface PricingConfigRepository extends JpaRepository<PricingConfig, Long> {

    /**
     * Finds the currently active pricing configuration.
     * There should only be one active config at any time.
     *
     * @return the active pricing configuration, if exists
     */
    Optional<PricingConfig> findByActiveTrue();

    /**
     * Deactivates all pricing configurations.
     * Used before setting a new active configuration.
     */
    @Modifying
    @Query("UPDATE PricingConfig p SET p.active = false WHERE p.active = true")
    void deactivateAll();
}
