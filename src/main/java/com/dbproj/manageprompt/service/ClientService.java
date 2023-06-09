package com.dbproj.manageprompt.service;

import com.dbproj.manageprompt.common.exception.NotFoundException;
import com.dbproj.manageprompt.dao.ClientInfoDao;
import com.dbproj.manageprompt.dto.ClientResponseDto;
import com.dbproj.manageprompt.dto.ClientUpdateRequestDto;
import com.dbproj.manageprompt.entity.ClientInfoEntity;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class ClientService {

    private final ClientInfoDao clientInfoDao;

    // 발주처 상세 조회
    @Transactional(readOnly = true)
    public ClientResponseDto findOne(long clientId) {
        ClientInfoEntity client = findClient(clientId);
        return ClientResponseDto.from(client);
    }

    // findClient method
    @Transactional(readOnly = true)
    public ClientInfoEntity findClient(long clientId) {
        ClientInfoEntity client = clientInfoDao.findById(clientId).orElseThrow(() ->
                new IllegalArgumentException("해당 발주처는 존재하지 않습니다. => " + clientId));
        return client;
    }

    // 발주처 담당자 정보 수정
    public Map update(long clientId, ClientUpdateRequestDto requestDto) {
        ClientInfoEntity updateClient = clientInfoDao.findById(clientId).orElseThrow(NotFoundException::new);
        updateClient.update(
                requestDto.getClient_emp_name(),
                requestDto.getClient_emp_ph(),
                requestDto.getClient_emp_email()
        );

        Map response = new HashMap<String, Object>();
        response.put("message", "발주처 담당자 정보가 수정되었습니다.");
        response.put("status", 1);
        response.put("client_id", clientInfoDao.save(updateClient).getClientId());

        return response;
    }
}
