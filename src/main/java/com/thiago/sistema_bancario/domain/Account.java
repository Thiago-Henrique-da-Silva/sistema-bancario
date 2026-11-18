package com.thiago.sistema_bancario.domain;

import com.thiago.sistema_bancario.exception.BusinessException;
import com.thiago.sistema_bancario.exception.InvalidInputExeption;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, updatable = false, length = 6)
    private String number;
    @Column(nullable = false)
    private BigDecimal balance;
    @Column(nullable = false)
    private BigDecimal overdraftLimit;
    @Column(nullable = false)
    private BigDecimal  overdraftUsed;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusAccount status;
    @JoinColumn(nullable = false)
    @ManyToOne
    private Client client;

    public Account(Client client) {
        Random random = new Random();
        int numberAccount = random.nextInt(900_000) + 100_000;
        String numberString = String.format("%06d", numberAccount);

        this.number = numberString;
        this.balance = BigDecimal.ZERO;
        this.overdraftLimit = client.getMonthlyIncome().divide(BigDecimal.valueOf(5));
        this.overdraftUsed = BigDecimal.ZERO;
        this.status = StatusAccount.ACTIVE;
        this.client = client;
    }

    private static final BigDecimal OVERDRAFT_FEE = new BigDecimal("0.10");                // taxa de 10%
    private static final BigDecimal OVERDRAFT_MULTIPLIER = BigDecimal.ONE.add(OVERDRAFT_FEE);   // 1.10

    public void withdrawal(BigDecimal value) {

        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidInputExeption("valor inserido negativo ou invalido.");
        }

        if (status == StatusAccount.BLOCKED || status == StatusAccount.CLOSED) {
            throw new BusinessException("Conta bloqueada ou cancelada.");
        }

        if (value.compareTo(balance.add(getOverdraftAvailable())) > 0) {
            throw new BusinessException("Saldo insuficiente.");
        }

        if (value.compareTo(balance) <= 0) {
            balance = balance.subtract(value);
        } else {
            overdraftUsed = overdraftUsed.add(value.subtract(balance));
            balance = BigDecimal.ZERO;
        }
    }

    public BigDecimal deposit(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidInputExeption("Insira um valor válido para depositar.");
        }

        if (status == StatusAccount.BLOCKED || status == StatusAccount.CLOSED) {
            throw new BusinessException("Conta bloqueada ou cancelada.");
        }

        if (!isUsingOverdraft()) {
            balance = balance.add(value);
            return BigDecimal.ZERO;
        }

        BigDecimal totalDebt = getOverdraftWithInterestRate();

        if (value.compareTo(totalDebt) >= 0) {
            BigDecimal overdraftFee = totalDebt.subtract(overdraftUsed);
            balance = balance.add(value.subtract(totalDebt));
            overdraftUsed = BigDecimal.ZERO;
            return overdraftFee;
        }

        BigDecimal paidAmount = value.divide(OVERDRAFT_MULTIPLIER, 2, RoundingMode.HALF_UP);
        overdraftUsed = overdraftUsed.subtract(paidAmount);
        return value.subtract(paidAmount);
    }

    public BigDecimal getOverdraftWithInterestRate() {
        return overdraftUsed.multiply(OVERDRAFT_MULTIPLIER).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isUsingOverdraft() {
        return overdraftUsed.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal getOverdraftAvailable() {
        return overdraftLimit.subtract(overdraftUsed);
    }
}
