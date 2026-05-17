package br.com.zenon;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TransactionIngestor {
  public static List<Transaction> readBufferedReader(String fileName, boolean skipHeader, int lineLimit) {
    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
      if (skipHeader) {
        reader.readLine();
      }
      return reader.lines()
        .limit(lineLimit)
        .map(line -> line.split(",", -1))
        .map(TransactionIngestor::parseTransaction)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
    } catch (Exception e) {
      throw new RuntimeException("Erro ao ler o arquivo: " + fileName, e);
    }
  }

  public static List<Transaction> read( String fileName, boolean skipHeader, int lineLimit) {
    Path path = Path.of(fileName);
    try {
      List<String> lines = Files.readAllLines(path);
      return lines.stream()
        .skip(skipHeader ? 1 : 0)
        .limit(lineLimit)
        .map(line -> line.split(",", -1))
        .map(TransactionIngestor::parseTransaction)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();

    } catch (Exception e) {
      throw new RuntimeException("Erro ao ler o arquivo: " + fileName, e);
    }
  }

  private static Optional<Transaction> parseTransaction(String[] chunks) {
    try {
      int step = Integer.parseInt(chunks[0]);
      TransactionType type = TransactionType.valueOf(chunks[1]);
      if(chunks[2].isEmpty() || chunks[2].trim().isBlank()) {
        System.err.println("amount está vazio");
      }
      BigDecimal amount = new BigDecimal(chunks[2]);
      String originName = chunks[3];
      if(chunks[4].isEmpty() || chunks[4].trim().isBlank()) {
        System.err.println("originOldBalance está vazio");
      }
      BigDecimal originOldBalance = new BigDecimal(chunks[4]);
      if(chunks[5].isEmpty() || chunks[5].trim().isBlank()) {
        System.err.println("originNewBalance está vazio");
      }
      BigDecimal originNewBalance = new BigDecimal(chunks[5]);
      String recipientName = chunks[6];
      if(chunks[7].isEmpty() || chunks[7].trim().isBlank()) {
        System.err.println("recipientOldBalance está vazio");
      }
      BigDecimal recipientOldBalance = new BigDecimal(chunks[7]);
      if(chunks[8].isEmpty() || chunks[8].trim().isBlank()) {
        System.err.println("recipientNewBalance está vazio");
      }
      BigDecimal recipientNewBalance = new BigDecimal(chunks[8]);
      boolean isFraud = "1".equals(chunks[9]);
      boolean isFlaggedFraud = "1".equals(chunks[10]);
      return Optional.of(new Transaction(step, type, amount, new TransactionCustomer(originName, originOldBalance, originNewBalance), new TransactionCustomer(recipientName, recipientOldBalance, recipientNewBalance), isFraud, isFlaggedFraud));
    } catch (Exception e) {
      System.err.println("Erro ao parsear a transação: " + Arrays.toString(chunks) + " - exeção:" + e);
      return Optional.empty();
    }
  }
  
}
