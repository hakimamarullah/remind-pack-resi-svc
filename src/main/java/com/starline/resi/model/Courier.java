package com.starline.resi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Setter
@Table(name = "COURIER")
public class Courier extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "COURIER_SEQ")
    @SequenceGenerator(name = "COURIER_SEQ", sequenceName = "COURIER_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "CODE", length = 20, unique = true, nullable = false)
    @Comment(value = "Courier code or ID to match with cekresi.com values", on = "CODE")
    private String code;

    @Column(name = "NAME", length = 100, nullable = false)
    @Comment(value = "Courier name", on = "NAME")
    private String name;
}
