package org.example.paperless.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.paperless.model.DocumentType;

import java.time.LocalDateTime;

@Entity
//@Table(name = "document")
@Getter
@Setter

public class Document
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private LocalDateTime uploadDate = LocalDateTime.now();

    @OneToOne(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private Reminder reminder;

}
