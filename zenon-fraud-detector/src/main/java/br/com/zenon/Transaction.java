package br.com.zenon;

import java.math.BigDecimal;
import java.util.Objects;

public record Transaction(int step, TransactionType type, BigDecimal amount, TransactionCustomer origin, TransactionCustomer recipient, boolean isFraud,
    boolean isFlaggedFraud) {
        public Transaction {
            Objects.requireNonNull(type, "O campo type não pode ser nulo");
            Objects.requireNonNull(amount, "O campo amount não pode ser nulo");
            Objects.requireNonNull(origin, "O campo origin não pode ser nulo");
            Objects.requireNonNull(recipient, "O campo recipient não pode ser nulo");

            if(step <= 0) {
                throw new IllegalArgumentException("O campo step deve ser maior que 0");
            }
            if(amount.signum() < 0 ) {
                throw new IllegalArgumentException("O campo amount não pode ser nulo");
            }
        }
}
