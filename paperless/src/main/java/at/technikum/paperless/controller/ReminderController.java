package at.technikum.paperless.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import at.technikum.paperless.dto.in.ReminderCreate;
import at.technikum.paperless.dto.out.ReminderPublic;
import at.technikum.paperless.entity.Reminder;
import at.technikum.paperless.mapper.IReminderMapper;
import at.technikum.paperless.service.IReminderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/reminders")
@CrossOrigin
@SecurityRequirement(name = "bearerAuth")
public class ReminderController {

    private final IReminderService reminderService;
    private final IReminderMapper reminderMapper;

    @GetMapping("/{id}")
    public ReminderPublic get(@PathVariable Long id) {
        return reminderMapper.toObject(
                reminderService.findById(id)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReminderPublic create(
            @Valid @RequestBody ReminderCreate reminderIn) {

        Reminder saved = reminderService.create(
                reminderIn.getDocumentId(),
                reminderMapper.toEntity(reminderIn)
        );

        return reminderMapper.toObject(saved);
    }

    @PutMapping("/{id}")
    public ReminderPublic update(
            @PathVariable Long id,
            @Valid @RequestBody ReminderCreate reminderIn) {

        Reminder updated = reminderService.update(
                id,
                reminderMapper.toEntity(reminderIn)
        );

        return reminderMapper.toObject(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        reminderService.deleteById(id);
    }
}
