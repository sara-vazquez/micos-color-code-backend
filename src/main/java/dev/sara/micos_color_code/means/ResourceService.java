package dev.sara.micos_color_code.means;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ResourceMapper resourceMapper;

    public List<ResourceItemResponseDTO> findAllPublic() {
        List<ResourceEntity> entities = resourceRepository.findAll(); 

        return resourceMapper.toListItemDTOs(entities);
    }

    public ResourceDetailsResponseDTO findByIdPublic(Long id) {
        ResourceEntity entity = resourceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado con ID: " + id)); 
        
        return resourceMapper.toDetailsResponseDTO(entity);
    }
    
}
