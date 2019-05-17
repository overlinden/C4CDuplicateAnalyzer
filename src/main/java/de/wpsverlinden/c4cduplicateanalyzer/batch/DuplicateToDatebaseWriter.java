package de.wpsverlinden.c4cduplicateanalyzer.batch;

import de.wpsverlinden.c4cduplicateanalyzer.model.Duplicate;
import de.wpsverlinden.c4cduplicateanalyzer.persistence.DuplicateRepository;
import java.util.List;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DuplicateToDatebaseWriter implements ItemWriter<List<Duplicate>>{

    @Autowired
    private DuplicateRepository repo;
    
    @Override
    public void write(List<? extends List<Duplicate>> dupes) throws Exception {
        dupes.forEach(repo::saveAll);
        repo.flush();
    }
}