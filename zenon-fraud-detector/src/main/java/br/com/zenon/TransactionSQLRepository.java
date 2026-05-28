package br.com.zenon;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class TransactionSQLRepository implements TransactionRepository {

  private static final int BATCH_SIZE = 5000;

  @Override
  public Optional<Transaction> findByOriginName(String originName) {

    String sql = """
        SELECT
          id,
          step,
          `type`,
          amount,
          name_origin,
          old_balance_origin,
          new_balance_origin,
          name_recipient,
          old_balance_recipient,
          new_balance_recipient,
          is_fraud,
          is_flagged_fraud
        FROM zenon_frauds.transactions
        WHERE name_origin = ?
        ORDER BY id DESC
        LIMIT 1;
        """;

    try (var connection = ConnectionFactory.getConnection();
         var ps = connection.prepareStatement(sql)) {

      ps.setString(1, originName);
      try (var resultSet = ps.executeQuery()) {
        while (resultSet.next()) {
          Transaction transaction = mapResultSetToTransaction(resultSet);
          if (transaction.origin().name().equals(originName)) {
            return Optional.of(transaction);
          } else {
            IO.println("Transaction %s não encontrada".formatted(originName));
            return Optional.empty();
          }
        }
      } catch (SQLException e) {
        throw new RuntimeException("Erro ao buscar a transação: " + originName, e);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Erro ao buscar a transação: " + e);
    }
    return Optional.empty();
  }

  @Override
  public void save(Transaction transaction) {
    String sql = """
        INSERT INTO transactions (
          step,
          `type`,
          amount,
          name_origin,
          old_balance_origin,
          new_balance_origin,
          name_recipient,
          old_balance_recipient,
          new_balance_recipient,
          is_fraud,
          is_flagged_fraud)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        """;
    try (var connection = ConnectionFactory.getConnection();
         var ps = connection.prepareStatement(sql)) {
          mapTransactionToPreparedStatement(ps, transaction);
          ps.execute();
    } catch (SQLException e) {
      throw new RuntimeException("Erro ao salvar a transação: " + transaction, e);
    }
  }

  @Override
  public void saveAll(List<Transaction> transactions) {
    String sql = """
        INSERT INTO transactions (
          step,
          `type`,
          amount,
          name_origin,
          old_balance_origin,
          new_balance_origin,
          name_recipient,
          old_balance_recipient,
          new_balance_recipient,
          is_fraud,
          is_flagged_fraud)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        """;
    try (var connection = ConnectionFactory.getConnection()) {
      try (var ps = connection.prepareStatement(sql)) {
        connection.setAutoCommit(false);

        int count = 0;
        for (Transaction transaction : transactions) {
          mapTransactionToPreparedStatement(ps, transaction);
          ps.addBatch();
          count++;
          if (count % BATCH_SIZE == 0) {
            ps.executeBatch();
            connection.commit();
          }
        }
        if (count % BATCH_SIZE != 0) {
          ps.executeBatch();
          connection.commit();
        }

      } catch (SQLException e) {
        try {
          connection.rollback();
        } catch (SQLException e2) {
          throw new RuntimeException("Erro ao rollback: " + e2);
        }
        throw new RuntimeException("Erro ao salvar as transações: " + transactions, e);
      } finally {
        connection.setAutoCommit(true);
      }

    } catch (SQLException e) {
      throw new RuntimeException("Erro ao conectar ao banco", e);
    }
  }

    private Transaction mapResultSetToTransaction(ResultSet resultSet) {
      try {
        return new Transaction(
          resultSet.getInt("step"),
          TransactionType.valueOf(resultSet.getString("type")),
          resultSet.getBigDecimal("amount"),
  
          new TransactionCustomer(resultSet.getString("name_origin"),
            resultSet.getBigDecimal("old_balance_origin"),
            resultSet.getBigDecimal("new_balance_origin")),
          new TransactionCustomer(resultSet.getString("name_recipient"),
            resultSet.getBigDecimal("old_balance_recipient"),
            resultSet.getBigDecimal("new_balance_recipient")),
  
          resultSet.getBoolean("is_fraud"), resultSet.getBoolean("is_flagged_fraud"));
      } catch (SQLException e) {
        throw new RuntimeException("Erro ao mapear o resultado do ResultSet para Transaction", e);
      }
    }

    private void mapTransactionToPreparedStatement(PreparedStatement ps, Transaction transaction) {
      try {
        ps.setInt(1, transaction.step());
        ps.setString(2, transaction.type().name());
        ps.setBigDecimal(3, transaction.amount());

        ps.setString(4, transaction.origin().name());
        ps.setBigDecimal(5, transaction.origin().oldBalance());
        ps.setBigDecimal(6, transaction.origin().newBalance());
        ps.setString(7, transaction.recipient().name());
        ps.setBigDecimal(8, transaction.recipient().oldBalance());
        ps.setBigDecimal(9, transaction.recipient().newBalance());
      
        ps.setBoolean(10, transaction.isFraud());
        ps.setBoolean(11, transaction.isFlaggedFraud());
      } catch (SQLException e) {
        throw new RuntimeException("Erro ao mapear a transação para PreparedStatement", e);
      }
    }
}
