package com.capg.ApplicationService.service;
import com.capg.ApplicationService.dto.ApplicationRequest;
import com.capg.ApplicationService.dto.ApplicationResponse;
import com.capg.ApplicationService.dto.StatusUpdateRequest;
import com.capg.ApplicationService.entity.Application;
import com.capg.ApplicationService.entity.ApplicationStatus;
import com.capg.ApplicationService.exception.DuplicateApplicationException;
import com.capg.ApplicationService.exception.ResourceNotFoundException;
import com.capg.ApplicationService.repository.ApplicationRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ApplicationServiceImpl implements ApplicationService{

    private ModelMapper modelMapper;
    private ApplicationRepository repo;

    @Override
    public ApplicationResponse apply(ApplicationRequest request) {
        if(repo.existsByUserIdAndJobId(request.getUserId(), request.getJobId())){
            throw new DuplicateApplicationException("you already applied!");
        }
        Application app= modelMapper.map(request, Application.class);
        app.setStatus(ApplicationStatus.APPLIED);
        app.setAppliedAt(LocalDateTime.now());

        repo.save(app);

        return modelMapper.map(app,ApplicationResponse.class);
    }

    @Override
    public List<ApplicationResponse> getUserApplications(Long userId) {

        return repo.findByUserId(userId)
                .stream()
                .map(app -> modelMapper.map(app, ApplicationResponse.class))
                .toList();
    }

    @Override
    public List<ApplicationResponse> getJobApplicants(Long jobId) {

        return repo.findByJobId(jobId)
                .stream()
                .map(app -> modelMapper.map(app, ApplicationResponse.class))
                .toList();
    }

    @Override
    public ApplicationResponse updateStatus(StatusUpdateRequest request) {
        Application app = repo.findById(request.getApplicationId())
                .orElseThrow(()-> new ResourceNotFoundException("Not found"));

        app.setStatus(request.getStatus());
        repo.save(app);

        return modelMapper.map(app, ApplicationResponse.class);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deleteByUserId(Long userId) {
        repo.deleteByUserId(userId);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deleteByJobId(Long jobId) {
        repo.deleteByJobId(jobId);
    }
}
