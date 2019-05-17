package de.wpsverlinden.c4cduplicateanalyzer.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import de.wpsverlinden.c4cduplicateanalyzer.model.Duplicate;

@Repository
public interface DuplicateRepository extends JpaRepository<Duplicate, Long> {
}
