package br.com.zenon;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FraudAnalyzer {
  public static List<Transaction> analyze(List<Transaction> transactions) {
    return transactions.stream()
      .filter(Transaction::isFraud)
      .toList();
  }

  public static int getCountOfFrauds(List<Transaction> transactions) {
    return (int) transactions.stream()
      .filter(Transaction::isFraud)
      .count();
  }

  public static List<Transaction> getTopFraudes(List<Transaction> transactions, int limit) {
    return transactions.stream()
      .sorted(Comparator.comparing(Transaction::amount).reversed())
      .limit(limit)
      .toList();
  }

  public static List<Transaction> getTopSuspiciousCustomers(List<Transaction> transactions, int limit) {
    return transactions.stream()
      .sorted(Comparator.comparing(Transaction::amount).reversed())
      .limit(limit)
      .toList();
  }

  public static BigDecimal getTotalLoss(List<Transaction> transactions) {
    return transactions.stream()
      .map(Transaction::amount)
      .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public static Map<TransactionType, List<Transaction>> getFraudsByType(List<Transaction> transactions) {
    return transactions.stream()
      .collect(Collectors.groupingBy(Transaction::type));
  }
}
