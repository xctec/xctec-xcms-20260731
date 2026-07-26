package com.df4j.xctec.xcms.file.mapper;

import com.df4j.xctec.xcms.file.api.dto.FileDTO;
import com.df4j.xctec.xcms.file.domain.FileInfo;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FileMapper {

    FileDTO toDto(FileInfo entity);

    List<FileDTO> toDtoList(List<FileInfo> entities);
}
