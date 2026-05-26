package br.com.zenon;

import java.util.List;

public class DbMain {
  void main() {
    var connection = ConnectionFactory.getConnection();
    IO.println("Conexão estabelecida com sucesso: " + connection);

    var transaction = new TransactionSQLRepository().findByOriginName("C1000001");
    transaction.ifPresentOrElse(IO::println, () -> IO.println("Transaction não encontrada"));

    /* salvar transação
    var transaction1 = new Transaction(1,
      TransactionType.PAYMENT,
      new BigDecimal("9839.64"),
      new TransactionCustomer("C1231006815",
      new BigDecimal("170136.0"),
      new BigDecimal("160296.36")),
      new TransactionCustomer("M1979787155",
      new BigDecimal("0.00"),
      new BigDecimal("0.00")),
      false,
      false);

    new TransactionSQLRepository().save(transaction1);
    */
    long startTimeSQL = System.currentTimeMillis();
    
    List<Transaction> transactions = TransactionIngestor.read("zenon-fraud-detector/data/PS_20174392719_1491204439457_log.csv", true, 10000);
    IO.println("Quantidade de transações: " + transactions.size());

    IO.println("Salvando transações...");
    TransactionRepository transactionRepository = new TransactionSQLRepository();
    transactions.forEach(transactionRepository::save);
    IO.println("Transações salvas com sucesso");

    long endTimeSQL = System.currentTimeMillis();
    System.out.println("Tempo de execução SQL: " + (endTimeSQL - startTimeSQL) + "ms");
    
    var transaction2 = new TransactionSQLRepository().findByOriginName("C1231006815");
    transaction2.ifPresentOrElse(IO::println, () -> IO.println("Transaction não encontrada"));

  }
}
