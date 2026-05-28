package br.com.zenon;

public class DbMainFaster {
  void main() {
    long startTimeSQL = System.currentTimeMillis();
    
    TransactionIngestorFaster.readAsBatch(
      "zenon-fraud-detector/data/PS_20174392719_1491204439457_log.csv",
      true,
      6_300_000,
      10_000,
      8,
      new TransactionSQLRepository()::saveAll
    );

    IO.println("Transações salvas com sucesso");

    long endTimeSQL = System.currentTimeMillis();
    System.out.println("Tempo de execução SQL: " + (endTimeSQL - startTimeSQL) + "ms");
    
    var transaction2 = new TransactionSQLRepository().findByOriginName("C1231006815");
    transaction2.ifPresentOrElse(IO::println, () -> IO.println("Transaction não encontrada"));

  }
}
