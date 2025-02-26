package de.wpsverlinden.c4cduplicateanalyzer.repository;

import de.wpsverlinden.c4cduplicateanalyzer.model.Account;
import org.springframework.data.repository.CrudRepository;

public interface AccountRepository extends CrudRepository<Account, Long> {

}
