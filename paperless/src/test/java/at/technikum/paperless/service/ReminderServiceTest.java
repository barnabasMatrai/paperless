package at.technikum.paperless.service;

import at.technikum.paperless.entity.Document;
import at.technikum.paperless.entity.Reminder;
import at.technikum.paperless.exception.ResourceNotFoundException;
import at.technikum.paperless.repository.IDocumentRepository;
import at.technikum.paperless.repository.IReminderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock
    private IReminderRepository reminderRepository;
    @Mock
    private IDocumentRepository documentRepository;

    @InjectMocks
    private ReminderService reminderService;

    private Reminder reminder;
    private Document document;

    @BeforeEach
    void setUp() {
        reminder = new Reminder();
        reminder.setId(5L);
        reminder.setDescription("Erinnerung");
        reminder.setDueDate(Instant.now().plusSeconds(3600));

        document = new Document();
        document.setId(7L);
        reminder.setDocument(document);
        document.setReminder(reminder);
    }

    @Test
    void findAllReturnsAllReminders() {
        when(reminderRepository.findAll()).thenReturn(List.of(reminder));

        assertThat(reminderService.findAll()).containsExactly(reminder);
    }

    @Test
    void findByIdReturnsReminder() {
        when(reminderRepository.findById(5L)).thenReturn(Optional.of(reminder));

        assertThat(reminderService.findById(5L)).isSameAs(reminder);
    }

    @Test
    void findByIdThrowsIfNotFound() {
        when(reminderRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reminderService.findById(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByDocumentIdReturnsReminder() {
        when(reminderRepository.findByDocumentId(7L)).thenReturn(Optional.of(reminder));

        assertThat(reminderService.findByDocumentId(7L)).isSameAs(reminder);
    }

    @Test
    void findByDocumentIdThrowsIfNotFound() {
        when(reminderRepository.findByDocumentId(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reminderService.findByDocumentId(7L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createSetsDocumentAndSavesReminder() {
        when(documentRepository.findById(7L)).thenReturn(Optional.of(document));
        when(reminderRepository.save(reminder)).thenReturn(reminder);

        Reminder saved = reminderService.create(7L, reminder);

        assertThat(saved).isSameAs(reminder);
        assertThat(reminder.getDocument()).isSameAs(document);
        verify(reminderRepository).save(reminder);
    }

    @Test
    void createThrowsIfDocumentNotFound() {
        when(documentRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reminderService.create(7L, reminder))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateCopiesFieldsAndSaves() {
        when(reminderRepository.findById(5L)).thenReturn(Optional.of(reminder));
        when(reminderRepository.save(reminder)).thenReturn(reminder);

        Reminder updated = new Reminder();
        updated.setDescription("Geänderte Erinnerung");
        updated.setDueDate(Instant.now().plusSeconds(7200));
        updated.setNotified(true);

        Reminder result = reminderService.update(5L, updated);

        assertThat(result).isSameAs(reminder);
        assertThat(reminder.getDescription()).isEqualTo("Geänderte Erinnerung");
        assertThat(reminder.getDueDate()).isEqualTo(updated.getDueDate());
        assertThat(reminder.isNotified()).isTrue();
        verify(reminderRepository).save(reminder);
    }

    @Test
    void deleteByDocumentIdUnlinksDocumentAndDeletesReminder() {
        when(reminderRepository.findById(5L)).thenReturn(Optional.of(reminder));

        reminderService.deleteByDocumentId(5L);

        assertThat(document.getReminder()).isNull();
        assertThat(reminder.getDocument()).isNull();
        verify(reminderRepository).delete(reminder);
    }

    @Test
    void deleteByIdUnlinksDocumentAndDeletesReminder() {
        when(reminderRepository.findById(5L)).thenReturn(Optional.of(reminder));

        reminderService.deleteById(5L);

        assertThat(document.getReminder()).isNull();
        verify(reminderRepository).delete(reminder);
    }
}
