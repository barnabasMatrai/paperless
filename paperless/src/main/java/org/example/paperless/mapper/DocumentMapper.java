package org.example.paperless.mapper;

import org.example.paperless.dto.in.DocumentCreate;
import org.example.paperless.dto.out.DocumentPublic;
import org.example.paperless.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = ReminderMapper.class)
public interface DocumentMapper {

    @Mapping(source = "type", target = "documentType")
    Document toEntity(DocumentCreate documentIn);

    @Mapping(source = "documentType", target = "type")
    DocumentPublic toObject(Document document);
}

