package com.dbproj.manageprompt.dto;

import com.dbproj.manageprompt.entity.ProjectEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ProjectSpecificationResponseDto {
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private final Date startDate;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    private final Date endDate;

    private final String proName;
    private final String proId;
    private final Integer budget;
//    private final String state;
    private final Integer numOfParicipant;
    private final String clientName;
    private final List<EmployeeProjectResponseDto> participantList; // PM 찾는 용도

    public static ProjectSpecificationResponseDto from(ProjectEntity projectEntity) {
        return ProjectSpecificationResponseDto.builder()
                .startDate(projectEntity.getStartDate())
                .endDate(projectEntity.getEndDate())
                .proName(projectEntity.getProName())
                .proId(projectEntity.getProId())
                .budget(projectEntity.getBudget())
//                .state(projectEntity.get.getState())
                .numOfParicipant(projectEntity.getEmployeeProjectEntities().size())
                .clientName(projectEntity.getClientInfoEntity().getClientName())
                // PM 찾는 용도
                .participantList(projectEntity.getEmployeeProjectEntities().stream().map(EmployeeProjectResponseDto::from).collect(Collectors.toList()))
                .build();
    }
}
