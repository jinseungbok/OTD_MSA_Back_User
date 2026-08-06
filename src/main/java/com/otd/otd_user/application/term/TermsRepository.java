package com.otd.otd_user.application.term;

import com.otd.otd_user.entity.Terms;
import com.otd.configuration.enumcode.model.EnumTermsType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TermsRepository extends JpaRepository<Terms, Long> {

    List<Terms> findByIsActiveTrueOrderByTypeAsc();

    Optional<Terms> findByTypeAndIsActiveTrue(EnumTermsType type);

    List<Terms> findByTypeOrderByVersionDesc(EnumTermsType type);

    List<Terms> findByIsActiveTrueAndIsRequiredTrue();
}