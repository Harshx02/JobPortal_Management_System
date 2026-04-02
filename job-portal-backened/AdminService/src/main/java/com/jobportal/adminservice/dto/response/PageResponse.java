package com.jobportal.adminservice.dto.response;

import lombok.Data;
import lombok.Builder;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse {
    private List<JobResponse> content;
    private int totalPages;
    private long totalElements;
    private int size;
    private int number;
    private boolean last;
}