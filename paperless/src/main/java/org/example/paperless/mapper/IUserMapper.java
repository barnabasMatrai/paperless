package org.example.paperless.mapper;

import org.example.paperless.dto.in.ReminderCreate;
import org.example.paperless.dto.in.UserCreate;
import org.example.paperless.dto.out.ReminderPublic;
import org.example.paperless.dto.out.UserPublic;
import org.example.paperless.entity.Reminder;
import org.example.paperless.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = IUserMapper.class)
public interface IUserMapper {
    User toEntity(UserCreate userIn);
    UserPublic toObject(User user);
}
