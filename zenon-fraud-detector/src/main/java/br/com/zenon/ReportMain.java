package br.com.zenon;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class ReportMain {
  void main() {
    IO.println("Escolha o idioma / Choose language:");
    IO.println("  pt - Português");
    IO.println("  en - English");
    String choice;
    do {
      choice = IO.readln("Digite pt ou en: ").trim().toLowerCase();
    } while (!choice.equals("pt") && !choice.equals("en"));

    var report = new TransactionReport().generateReport("zenon-fraud-detector/data/PS_20174392719_1491204439457_log.csv");

    Locale locale = switch (choice) {
      case "pt" -> Locale.of("pt", "US");
      case "en" -> Locale.of("en", "US");
      default -> throw new IllegalStateException();
    };

    var integerFormatter = NumberFormat.getIntegerInstance(locale);
    var currencyFormatter = DecimalFormat.getCurrencyInstance(locale);

    String totalTransactions = integerFormatter.format(report.totalTransactions());
    String totalFrauds = integerFormatter.format(report.totalFrauds());
    String totalAmount = currencyFormatter.format(report.totalAmount());

    ResourceBundle messages = ResourceBundle.getBundle("report", locale);

    IO.println("%s: %s".formatted(messages.getString("total_transactions"), totalTransactions));
    IO.println("%s: %s".formatted(messages.getString("total_frauds"), totalFrauds));
    IO.println("%s: %s".formatted(messages.getString("total_amount"), totalAmount));
  }
}
