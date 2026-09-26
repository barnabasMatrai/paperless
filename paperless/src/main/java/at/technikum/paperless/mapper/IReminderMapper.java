package at.technikum.paperless.mapper;

import at.technikum.paperless.dto.in.ReminderCreate;
import at.technikum.paperless.dto.out.ReminderPublic;
import at.technikum.paperless.entity.Reminder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IReminderMapper {
    Reminder toEntity(ReminderCreate reminderIn);
    ReminderPublic toObject(Reminder reminder);
}
