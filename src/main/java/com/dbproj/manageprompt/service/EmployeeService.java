package com.dbproj.manageprompt.service;

import com.dbproj.manageprompt.Interface.WapperInterface;
import com.dbproj.manageprompt.Interface.WrapperInterface;
import com.dbproj.manageprompt.common.exception.NotFoundException;
import com.dbproj.manageprompt.dao.AccountDao;
import com.dbproj.manageprompt.dao.EmployeeDao;
import com.dbproj.manageprompt.dto.*;
import com.dbproj.manageprompt.entity.AccountEntity;
import com.dbproj.manageprompt.specification.EmployeeSpecification;
import com.dbproj.manageprompt.entity.EmployeeEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class EmployeeService {

    private final EmployeeDao employeeDao;
    private final AccountDao accountDao;


    // 직원 검색 Service
    // 직무, 프로젝트 이름, 스킬이름, 프로젝트 참여 여부로 직원을 검색
    public List<List> getProjectEmployeeSearch(ProjectEmployeeSearchDto projectEmployeeSearchDto){
        String a = "asd123";
        Specification<EmployeeEntity> spec = (root, query, criteriaBuilder) -> null;
        if(projectEmployeeSearchDto.getPeriod_start() != null) {
            spec = spec.and(EmployeeSpecification.afterDate(projectEmployeeSearchDto.getPeriod_start()));
        }
        if(projectEmployeeSearchDto.getPeriod_end() != null) {
            spec = spec.and(EmployeeSpecification.beforeDate(projectEmployeeSearchDto.getPeriod_end()));
        }
        if(projectEmployeeSearchDto.getRole() != null)
            spec = spec.and(EmployeeSpecification.equalRole(projectEmployeeSearchDto.getRole()));
        //if(!projectEmployeeSearchDto.getPro_id().equals(""))
        //    spec = spec.and(EmployeeSpecification.equalProId(projectEmployeeSearchDto.getPro_id()));
        if(!projectEmployeeSearchDto.getSkill_name().equals(""))
            spec = spec.and(EmployeeSpecification.equalSkill(projectEmployeeSearchDto.getSkill_name()));
//        if(projectEmployeeSearchDto.getIs_work() == 1) {
//            Date date = new Date();
//            spec = spec.and(EmployeeSpecification.isWork(date));
//        }
        if(
            projectEmployeeSearchDto.getPeriod_start() == null &&
            projectEmployeeSearchDto.getPeriod_end() == null &&
            projectEmployeeSearchDto.getRole() == null &&
            //projectEmployeeSearchDto.getPro_id().equals("") &&
            projectEmployeeSearchDto.getSkill_name().equals("")
        ) {
            spec = spec.and(EmployeeSpecification.all(a));
        }
        List<EmployeeEntity> e =  employeeDao.findAll(spec);
        List<List> result = new ArrayList<>();

        if(projectEmployeeSearchDto.getIs_work() == 1) {
            for (EmployeeEntity ee : e) {
                List<WapperInterface> new_list = new ArrayList<>();
                List<WrapperInterface> count_list = new ArrayList<>();
                new_list =  employeeDao.findByQuery3(ee.getEmpId());
                if (!new_list.isEmpty()) {
                    count_list = employeeDao.findByQuery2(new_list.get(0).getEmp_id());
                    result.add(new_list);
                    result.add(count_list);
                }

            }
        } else {
            for (EmployeeEntity ee : e) {
                List<WapperInterface> new_list = new ArrayList<>();
                List<WrapperInterface> count_list = new ArrayList<>();
                new_list =  employeeDao.findByQuery(ee.getEmpId());
                count_list = employeeDao.findByQuery2(ee.getEmpId());
                result.add(new_list);
                result.add(count_list);
            }
        }


        return result;
        //return 할 정보는???
        //사번, 이름, 주민등록번호, 이메일, 학력, 경력, 스킬,
        //프로젝트id, 직무, 평점, 참여중인 프로젝트 개수
        //참여중인 프로젝트 수 = 참여 여부 판단 후 참여 중인 프로젝트 개수 count
        //평가점수 평균 = 직원 id로 join 해서 평균 구하기

    }
    public List<List> getProjEmployee(){
        String a = "asd123";
        Specification<EmployeeEntity> spec = (root, query, criteriaBuilder) -> null;
        spec = spec.and(EmployeeSpecification.all(a));
        List<EmployeeEntity> ee =  employeeDao.findAll(spec);
        List<List> result = new ArrayList<>();
        for (EmployeeEntity eee : ee) {
            List<WapperInterface> new_list = new ArrayList<>();
            List<WrapperInterface> count_list = new ArrayList<>();
            new_list =  employeeDao.findByQuery(eee.getEmpId());
            count_list = employeeDao.findByQuery2(eee.getEmpId());
            result.add(new_list);
            result.add(count_list);
        }
        return result;
    }

    public List<EmployeeEntity> getEmployeeSearch(String empId, String empName, String empSkill) {
        if(empId != "")
            return employeeDao.findByEmpIdContaining(empId);
        if(empName != "")
            return employeeDao.findByEmpNameContaining(empName);
        if(empSkill != "")
            return employeeDao.findByEmpSkillContaining(empSkill);
        if(empId == "" && empName == "" && empSkill == ""){
            log.info("여기 들어왔음");
           return employeeDao.findAllAsc();
        }
        return employeeDao.findAllAsc();
    }

    // 직원 정보 수정 (개인)
    public Map update(Long emp_id, EmployeeUpdateRequestDto requestDto) {
        EmployeeEntity updateEmployee = employeeDao.findById(emp_id).orElseThrow(NotFoundException::new);

        updateEmployee.update(
                requestDto.getEmp_ph(),
                requestDto.getEmp_email()
        );

        Map response = new HashMap<String, Object>();
        response.put("message", "직원 정보가 수정되었습니다.");
        response.put("status", 1);
        response.put("emp_id", employeeDao.save(updateEmployee).getEmpId());

        return response;
    }

    // 직원별 참여 프로젝트 조회 (개인)
    @Transactional(readOnly = true)
    public ProjectEmployeeResponseDto participantProjectAllRead(Long addId) {
        Optional<AccountEntity> accountEntity = accountDao.findByaccId(addId);
        AccountEntity account = accountEntity.get();
        Long empId = account.getEmployeeEntity().getEmpId();
        EmployeeEntity emp = employeeDao.findByEmpId(empId);

        // 프로젝트별 받은 평가 조회
        return ProjectEmployeeResponseDto.from(emp);
    }

    @Transactional(readOnly = true)
    public List<EmployeeEntity> findByEmpId(String keyword) {
        return employeeDao.findByEmpIdContaining(keyword);
    }
    // searchOption : 이름
    @Transactional(readOnly = true)
    public List<EmployeeEntity> findByEmpName(String keyword) {
        return employeeDao.findByEmpNameContaining(keyword);
    }
    // searchOption : 스킬
    @Transactional(readOnly = true)
    public List<EmployeeEntity> findByEmpSkill(String keyword) {
        return employeeDao.findByEmpSkillContaining(keyword);
    }


    public boolean checkEmpIdDuplicate(Long emp_id) {
        return employeeDao.existsByEmpId(emp_id);
    }

    public boolean checkEmpEmailDuplicate(String emp_email) {
        return employeeDao.existsByEmpEmail(emp_email);
    }
}
