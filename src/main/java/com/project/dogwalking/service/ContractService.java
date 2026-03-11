package com.project.dogwalking.service;

import com.project.dogwalking.dto.ContractResponseDto;
import com.project.dogwalking.entity.Contract;
import com.project.dogwalking.exception.ResourceNotFoundException;
import com.project.dogwalking.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;

    @Transactional(readOnly = true)
    public ContractResponseDto getContractById(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));
        return mapToDto(contract);
    }

    @Transactional(readOnly = true)
    public List<ContractResponseDto> getContractsByWalker(Long walkerId) {
        return contractRepository.findByWalkerId(walkerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContractResponseDto getContractByOrderId(Long orderId) {
        Contract contract = contractRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found for order id: " + orderId));
        return mapToDto(contract);
    }

    private ContractResponseDto mapToDto(Contract contract) {
        ContractResponseDto dto = new ContractResponseDto();
        dto.setId(contract.getId());
        dto.setOrderId(contract.getOrder().getId());
        dto.setWalkerId(contract.getWalker().getId());
        dto.setWalkerName(contract.getWalker().getUsername());
        dto.setStatus(contract.getStatus().name());
        dto.setPrepaid(contract.getPrepaid());
        dto.setCreatedAt(contract.getCreatedAt());
        dto.setCompletedAt(contract.getCompletedAt());
        return dto;
    }
}