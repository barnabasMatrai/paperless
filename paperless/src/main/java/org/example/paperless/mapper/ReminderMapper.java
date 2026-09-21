package org.example.paperless.mapper;

import org.example.paperless.dto.in.ReminderCreate;
import org.example.paperless.dto.out.ReminderPublic;
import org.example.paperless.entity.Reminder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReminderMapper {
    Reminder toEntity(ReminderCreate reminderIn);
    ReminderPublic toObject(Reminder reminder);
}
