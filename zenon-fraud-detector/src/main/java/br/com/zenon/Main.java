package br.com.zenon;

//import java.math.BigDecimal;

public class Main {
    void main(String[] args) {
        /*
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

        var transaction2 = new Transaction(743,
            TransactionType.CASH_OUT,
            new BigDecimal("850002.52"),
        new TransactionCustomer("C1280323807",
        new BigDecimal("850002.52"),
        new BigDecimal("0.0")),
        new TransactionCustomer("C873221189",
        new BigDecimal("6510099.11"),
        new BigDecimal("7360101.63")),
        true,
        false);

        
        IO.println(transaction1);
        IO.println(transaction2);
        IO.println("--------------------------------");
        IO.println("Lendo arquivo de transações...");
        IO.println(TransactionIngestor.read("zenon-fraud-detector/data/PS_20174392719_1491204439457_log.csv", true, 10));
        IO.println("--------------------------------");
        IO.println("Lendo arquivo de transações com erros...");
        var transactions = TransactionIngestor.read("zenon-fraud-detector/data/paysim_with_bad_data.csv", true, 16);
        IO.println("Quantidade de transações: " + transactions.size());
        transactions.forEach(IO::println);
        */
        var transactions = TransactionIngestor.read("zenon-fraud-detector/data/PS_20174392719_1491204439457_log.csv", true, 50000);
        var fraudTransactions = FraudAnalyzer.analyze(transactions);

        IO.println("1. Total de Fraudes: " + FraudAnalyzer.getCountOfFrauds(fraudTransactions));

        IO.println("2. Top 3 Fraudes de maior valor amount: ");
        var top3FraudesAmount = FraudAnalyzer.getTopFraudes(fraudTransactions, 3).stream().map(Transaction::amount).toList();
        top3FraudesAmount.forEach(amount -> IO.println("%.2f".formatted(amount)));

        IO.println("3. Top 5 Clientes Suspeitos: ");
        var top5SuspiciousCustomers = FraudAnalyzer.getTopSuspiciousCustomers(fraudTransactions, 5).stream().map(Transaction::origin).toList();
        top5SuspiciousCustomers.forEach(customer -> IO.println(customer.name()));
        
        IO.println("4. Prejuízo total: " + FraudAnalyzer.getTotalLoss(fraudTransactions));

        IO.println("5. Fraudes por tipo: ");
        var fraudsByType = FraudAnalyzer.getFraudsByType(fraudTransactions);
        fraudsByType.forEach((type, transactionsType) -> IO.println(" - " + type + ": " + transactionsType.size()));
    }
}