package br.com.zenon;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class TransactionReport {
  private record ReportTransaction(BigDecimal amount, boolean isFraud) {    
  }

  public record Statistics(long totalTransactions, long totalFrauds, BigDecimal totalAmount) {}

  public Statistics generateReport(String fileName) {
    try (Stream<String> lines = Files.lines(Path.of(fileName))) {

      var reportTransactions = lines
        .skip(1)
        .map(line -> line.split(",", -1))
        .map(TransactionReport::parseReportTransaction)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList()
      ;

      return new Statistics(
        reportTransactions.size(),
        reportTransactions.stream()
                          .filter(ReportTransaction::isFraud)
                          .count(),
        reportTransactions.stream()
                          .map(ReportTransaction::amount)
                          .reduce(BigDecimal.ZERO, BigDecimal::add));

    } catch (Exception e) {
      throw new RuntimeException("Erro ao gerar o relatório: " + e);
    }
  }

  private static Optional<ReportTransaction> parseReportTransaction(String[] chunks) {
    try {
      BigDecimal amount = new BigDecimal(chunks[2]);
      boolean isFraud = "1".equals(chunks[9]);
      return Optional.of(new ReportTransaction(amount, isFraud));
    } catch (Exception e) {
      System.err.println("Erro ao parsear a transação: " + Arrays.toString(chunks) + " - exeção:" + e);
      return Optional.empty();
    }
  }
}
