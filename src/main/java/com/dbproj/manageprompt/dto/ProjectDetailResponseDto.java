package com.dbproj.manageprompt.dto;

import com.dbproj.manageprompt.entity.EvaluationRequestEntity;
import com.dbproj.manageprompt.entity.ProjectEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

// 프로젝트 관리
@Getter
@Builder(access = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ProjectDetailResponseDto {
    private final String proId;
    private final String proName;
    private final Date startDate;
    private final Date endDate;
    private final Integer budget;
    private final String pmName;
    private final Long cliendId;
    private final String clientName;
    private final String clientEmpName;
    private final String clientEmpPh;
    private final String clientEmpEmail;
    private final Integer numOfParicipant;
    private final List<EmployeeProjectResponseDto> participantList;
    private final List<EmployeeProjectEvaluationResponseDto> empEvaluationList;
    private final List<EvaluationRequestResponseDto> clientEvaluationList;

    public static ProjectDetailResponseDto from(ProjectEntity projectEntity) {
        return ProjectDetailResponseDto.builder()
                .proId(projectEntity.getProId())
                .proName(projectEntity.getProName())
                .startDate(projectEntity.getStartDate())
                .endDate(projectEntity.getEndDate())
                .budget(projectEntity.getBudget())
                .cliendId(projectEntity.getClientInfoEntity().getClientId())
                .clientName(projectEntity.getClientInfoEntity().getClientName())
                .clientEmpName(projectEntity.getClientInfoEntity().getClientEmpName())
                .clientEmpPh(projectEntity.getClientInfoEntity().getClientEmpPh())
                .clientEmpEmail(projectEntity.getClientInfoEntity().getClientEmpEmail())
                .numOfParicipant(projectEntity.getEmployeeProjectEntities().size())
                .participantList(projectEntity.getEmployeeProjectEntities().stream().map(EmployeeProjectResponseDto::from).collect(Collectors.toList()))
                .empEvaluationList(projectEntity.getEmployeeProjectEntities().stream().map(EmployeeProjectEvaluationResponseDto::from).collect(Collectors.toList()))
                .clientEvaluationList(projectEntity.getClientInfoEntity().getEvaluationRequestEntities().stream().map(EvaluationRequestResponseDto::from).collect(Collectors.toList()))
                .build();
    }
}