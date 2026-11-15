package at.technikum.ocrworker.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

/**
 * Eintrag im Elasticsearch-Index "documents": verknüpft die Dokument-ID aus der Datenbank
 * mit dem per OCR erkannten Text, damit danach gesucht werden kann.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "documents")
public class OcrDocument {

    // Gleiche ID wie in Postgres, damit ein Suchtreffer direkt dem Dokument zugeordnet werden kann
    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant indexedAt;
}