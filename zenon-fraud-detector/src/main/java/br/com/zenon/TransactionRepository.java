package br.com.zenon;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository {

  Optional<Transaction> findByOriginName(String originName);

  void save(Transaction transaction);
  void saveAll(List<Transaction> transactions);

}