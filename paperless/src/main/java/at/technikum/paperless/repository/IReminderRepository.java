package at.technikum.paperless.repository;

import at.technikum.paperless.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IReminderRepository extends JpaRepository<Reminder, Long> {
    Optional<Reminder> findByDocumentId(Long documentId);
}
