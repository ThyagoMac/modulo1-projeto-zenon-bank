package br.com.zenon;

import java.math.BigDecimal;
import java.util.Objects;

public record TransactionCustomer(String name, BigDecimal oldBalance, BigDecimal newBalance) {
  public TransactionCustomer {
    Objects.requireNonNull(name, "O campo name não pode ser nulo");
    Objects.requireNonNull(oldBalance, "O campo oldBalance não pode ser nulo");
    Objects.requireNonNull(newBalance, "O campo newBalance não pode ser nulo");

    if(name.isEmpty() || name.trim().isBlank()) {
      throw new IllegalArgumentException("O campo name não pode ser vazio");
    }
    if(oldBalance.signum() < 0) {
      throw new IllegalArgumentException("O campo oldBalance não pode ser negativo");
    }
    if(newBalance.signum() < 0) {
      throw new IllegalArgumentException("O campo newBalance não pode ser negativo");
    }
  }
}
