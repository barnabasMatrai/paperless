package org.example.paperless.mapper;

import org.example.paperless.dto.in.DocumentCreate;
import org.example.paperless.dto.out.DocumentPublic;
import org.example.paperless.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DocumentMapper {
    Document toEntity(DocumentCreate documentIn);
    DocumentPublic toObject(Document document);
}

