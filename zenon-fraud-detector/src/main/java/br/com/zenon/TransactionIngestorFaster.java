package br.com.zenon;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class TransactionIngestorFaster {

  private static final int DEFAULT_PARALLELISM = 4;
  public static List<Transaction> readBufferedReader(String fileName, boolean skipHeader, int lineLimit) {
    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
      if (skipHeader) {
        reader.readLine();
      }
      return reader.lines()
        .limit(lineLimit)
        .map(line -> line.split(",", -1))
        .map(TransactionIngestorFaster::parseTransaction)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
    } catch (Exception e) {
      throw new RuntimeException("Erro ao ler o arquivo: " + fileName, e);
    }
  }

  public static void readAsBatch(String fileName, boolean skipHeader, int lineLimit, int batchLimit,
      Consumer<List<Transaction>> batchConsumer) {
    readAsBatch(fileName, skipHeader, lineLimit, batchLimit, DEFAULT_PARALLELISM, batchConsumer);
  }

  public static void readAsBatch(String fileName, boolean skipHeader, int lineLimit, int batchLimit,
      int parallelism, Consumer<List<Transaction>> batchConsumer) {
    Path path = Path.of(fileName);
    ExecutorService executor = Executors.newFixedThreadPool(parallelism);
    List<Future<?>> futures = new ArrayList<>();

    try (Stream<String> lines = Files.lines(path).skip(skipHeader ? 1 : 0).limit(lineLimit)) {
      int count = 0;
      var iterator = lines.iterator();
      List<Transaction> lineBatch = new ArrayList<>(batchLimit);

      while (iterator.hasNext()) {
        String line = iterator.next();
        String[] chunks = line.split(",", -1);
        Optional<Transaction> transaction = parseTransaction(chunks);
        if (transaction.isPresent()) {
          lineBatch.add(transaction.get());
          if (lineBatch.size() >= batchLimit) {
            final List<Transaction> currentLineBatch = List.copyOf(lineBatch);
            futures.add(executor.submit(() -> batchConsumer.accept(currentLineBatch)));
            lineBatch.clear();
          }
        }
        count++;
        if (count >= lineLimit) {
          break;
        }
      }
      if (!lineBatch.isEmpty()) {
        final List<Transaction> currentLineBatch = List.copyOf(lineBatch);
        futures.add(executor.submit(() -> batchConsumer.accept(currentLineBatch)));
      }
    } catch (Exception e) {
      executor.shutdownNow();
      throw new RuntimeException("Erro ao ler o arquivo: " + fileName, e);
    } finally {
      awaitBatchPersistence(executor, futures);
    }
  }

  private static void awaitBatchPersistence(ExecutorService executor, List<Future<?>> futures) {
    executor.shutdown();
    try {
      if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
        executor.shutdownNow();
        throw new RuntimeException("Timeout ao aguardar persistência dos lotes");
      }
      for (Future<?> future : futures) {
        future.get();
      }
    } catch (ExecutionException e) {
      throw new RuntimeException("Erro ao persistir lote", e.getCause());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      executor.shutdownNow();
      throw new RuntimeException("Ingestão interrompida", e);
    }
  }

  public static void readAsStream( String fileName, boolean skipHeader, int lineLimit, Consumer<Transaction> consumer) {
    Path path = Path.of(fileName);
    try (Stream<String> lines = Files.lines(path)) {
      lines.skip(skipHeader ? 1 : 0)
        .limit(lineLimit)
        .map(line -> line.split(",", -1))
        .map(TransactionIngestorFaster::parseTransaction)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(consumer);

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
