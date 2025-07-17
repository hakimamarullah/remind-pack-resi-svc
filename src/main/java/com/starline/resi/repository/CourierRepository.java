package com.starline.resi.repository;

import com.starline.resi.model.Courier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourierRepository extends JpaRepository<Courier, Long> {

     Page<Courier> findByNameContainingIgnoreCaseAndEnabledTrue(String name, Pageable pageable);

     Optional<Courier> findByCode(String code);
}
