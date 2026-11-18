package com.thiago.sistema_bancario.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private BigDecimal requestAmount;
    @Column(nullable = false)
    private BigDecimal interestRate;
    @Column(nullable = false)
    private int numberInstallments;
    @Column(nullable = false)
    private BigDecimal totalValue;
    @Column(nullable = false)
    private BigDecimal installmentAmount;
    @Column(nullable = false)
    private int paidInstallments;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusLoan loanStatus;
    @JoinColumn(nullable = false)
    @ManyToOne
    private Account account;
    
    public Loan(BigDecimal requestAmount, int numberInstallments) {
        this.requestAmount = requestAmount;
        this.numberInstallments = numberInstallments;
        this.interestRate = installmentFee(numberInstallments);
        this.totalValue = totalValue();
        this.installmentAmount = installmentAmount();
        this.paidInstallments = 0;
        this.loanStatus = StatusLoan.ACTIVE;
    }


    private BigDecimal installmentFee(int numberInstallments) {
        if (numberInstallments <= 6) {
            return new BigDecimal("0.02");
        } else if (numberInstallments <= 12) {
            return new BigDecimal("0.03");
        } else {
            return new BigDecimal("0.04");
        }
    }

    private BigDecimal totalValue() {
        BigDecimal fee = requestAmount
                .multiply(interestRate)
                .multiply(BigDecimal.valueOf(numberInstallments));

        return requestAmount.add(fee);
    }

    public BigDecimal installmentAmount() {
        return totalValue.divide(BigDecimal.valueOf(numberInstallments),2, RoundingMode.HALF_UP);
    }
}



