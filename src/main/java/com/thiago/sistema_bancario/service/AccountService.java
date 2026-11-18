package com.thiago.sistema_bancario.service;

import com.thiago.sistema_bancario.Mapper.AccountMapper;
import com.thiago.sistema_bancario.Mapper.ExtractMapper;
import com.thiago.sistema_bancario.Mapper.LoanMapper;
import com.thiago.sistema_bancario.domain.*;
import com.thiago.sistema_bancario.dto.*;

import com.thiago.sistema_bancario.exception.BusinessException;
import com.thiago.sistema_bancario.exception.ResourceNotFoundException;
import com.thiago.sistema_bancario.repository.AccountRepository;
import com.thiago.sistema_bancario.repository.ExtractRepository;
import com.thiago.sistema_bancario.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {
    private final ClientService clientService;
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final ExtractRepository extractRepository;
    private final ExtractMapper extractMapper;
    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;

    @Transactional
    public AccountResponse createAccount(Long id) {
        Client client = clientService.findClientByIdEntity(id);

        if (accountRepository.existsByClientId(id)) {
            throw new BusinessException("Este cliente ja possui uma conta.");
        }

        Account account;
        do {
            account = new Account(client);
        } while (accountRepository.existsByNumber(account.getNumber()));

        accountRepository.save(account);
        return accountMapper.toAccountResponse(account);
    }

    @Transactional
    public List<ExtractResponse> deposit(Long id,
                                                            DepositOrWithdrawalRequest depositOrWithdrawalRequest) {

        Account account = findAccountByIdEntity(id);
        BigDecimal fee = account.deposit(depositOrWithdrawalRequest.value());
        accountRepository.save(account);
        List<ExtractResponse> extracts = new ArrayList<>();

        Extract extract = new Extract(TypeExtract.DEPOSIT, depositOrWithdrawalRequest.value(), LocalDateTime.now(), account);
        extract =  extractRepository.save(extract);
        extracts.add(extractMapper.toExtractResponse(extract));

        if (fee.compareTo(BigDecimal.ZERO) > 0) {
            Extract extractFee = new Extract(TypeExtract.RATE, fee, LocalDateTime.now(), account);
            extractFee = extractRepository.save(extractFee);
            extracts.add(extractMapper.toExtractResponse(extractFee));
        }

        return extracts;
    }

    @Transactional
    public ExtractResponse withdrawal(Long id,DepositOrWithdrawalRequest depositOrWithdrawalRequest) {

        Account account = findAccountByIdEntity(id);
        account.withdrawal(depositOrWithdrawalRequest.value());
        accountRepository.save(account);

        Extract extract = new Extract(TypeExtract.WITHDRAWAL, depositOrWithdrawalRequest.value(),
                LocalDateTime.now(), account);
        extract =  extractRepository.save(extract);

        return extractMapper.toExtractResponse(extract);
    }

    @Transactional
    public List<ExtractResponse> transfer(TransferRequest transferRequest) {

        existsByNumberAccount(transferRequest.originAccountNumber());
        existsByNumberAccount(transferRequest.destinationAccountNumber());
        if(transferRequest.originAccountNumber().equals(transferRequest.destinationAccountNumber())){
            throw new BusinessException("Numero da conta de origem deve ser diferente do destinatário.");
        }

        Account originAccount = accountRepository.findByNumber(transferRequest.originAccountNumber());
        Account destinationAccount = accountRepository.findByNumber(transferRequest.destinationAccountNumber());
        List<ExtractResponse> transferExtracts = new ArrayList<>();

        originAccount.withdrawal(transferRequest.value());
        accountRepository.save(originAccount);
        Extract originExtract = new Extract(TypeExtract.TRANSFER_SENT, transferRequest.value(), LocalDateTime.now(),
                originAccount);
        originExtract = extractRepository.save(originExtract);
        transferExtracts.add(extractMapper.toExtractResponse(originExtract));

        BigDecimal fee = destinationAccount.deposit(transferRequest.value());
        accountRepository.save(destinationAccount);
        Extract destinationExtract = new Extract(TypeExtract.TRANSFER_RECEIVED, transferRequest.value(), LocalDateTime.now(),
                destinationAccount);
        destinationExtract = extractRepository.save(destinationExtract);
        transferExtracts.add(extractMapper.toExtractResponse(destinationExtract));

        if (fee.compareTo(BigDecimal.ZERO) > 0) {
            Extract feeExtract = new Extract(TypeExtract.RATE, fee, LocalDateTime.now(), destinationAccount);
            feeExtract = extractRepository.save(feeExtract);
            transferExtracts.add(extractMapper.toExtractResponse(feeExtract));
        }

        return transferExtracts;
    }

    public Page<ExtractResponse> findAllExtractsAccount(Long idAccount, Pageable pageable) {
        Account account = findAccountByIdEntity(idAccount);
        return extractRepository.findByAccountOrderByDateTimeDesc(account, pageable).map(extractMapper::toExtractResponse);
    }

    @Transactional
    public AccountResponse blockAccount(Long id) {
        Account account = findAccountByIdEntity(id);

        switch (account.getStatus()) {
            case BLOCKED -> throw new BusinessException("Esta conta ja esta bloqueada.");
            case CLOSED -> throw new BusinessException("Esta conta foi cancelada e nao pode ser bloqueada.");
        }

        account.setStatus(StatusAccount.BLOCKED);
        accountRepository.save(account);
        return accountMapper.toAccountResponse(account);
    }

    @Transactional
    public AccountResponse cancelAccount(Long id) {
        Account account = findAccountByIdEntity(id);

        checkAccountToClose(account);

        account.setStatus(StatusAccount.CLOSED);
        accountRepository.save(account);
        return accountMapper.toAccountResponse(account);
    }

    public LoanSimulationResponse simulateLoan(Long id, LoanSimulationRequest loanSimulationRequest) {
        Account account = findAccountByIdEntity(id);

        Loan loan = buildValidatedLoan(account, loanSimulationRequest.requestAmount(),
                loanSimulationRequest.numberInstallments());

        return loanMapper.toLoanSimulationResponse(loan);
    }

    @Transactional
    public  LoanResponse createLoan(Long id, LoanRequest loanRequest) {
        Account account = findAccountByIdEntity(id);

        Loan loan = buildValidatedLoan(account, loanRequest.requestAmount(), loanRequest.numberInstallments());

        loanRepository.save(loan);
        LoanResponse loanResponse = loanMapper.toLoanResponse(loan);
        account.deposit(loan.getRequestAmount());
        accountRepository.save(account);

        Extract extract = new Extract(TypeExtract.LOAN_CREDIT,loanResponse.requestAmount(),
                LocalDateTime.now(),account);
        extractRepository.save(extract);

        return loanResponse;
    }

    @Transactional
    public LoanResponse payLoanInstallment(Long idAccount) {
        Account account = findAccountByIdEntity(idAccount);

        Loan loan = loanRepository.findByAccountAndLoanStatus(account, StatusLoan.ACTIVE)
                        .orElseThrow(() -> new ResourceNotFoundException("emprestimo ativo não encontrado."));

        account.withdrawal(loan.getInstallmentAmount());
        accountRepository.save(account);
        loan.setPaidInstallments(loan.getPaidInstallments() + 1);

        if (loan.getPaidInstallments() == loan.getNumberInstallments()) {
            loan.setLoanStatus(StatusLoan.FINISHED);
        }

        loanRepository.save(loan);

        Extract extract = new Extract(TypeExtract.LOAN_PAYMENT, loan.getInstallmentAmount(), LocalDateTime.now(),
                account);
        extractRepository.save(extract);

        return loanMapper.toLoanResponse(loan);
    }

    public Account findAccountByIdEntity(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta com id " + id + " não encontrada."));
    }

    public AccountResponse findAccountById(Long id) {
        Account account =  findAccountByIdEntity(id);
        return accountMapper.toAccountResponse(account);
    }

    private Loan buildValidatedLoan(Account account, BigDecimal requestAmount, int numberInstallments) {
        checkAccountStatus(account);
        checkActiveLoans(account);

        if (requestAmount.compareTo(totalLoanAmount(account.getClient().getMonthlyIncome())) > 0) {
            throw new BusinessException("valor maximo do emprestimo excedido.");
        }

        Loan loan = new Loan(requestAmount, numberInstallments);
        loan.setAccount(account);

        if (installmentsAboveThirtyPercentOfIncome(loan)) {
            throw new BusinessException("emprestimo recusado,valor das parcelas acima do valor permitido.");
        }

        return loan;
    }

    private void checkAccountToClose(Account account) {
        if (account.getBalance().compareTo(BigDecimal.ZERO) >0) {
            throw new  BusinessException("Não e possivel encerrar uma conta com saldo.");
        }

        if (account.getOverdraftUsed().compareTo(BigDecimal.ZERO) >0) {
            throw new BusinessException("Não e possivel encerrar uma conta com cheque especial em uso.");
        }

        if (loanRepository.existsByAccountAndLoanStatus(account, StatusLoan.ACTIVE)) {
            throw new BusinessException("Não e possivel encerrar uma conta com emprestimo ativo.");
        }

        if (account.getStatus() == StatusAccount.CLOSED) {
            throw  new BusinessException("Esta conta ja esta cancelada.");
        }

    }

    private BigDecimal totalLoanAmount (BigDecimal monthlyIncomeClient) {
        return monthlyIncomeClient.multiply(BigDecimal.TEN);
    }

    private void checkAccountStatus(Account account) {
       switch (account.getStatus()) {
           case BLOCKED -> throw new BusinessException("Esta conta esta bloqueada.");
           case CLOSED ->  throw new BusinessException("esta conta foi cancelada.");
       }
    }

    private void checkActiveLoans(Account account) {
        if (loanRepository.existsByAccountAndLoanStatus(account, StatusLoan.ACTIVE)) {
            throw new BusinessException("Esta conta ja possui um emprestimo ativo.");
        }
    }

    private boolean installmentsAboveThirtyPercentOfIncome(Loan loan) {
        BigDecimal limit = loan.getAccount().getClient().getMonthlyIncome().multiply(new BigDecimal("0.30"));

        return loan.installmentAmount().compareTo(limit) > 0;
    }

    public void existsByNumberAccount(String numberAccount) {
        if (!accountRepository.existsByNumber(numberAccount)) {
            throw new ResourceNotFoundException("conta com o numero " + numberAccount + " não encontrada");
        }
    }







}
