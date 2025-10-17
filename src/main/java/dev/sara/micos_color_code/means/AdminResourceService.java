package dev.sara.micos_color_code.means;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminResourceService {

    private final ResourceRepository resourceRepository;
    private final ResourceMapper resourceMapper;
    
    public ResourceDetailsResponseDTO create(ResourceRequestDTO requestDTO) {
        ResourceEntity newEntity = resourceMapper.toEntity(requestDTO);
        
        ResourceEntity savedEntity = resourceRepository.save(newEntity);
        
        return resourceMapper.toDetailsResponseDTO(savedEntity);
    }

    public ResourceDetailsResponseDTO update(Long id, ResourceRequestDTO requestDTO) {
        ResourceEntity existingEntity = resourceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado con ID: " + id));

        existingEntity.setImage(requestDTO.image());
        existingEntity.setName(requestDTO.name());
        existingEntity.setIntro(requestDTO.intro());
        existingEntity.setDescription(requestDTO.description());
        existingEntity.setPdf(requestDTO.pdf());
        
        ResourceEntity updatedEntity = resourceRepository.save(existingEntity);
        
        return resourceMapper.toDetailsResponseDTO(updatedEntity);
    }

    public void delete(Long id) {
        if (!resourceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Recurso a borrar no encontrado con ID: " + id);
        }
        resourceRepository.deleteById(id);
    }

    public List<ResourceItemResponseDTO> findAllAdmin() {
        List<ResourceEntity> entities = resourceRepository.findAll();
        
        return resourceMapper.toListItemDTOs(entities);
    }
}
