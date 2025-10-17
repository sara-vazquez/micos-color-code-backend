package dev.sara.micos_color_code.means;

import org.springframework.stereotype.Component;

@Component
public class ResourceMapper {
    public ResourceEntity toEntity(ResourceRequestDTO dto) {
        return ResourceEntity.builder()
        .image(dto.image())
        .name(dto.name())
        .intro(dto.intro())
        .description(dto.descrption())
        .pdf(dto.pdf())
        .build();
    }

    public ResourceResponseDTO toResponse(ResourceEntity entity) {
        return new ResourceResponseDTO(entity.getId(), entity.getImage(), entity.getName(), entity.getIntro(), entity.getDescription(), entity.getPdf());
    }
}
