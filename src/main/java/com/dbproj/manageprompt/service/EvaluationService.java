package com.dbproj.manageprompt.service;

import com.dbproj.manageprompt.Interface.ProjectInfoResponseInterface;
import com.dbproj.manageprompt.dao.*;
import com.dbproj.manageprompt.dto.*;
import com.dbproj.manageprompt.entity.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j

public class EvaluationService {
    private final ProjectDao projectDao;

    private final EvaluationInnerDao evaluationInnerDao;
    private final EvaluationRequestDao evaluationRequestDao;
    private final EmployeeProjectDao employeeProjectDao;
    private final ClientInfoDao clientInfoDao;
    private final AccountDao accountDao;
    private final EmployeeDao employeeDao;


    // 동료 평가 등록
    public Map coworkEvalcreate(Long addId, ParticipantEvaluationCreateRequestDto requestDto) {
        Optional<AccountEntity> accountEntity = accountDao.findByaccId(addId);
        AccountEntity account = accountEntity.get();
        Long empId = account.getEmployeeEntity().getEmpId();

        // 피평가자 직원의 직원프로젝트 조회
        EmployeeProjectEntity empProj = employeeProjectDao.
                findByProjectEntity_ProIdAndAndEmployeeEntity_EmpId(
                        requestDto.getPro_id(),
                        requestDto.getCoworker_emp_id()
                );

        if (empProj == null) {
            Map response = new HashMap<String, Object>();
            response.put("message", "관련 정보가 없습니다.");
            response.put("status", 0);

            return response;
        }

        EvaluationInnerEntity eval = evaluationInnerDao.findByEvaluator(empId, empProj.getEmpProId());
        if (eval != null) {
            Map response = new HashMap<String, Object>();
            response.put("message", "이미 평가한 회원입니다.");
            response.put("status", 0);

            return response;
        }

        requestDto.setEvaluator(empId); // 평가자
        requestDto.setCommunication_rating(requestDto.getCommunication_rating());
        requestDto.setCommunication_desc(requestDto.getCommunication_desc());
        requestDto.setPerformance_rating(requestDto.getPerformance_rating());
        requestDto.setPerformance_desc(requestDto.getPerformance_desc());
        requestDto.setEmployeeProject(empProj); // 피평가자의 직원프로젝트

        EvaluationInnerEntity newEval = requestDto.toEntity();
        newEval = evaluationInnerDao.save(newEval);

        Map response = new HashMap<String, Object>();
        response.put("message", "프로젝트에 평가가 등록되었습니다.");
        response.put("status", 1);
        response.put("eval_id", newEval.getEvalId());
        response.put("emp_pro_id", empProj.getEmpProId());

        return response;
    }

    // 고객 평가 등록
    public Map clientEvalcreate(Long addId, ClientEvaluationCreateRequestDto requestDto) {
        // 고객 ID (고객 사번)
        Optional<AccountEntity> accountEntity = accountDao.findByaccId(addId);
        AccountEntity account = accountEntity.get();
        String clientName = account.getEmployeeEntity().getEmpName();

        // 고객 정보
        ClientInfoEntity client = clientInfoDao.findByClientEmpName(clientName);
        if (client == null) {
            Map response = new HashMap<String, Object>();
            response.put("message", "관련 고객 정보가 없습니다.");
            response.put("status", 0);
            response.put("clientName", clientName);

            return response;
        }

        String proId = projectDao.findProIdByClientName(clientName).getPro_id();

        // 고객의 프로젝트 조회
        ProjectEntity project = projectDao.findByProId(proId);
        if (project == null) {
            Map response = new HashMap<String, Object>();
            response.put("message", "관련 프로젝트 정보가 없습니다.");
            response.put("status", 0);

            return response;
        }

        // 평가 여부 확인
        EvaluationRequestEntity evalClient = evaluationRequestDao.findByClientInfoEntity_clientName(clientName);
        if (evalClient != null) {
            Map response = new HashMap<String, Object>();
            response.put("message", "이미 평가하였습니다.");
            response.put("status", 0);

            return response;
        }

        requestDto.setCommunication_rating(requestDto.getCommunication_rating());
        requestDto.setCommunication_desc(requestDto.getCommunication_desc());
        requestDto.setPerformance_rating(requestDto.getPerformance_rating());
        requestDto.setPerformance_desc(requestDto.getPerformance_desc());
        requestDto.setClientInfo(client); // 발주처 정보

        EvaluationRequestEntity newEval = requestDto.toEntity();
        newEval = evaluationRequestDao.save(newEval);

        Map response = new HashMap<String, Object>();
        response.put("message", "프로젝트에 고객 평가가 등록되었습니다.");
        response.put("status", 1);
        response.put("cus_eval_id", newEval.getCusEvalId());
        response.put("client_id", newEval.getClientInfoEntity().getClientId());

        return response;
    }

    // 고객이 발주한 프로젝트 조회(평가페이지에서 프로젝트 정보 조회용)
    public Map getClientProject(Long addId) {
        // 고객 ID (고객 사번)
        Optional<AccountEntity> accountEntity = accountDao.findByaccId(addId);
        AccountEntity account = accountEntity.get();
        String clientEmpName = account.getEmployeeEntity().getEmpName();

        // 고객 정보
        ClientInfoEntity client = clientInfoDao.findByClientEmpName(clientEmpName);
        if (client == null) {
            Map response = new HashMap<String, Object>();
            response.put("message", "관련 고객 정보가 없습니다.");
            response.put("status", 0);
            response.put("clientName", clientEmpName);

            return response;
        }

        String proId = projectDao.findProIdByClientName(clientEmpName).getPro_id();

        ProjectInfoResponseInterface pmName = projectDao.findPMByProId(proId);

        // 고객의 프로젝트 조회
        ProjectEntity project = projectDao.findByProId(proId);
        if (project == null) {
            Map response = new HashMap<String, Object>();
            response.put("message", "관련 프로젝트 정보가 없습니다.");
            response.put("status", 0);

            return response;
        }

        Map response = new HashMap<String, Object>();
        response.put("message", "고객이 발주한 프로젝트 입니다.");
        response.put("status", 1);
        response.put("project_name", project.getProName());
        response.put("project_startdate", project.getStartDate());
        response.put("project_endtdate", project.getEndDate());
        response.put("pm_name", pmName.getEmp_name());
        response.put("client_name", client.getClientName());
        response.put("client_emp_name", client.getClientEmpName());

        return response;
    }

    // 동료평가 및 PM 평가 조회 +++ 고객 평가도 조회
    @Transactional(readOnly = true)
    public Map coworkEvalPersonalRead(Long addId) {
        Optional<AccountEntity> accountEntity = accountDao.findByaccId(addId);
        AccountEntity account = accountEntity.get();
        Long empId = account.getEmployeeEntity().getEmpId();
        EmployeeEntity emp = employeeDao.findByEmpId(empId);

        System.out.println(emp.getEmpName());

        // 참여한 모든 프로젝트의 평가 조회
        List<ParticipantEvaluationResponseSummarizeInterface> allEvalDto = evaluationInnerDao.findAllEvalEmp(emp.getEmpId());
        // 프로젝트별 받은 동료 평가 조회
        List<ParticipantEvaluationResponseInterface> peerEvalDto = evaluationInnerDao.findAllByPeerEvalEmp(emp.getEmpId());
        // 프로젝트별 받은 PM 평가 조회
        List<ParticipantEvaluationResponseInterface> pmEvalDto = evaluationInnerDao.findAllByPmEvalEmp(emp.getEmpId());

        List<ClientEvalResponseInterface> clientEvalDto = evaluationRequestDao.findAllByEmpName(emp.getEmpId());

        Map response = new HashMap<String, Object>();
        response.put("all_eval", allEvalDto);
        response.put("cowork_eval", peerEvalDto);
        response.put("pm_eval", pmEvalDto);
        response.put("client_eval", clientEvalDto);

        return response;
    }

    // 직원별 평가 조회
    @Transactional(readOnly = true)
    public List<ParticipantEvaluationResponseInterface>  coworkEvalEmployeeRead(Long empId) {
        EmployeeEntity emp = employeeDao.findByEmpId(empId);

        // 프로젝트별 받은 평가 조회
        List<ParticipantEvaluationResponseInterface> dto = evaluationInnerDao.findAllByPeerEvalEmp(emp.getEmpId());

        return dto;
    }

//    @Transactional(readOnly = true)
//    // 고객평가 조회 (프로젝트 아이디를 통해)
//    public ClientEvaluationResponseDto clientEvalRead(Long addId) {
//        Optional<AccountEntity> accountEntity = accountDao.findByaccId(addId);
//        AccountEntity account = accountEntity.get();
//        Long empId = account.getEmployeeEntity().getEmpId();
//        EmployeeEntity emp = employeeDao.findByEmpId(empId);
//
//        // 프로젝트별 받은 평가 조회
//        return ClientEvaluationResponseDto.from(emp);
//    }

//    @Transactional(readOnly = true)
//    // 고객평가 조회 (고객이 조회)
//    public ClientEvaluationResponseDto clientEvalRead(Long addId) {
//        // 고객 ID (고객 사번)
//        Optional<AccountEntity> accountEntity = accountDao.findByaccId(addId);
//        AccountEntity account = accountEntity.get();
//        String clientName = account.getEmployeeEntity().getEmpName();
//
//        // 고객 정보
//        ClientInfoEntity client = clientInfoDao.findByClientEmpName(clientName);
////        if (client == null) {
////            Map response = new HashMap<String, Object>();
////            response.put("message", "관련 고객 정보가 없습니다.");
////            response.put("status", 0);
////            response.put("clientName", clientName);
////
////            return response;
////        }
//
//        // 프로젝트별 받은 고객 평가 조회
//        return ClientEvaluationResponseDto.from(client);
//    }
}
