package at.technikum.paperless.repository;

import at.technikum.paperless.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findAllByUserIdOrderByUploadDateDesc(Long userId);
}
