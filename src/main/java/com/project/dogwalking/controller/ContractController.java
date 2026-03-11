package com.project.dogwalking.controller;

import com.project.dogwalking.dto.ContractResponseDto;
import com.project.dogwalking.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @GetMapping("/{id}")
    public ResponseEntity<ContractResponseDto> getContractById(@PathVariable Long id) {
        ContractResponseDto contract = contractService.getContractById(id);
        return ResponseEntity.ok(contract);
    }

    @GetMapping(params = "walkerId")
    public ResponseEntity<List<ContractResponseDto>> getContractsByWalker(@RequestParam Long walkerId) {
        List<ContractResponseDto> contracts = contractService.getContractsByWalker(walkerId);
        return ResponseEntity.ok(contracts);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ContractResponseDto> getContractByOrderId(@PathVariable Long orderId) {
        ContractResponseDto contract = contractService.getContractByOrderId(orderId);
        return ResponseEntity.ok(contract);
    }
}