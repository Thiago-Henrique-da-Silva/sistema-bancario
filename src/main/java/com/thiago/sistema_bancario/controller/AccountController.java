package com.thiago.sistema_bancario.controller;

import com.thiago.sistema_bancario.dto.*;
import com.thiago.sistema_bancario.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/{id}")
    public ResponseEntity<AccountResponse> createAccount(@PathVariable Long id) {
        return new ResponseEntity<>(accountService.createAccount(id), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<List<ExtractResponse>> deposit(
            @PathVariable Long id, @RequestBody @Valid DepositOrWithdrawalRequest depositOrWithdrawalRequest) {
        return new ResponseEntity<>(accountService.deposit(id, depositOrWithdrawalRequest), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/withdrawal")
    public ResponseEntity<ExtractResponse> withdrawal(
            @PathVariable Long id, @RequestBody @Valid DepositOrWithdrawalRequest depositOrWithdrawalRequest) {
        return new ResponseEntity<>(accountService.withdrawal(id, depositOrWithdrawalRequest), HttpStatus.CREATED);
    }

    @PostMapping("/transfers")
    public ResponseEntity<List<ExtractResponse>> transfers(
            @RequestBody @Valid TransferRequest transferRequest) {
        return new ResponseEntity<>(accountService.transfer(transferRequest), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/loans/simulation")
    public ResponseEntity<LoanSimulationResponse> simulateLoan(
            @PathVariable Long id, @RequestBody @Valid LoanSimulationRequest loanSimulationRequest) {
        return ResponseEntity.ok(accountService.simulateLoan(id, loanSimulationRequest));
    }

    @PostMapping("/{id}/loans")
    public ResponseEntity<LoanResponse> createLoan(@PathVariable Long id,
                                                   @RequestBody @Valid LoanRequest loanRequest) {
        return new ResponseEntity<>(accountService.createLoan(id, loanRequest), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> findAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.findAccountById(id));
    }

    @GetMapping("/{id}/extracts")
    public ResponseEntity<Page<ExtractResponse>> findAllExtractsAccount(@PathVariable Long id, Pageable pageable) {
        return ResponseEntity.ok(accountService.findAllExtractsAccount(id, pageable));
    }

    @PatchMapping("/{id}/block")
    public ResponseEntity<AccountResponse> blockAccount(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.blockAccount(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AccountResponse> cancelAccount(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.cancelAccount(id));
    }

    @PatchMapping("/{id}/loans/payinstallment")
    public ResponseEntity<LoanResponse> payInstallment(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.payLoanInstallment(id));
    }
}
