package br.com.zenon;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TransactionMapRepository implements TransactionRepository {

  private final Map<String, Transaction> transactions;

  public TransactionMapRepository(List<Transaction> transactions) {
    Objects.requireNonNull(transactions, "Transactions cannot be null");
    this.transactions = transactions.stream()
      .collect(
        Collectors.toMap(
          transaction -> transaction.origin().name(),
          Function.identity(),
          (existing, replacement) -> existing
        )
      );
  }
  
  @Override
  public Optional<Transaction> findByOriginName(String originName) {
    return Optional.ofNullable(transactions.get(originName));
  }

  @Override
  public void save(Transaction transaction) {
    this.transactions.computeIfAbsent(transaction.origin().name(), key -> transaction);
  }
}
