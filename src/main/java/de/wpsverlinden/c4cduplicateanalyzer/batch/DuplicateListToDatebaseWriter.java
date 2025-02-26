package de.wpsverlinden.c4cduplicateanalyzer.batch;

import de.wpsverlinden.c4cduplicateanalyzer.model.Duplicate;
import de.wpsverlinden.c4cduplicateanalyzer.repository.DuplicateRepository;
import java.util.List;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

//At the moment the throttleLimit is defaulted to 4. There is no valid workaround for that. As workaround I use:
//(1) no parallelizing task executor in the step configuration,
//(2) chunk size of 1 from spring batch point of view
//(3) custom chunking within itemreader, parallelization in item processor, serializatiion in item writer.
public class DuplicateListToDatebaseWriter implements ItemWriter<List<Duplicate>> {

    @Autowired
    private DuplicateRepository repo;

    @Override
    public void write(Chunk<? extends List<Duplicate>> chunk) throws Exception {
        chunk.getItems().forEach(repo::saveAll);
    }
}
