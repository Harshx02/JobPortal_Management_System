package com.capg.ApplicationService.repository;

import com.capg.ApplicationService.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByUserId(Long userId);

    List<Application> findByJobId(Long jobId);

    boolean existsByUserIdAndJobId(Long userId, Long jobId);

    @Transactional
    void deleteByUserId(Long userId);

    @Transactional
    void deleteByJobId(Long jobId);
}
