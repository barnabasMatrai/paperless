package org.example.paperless.service;

import org.example.paperless.entity.Document;
import org.example.paperless.entity.Reminder;
import org.example.paperless.repository.DocumentRepository;
import org.example.paperless.repository.ReminderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final DocumentRepository documentRepository;

    public ReminderService(
            ReminderRepository reminderRepository,
            DocumentRepository documentRepository) {
        this.reminderRepository = reminderRepository;
        this.documentRepository = documentRepository;
    }

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

    public Reminder update(Long documentId, Reminder updatedReminder) {
        Reminder existingReminder = findByDocumentId(documentId);

        existingReminder.setDescription(updatedReminder.getDescription());
        existingReminder.setDueDate(updatedReminder.getDueDate());
        existingReminder.setNotified(updatedReminder.isNotified());

        return reminderRepository.save(existingReminder);
    }

    public void deleteByDocumentId(Long documentId) {
        Reminder reminder = findByDocumentId(documentId);

        Document document = reminder.getDocument();
        document.setReminder(null);

        reminder.setDocument(null);

        reminderRepository.delete(reminder);
    }

    public void deleteById(Long id) {
        reminderRepository.deleteById(id);
    }
}