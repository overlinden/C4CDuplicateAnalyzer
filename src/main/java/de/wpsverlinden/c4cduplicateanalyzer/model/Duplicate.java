package de.wpsverlinden.c4cduplicateanalyzer.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder()
public class Duplicate {
    @Id
    @GeneratedValue
    private long id;
    
    @Column(length = 1024)
    private String a;
    @Column(length = 1024)
    private String b;
    private float similarity;

}
