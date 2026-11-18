package com.thiago.sistema_bancario.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Extract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeExtract typeExtract;
    private @Column(nullable = false)
    BigDecimal value;
    @Column(nullable = false)
    private LocalDateTime dateTime;
    @JoinColumn(nullable = false)
    @ManyToOne
    private Account account;

    public Extract(TypeExtract typeExtract, BigDecimal value, LocalDateTime dateTime, Account account) {
        this.typeExtract = typeExtract;
        this.value = value;
        this.dateTime = dateTime;
        this.account = account;
    }

}
