package com.yakuba.repository;

import com.yakuba.model.PriceSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceSnapshotRepository extends JpaRepository<PriceSnapshotEntity, Long> {
}
