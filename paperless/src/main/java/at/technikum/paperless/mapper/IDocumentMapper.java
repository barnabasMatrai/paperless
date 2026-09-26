package at.technikum.paperless.mapper;

import at.technikum.paperless.dto.in.DocumentCreate;
import at.technikum.paperless.dto.out.DocumentPublic;
import at.technikum.paperless.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = IReminderMapper.class)
public interface IDocumentMapper {

    @Mapping(source = "type", target = "documentType")
    Document toEntity(DocumentCreate documentIn);

    @Mapping(source = "documentType", target = "type")
    DocumentPublic toObject(Document document);
}

