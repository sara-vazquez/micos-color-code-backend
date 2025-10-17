package dev.sara.micos_color_code.means;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class ResourceMapper {
    public ResourceEntity toEntity(ResourceRequestDTO dto) {
        return ResourceEntity.builder()
        .imageFile(dto.imageFile())
        .name(dto.name())
        .intro(dto.intro())
        .description(dto.description())
        .pdfFile(dto.pdfFile())
        .build();
    }

    public ResourceDetailsResponseDTO toDetailsResponseDTO(ResourceEntity entity) {
        return new ResourceDetailsResponseDTO(entity.getId(), entity.getImageFile(), entity.getName(), entity.getDescription(), entity.getPdfFile());
    }

    public ResourceItemResponseDTO toItemResponseDTO(ResourceEntity entity) {
        return new ResourceItemResponseDTO(entity.getId(), entity.getName(), entity.getIntro());
    }

    public List<ResourceItemResponseDTO> toListItemDTOs(List<ResourceEntity> entities) {
        return entities.stream()
            .map(this::toItemResponseDTO)
            .collect(Collectors.toList());
    }
}
