package at.technikum.paperless.service;

import at.technikum.paperless.entity.Document;
import at.technikum.paperless.entity.Reminder;
import at.technikum.paperless.repository.IDocumentRepository;
import at.technikum.paperless.repository.IReminderRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@AllArgsConstructor
public class ReminderService implements IReminderService {

    private final IReminderRepository reminderRepository;
    private final IDocumentRepository documentRepository;

    public List<Reminder> findAll() {
        log.debug("Fetching all reminders");

        List<Reminder> reminders = reminderRepository.findAll();

        log.debug("Retrieved {} reminders", reminders.size());
        return reminders;
    }

    public Reminder findById(Long id) {
        log.debug("Fetching reminder with ID {}", id);

        return reminderRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Reminder with ID {} was not found", id);
                    return new NoSuchElementException(
                            "Reminder not found: " + id);
                });
    }

    public Reminder findByDocumentId(Long documentId) {
        log.debug("Fetching reminder for document with ID {}", documentId);

        return reminderRepository.findByDocumentId(documentId)
                .orElseThrow(() -> {
                    log.warn("Reminder not found for document with ID {}",
                            documentId);
                    return new NoSuchElementException(
                            "Reminder not found for document: " + documentId);
                });
    }

    public Reminder create(Long documentId, Reminder reminder) {
        log.info("Creating reminder for document with ID {}", documentId);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> {
                    log.warn("Cannot create reminder: document with ID {} was not found",
                            documentId);
                    return new NoSuchElementException(
                            "Document not found: " + documentId);
                });

        reminder.setDocument(document);

        Reminder savedReminder = reminderRepository.save(reminder);

        log.info("Reminder with ID {} created successfully for document with ID {}",
                savedReminder.getId(), documentId);

        return savedReminder;
    }

    public Reminder update(Long id, Reminder updatedReminder) {
        log.info("Updating reminder with ID {}", id);

        Reminder existingReminder = findById(id);

        existingReminder.setDescription(updatedReminder.getDescription());
        existingReminder.setDueDate(updatedReminder.getDueDate());
        existingReminder.setNotified(updatedReminder.isNotified());

        Reminder savedReminder = reminderRepository.save(existingReminder);

        log.info("Reminder with ID {} updated successfully", id);

        return savedReminder;
    }

    public void deleteByDocumentId(Long reminderId) {
        log.info("Deleting reminder with ID {} by document association",
                reminderId);

        Reminder reminder = findById(reminderId);

        Document document = reminder.getDocument();
        document.setReminder(null);

        reminder.setDocument(null);

        reminderRepository.delete(reminder);

        log.info("Reminder with ID {} deleted successfully", reminderId);
    }

    @Override
    public void deleteById(Long id) {
        log.info("Deleting reminder with ID {}", id);

        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cannot delete reminder: ID {} was not found", id);
                    return new NoSuchElementException(
                            "Reminder not found: " + id);
                });

        Document document = reminder.getDocument();

        document.setReminder(null);

        reminderRepository.delete(reminder);

        log.info("Reminder with ID {} deleted successfully", id);
    }
}
