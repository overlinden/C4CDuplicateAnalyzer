package de.wpsverlinden.c4cduplicateanalyzer.repository;

import de.wpsverlinden.c4cduplicateanalyzer.model.Duplicate;
import org.springframework.data.repository.CrudRepository;

public interface DuplicateRepository extends CrudRepository<Duplicate, Long> {
    
}
