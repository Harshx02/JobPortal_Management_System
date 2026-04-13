package com.jobportal.jobservice.specification;

import com.jobportal.jobservice.dto.JobFilterDto;
import com.jobportal.jobservice.entity.Job;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JobSpecificationTest {

    private Root<Job> root;
    private CriteriaQuery<?> query;
    private CriteriaBuilder cb;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        root = mock(Root.class);
        query = mock(CriteriaQuery.class);
        cb = mock(CriteriaBuilder.class);
    }

    @Test
    void getFilteredJobs_WithAllFields_ReturnsMergedPredicate() {
        // Arrange
        JobFilterDto filter = new JobFilterDto();
        filter.setTitle("Java");
        filter.setSkill("Spring");
        filter.setLocation("NY");
        filter.setCompanyName("Google");
        filter.setMinSalary(50000.0);
        filter.setMaxSalary(150000.0);
        filter.setMinExperience(2);
        filter.setMaxExperience(10);

        Specification<Job> spec = JobSpecification.getFilteredJobs(filter);

        // Act
        spec.toPredicate(root, query, cb);

        // Assert
        verify(cb, atLeastOnce()).like(any(), anyString());
        verify(cb, atLeastOnce()).greaterThanOrEqualTo(any(), any(Double.class));
        verify(cb, times(1)).and(any(Predicate[].class));
    }

    @Test
    void getFilteredJobs_WithEmptyFilter_ReturnsAnd() {
        // Arrange
        JobFilterDto filter = new JobFilterDto();
        Specification<Job> spec = JobSpecification.getFilteredJobs(filter);

        // Act
        spec.toPredicate(root, query, cb);

        // Assert
        verify(cb).and(any(Predicate[].class));
        verify(cb, never()).like(any(), anyString());
    }

    @Test
    void getFilteredJobs_WithNullValues_DoesNotAddPredicates() {
        // Arrange
        JobFilterDto filter = new JobFilterDto();
        filter.setTitle("");
        filter.setSkill(null);
        
        Specification<Job> spec = JobSpecification.getFilteredJobs(filter);

        // Act
        spec.toPredicate(root, query, cb);

        // Assert
        verify(cb, never()).like(any(), anyString());
    }
}
