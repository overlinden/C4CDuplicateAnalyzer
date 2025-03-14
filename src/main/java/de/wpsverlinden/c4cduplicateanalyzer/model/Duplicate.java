package de.wpsverlinden.c4cduplicateanalyzer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Duplicate implements Persistable<Integer> {
    @Id
    private Integer DuplicateID;
    
    private Integer Account1Id;
    private Integer Account2Id;
    private float Similarity;

    @Override
    public Integer getId() {
        return DuplicateID;
    }

    @Override
    public boolean isNew() {
        return true;
    }
}
