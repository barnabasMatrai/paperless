package org.example.paperless.service;

import org.example.paperless.entity.Reminder;

import java.util.List;

public interface IReminderService {

    List<Reminder> findAll();

    Reminder findById(Long id);

    Reminder findByDocumentId(Long documentId);

    Reminder create(Long documentId, Reminder reminder);

    Reminder update(Long documentId, Reminder updatedReminder);

    void deleteByDocumentId(Long documentId);

    void deleteById(Long id);
}
