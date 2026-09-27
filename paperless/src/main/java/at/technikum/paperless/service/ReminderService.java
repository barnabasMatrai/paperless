package at.technikum.paperless.service;

import at.technikum.paperless.entity.Document;
import at.technikum.paperless.entity.Reminder;
import at.technikum.paperless.repository.IDocumentRepository;
import at.technikum.paperless.repository.IReminderRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class ReminderService implements IReminderService {

    private final IReminderRepository reminderRepository;
    private final IDocumentRepository documentRepository;

    public List<Reminder> findAll() {
        return reminderRepository.findAll();
    }

    public Reminder findById(Long id) {
        return reminderRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Reminder not found: " + id));
    }

    public Reminder findByDocumentId(Long documentId) {
        return reminderRepository.findByDocumentId(documentId)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Reminder not found for document: " + documentId));
    }

    public Reminder create(Long documentId, Reminder reminder) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Document not found: " + documentId));

        reminder.setDocument(document);

        return reminderRepository.save(reminder);
    }

    public Reminder update(Long id, Reminder updatedReminder) {
        Reminder existingReminder = findById(id);

        existingReminder.setDescription(updatedReminder.getDescription());
        existingReminder.setDueDate(updatedReminder.getDueDate());
        existingReminder.setNotified(updatedReminder.isNotified());

        return reminderRepository.save(existingReminder);
    }

    public void deleteByDocumentId(Long reminderId) {
        Reminder reminder = findById(reminderId);

        Document document = reminder.getDocument();
        document.setReminder(null);

        reminder.setDocument(null);

        reminderRepository.delete(reminder);
    }

    public void deleteById(Long id) {
        reminderRepository.deleteById(id);
    }
}