package br.com.zenon;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
        .toList();

    } catch (Exception e) {
      throw new RuntimeException("Erro ao ler o arquivo: " + fileName, e);
    }
  }

  private static Transaction parseTransaction(String[] chunks) {
    int step = Integer.parseInt(chunks[0]);
    TransactionType type = TransactionType.valueOf(chunks[1]);
    BigDecimal amount = new BigDecimal(chunks[2]);
    String originName = chunks[3];
    BigDecimal originOldBalance = new BigDecimal(chunks[4]);
    BigDecimal originNewBalance = new BigDecimal(chunks[5]);
    String recipientName = chunks[6];
    BigDecimal recipientOldBalance = new BigDecimal(chunks[7]);
    BigDecimal recipientNewBalance = new BigDecimal(chunks[8]);
    boolean isFraud = "1".equals(chunks[9]);
    boolean isFlaggedFraud = "1".equals(chunks[10]);
    return new Transaction(step, type, amount, new TransactionCustomer(originName, originOldBalance, originNewBalance), new TransactionCustomer(recipientName, recipientOldBalance, recipientNewBalance), isFraud, isFlaggedFraud);
  }
  
}
