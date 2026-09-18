package org.example.paperless.mapper;

import org.example.paperless.dto.DocumentPublic;
import org.example.paperless.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.Mapping;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DocumentMapper {


}

