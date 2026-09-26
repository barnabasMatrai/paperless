package at.technikum.paperless.mapper;

import at.technikum.paperless.dto.in.UserCreate;
import at.technikum.paperless.dto.in.UserRegisterCreate;
import at.technikum.paperless.dto.out.UserLoginPublic;
import at.technikum.paperless.dto.out.UserPublic;
import at.technikum.paperless.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = IUserMapper.class)
public interface IUserMapper {
    User toEntity(UserCreate userIn);
    User toEntity(UserRegisterCreate userIn);
    UserPublic toObject(User user);
    UserLoginPublic toLoginObject(User user);
}
