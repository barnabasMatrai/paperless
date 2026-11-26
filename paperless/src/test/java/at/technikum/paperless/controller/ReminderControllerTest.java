package at.technikum.paperless.controller;

import at.technikum.paperless.dto.in.ReminderCreate;
import at.technikum.paperless.dto.in.ReminderUpdateCreate;
import at.technikum.paperless.dto.out.ReminderPublic;
import at.technikum.paperless.entity.Reminder;
import at.technikum.paperless.mapper.IReminderMapper;
import at.technikum.paperless.service.IReminderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReminderControllerTest {

    @Mock
    private IReminderService reminderService;
    @Mock
    private IReminderMapper reminderMapper;

    private ReminderController controller;

    @BeforeEach
    void setUp() {
        controller = new ReminderController(reminderService, reminderMapper);
    }

    @Test
    void getReturnsMappedPublic() {
        Reminder reminder = new Reminder();
        ReminderPublic publicReminder = new ReminderPublic();
        when(reminderService.findById(5L)).thenReturn(reminder);
        when(reminderMapper.toObject(reminder)).thenReturn(publicReminder);

        assertThat(controller.get(5L)).isSameAs(publicReminder);
        verify(reminderService).findById(5L);
    }

    @Test
    void createMapsAndDelegates() {
        ReminderCreate in = new ReminderCreate();
        in.setDocumentId(7L);
        in.setDescription("Erinnerung");
        Reminder entity = new Reminder();
        Reminder saved = new Reminder();

        when(reminderMapper.toEntity(in)).thenReturn(entity);
        when(reminderService.create(7L, entity)).thenReturn(saved);
        when(reminderMapper.toObject(saved)).thenReturn(new ReminderPublic());

        assertThat(controller.create(in)).isNotNull();
        verify(reminderService).create(7L, entity);
    }

    @Test
    void updateMapsAndDelegates() {
        ReminderUpdateCreate in = new ReminderUpdateCreate();
        Reminder entity = new Reminder();

        when(reminderMapper.toEntity(in)).thenReturn(entity);
        when(reminderService.update(5L, entity)).thenReturn(new Reminder());
        when(reminderMapper.toObject(any())).thenReturn(new ReminderPublic());

        assertThat(controller.update(5L, in)).isNotNull();
        verify(reminderService).update(5L, entity);
    }

    @Test
    void deleteDelegatesToService() {
        controller.delete(5L);

        verify(reminderService).deleteById(5L);
    }
}
