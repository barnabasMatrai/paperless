package org.example.paperless.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.paperless.dto.in.ReminderCreate;
import org.example.paperless.dto.out.ReminderPublic;
import org.example.paperless.entity.Reminder;
import org.example.paperless.mapper.ReminderMapper;
import org.example.paperless.service.ReminderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/documents/{documentId}/reminder")
public class ReminderController {

    private final ReminderService reminderService;
    private final ReminderMapper reminderMapper;

    @GetMapping
    public ReminderPublic get(@PathVariable Long documentId) {
        return reminderMapper.toObject(
                reminderService.findByDocumentId(documentId)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReminderPublic create(
            @PathVariable Long documentId,
            @Valid @RequestBody ReminderCreate reminderIn) {

        Reminder saved = reminderService.create(
                documentId,
                reminderMapper.toEntity(reminderIn)
        );

        return reminderMapper.toObject(saved);
    }

    @PutMapping
    public ReminderPublic update(
            @PathVariable Long documentId,
            @Valid @RequestBody ReminderCreate reminderIn) {

        Reminder updated = reminderService.update(
                documentId,
                reminderMapper.toEntity(reminderIn)
        );

        return reminderMapper.toObject(updated);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long documentId) {
        reminderService.deleteByDocumentId(documentId);
    }
}
