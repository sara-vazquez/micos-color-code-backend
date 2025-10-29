package dev.sara.micos_color_code.unit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.sara.micos_color_code.means.ResourceDetailsResponseDTO;
import dev.sara.micos_color_code.means.ResourceEntity;
import dev.sara.micos_color_code.means.ResourceItemResponseDTO;
import dev.sara.micos_color_code.means.ResourceMapper;
import dev.sara.micos_color_code.means.ResourceNotFoundException;
import dev.sara.micos_color_code.means.ResourceRepository;
import dev.sara.micos_color_code.means.ResourceService;

@ExtendWith(MockitoExtension.class)
public class ResourceServiceTest {
    
    @InjectMocks
    private ResourceService resourceService;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private ResourceMapper resourceMapper;

    private ResourceEntity resourceEntity;
    private ResourceItemResponseDTO itemResponseDTO;
    private ResourceDetailsResponseDTO detailsResponseDTO;

    @BeforeEach
    void setUp() {
        resourceEntity = new ResourceEntity();
        resourceEntity.setId(1L);
        resourceEntity.setName("El sistema");

        itemResponseDTO = new ResourceItemResponseDTO(1L, "El sistema", "Póster", "Cómo se construye el sistema", "/uploads/images/img.png", "/uploads/pdfs/");

        detailsResponseDTO = new ResourceDetailsResponseDTO(1L, "El sistema", "Póster", "Cómo se construye el sistema", "/uploads/images/img.png", "/uploads/pdfs/");
    }


    @Test
    void findAllPublic_ShouldReturnListOfResources_WhenResourcesExist() {
        List<ResourceEntity> entities = List.of(resourceEntity);
        List<ResourceItemResponseDTO> expectedDTOs = List.of(itemResponseDTO);

        when(resourceRepository.findAll()).thenReturn(entities);
        when(resourceMapper.toListItemDTOs(entities)).thenReturn(expectedDTOs);

        List<ResourceItemResponseDTO> result = resourceService.findAllPublic();

        verify(resourceRepository).findAll();
        verify(resourceMapper).toListItemDTOs(entities);
        
        assertThat(result, is(notNullValue()));
        assertThat(result.get(0).id(), is(equalTo(1L)));
    }

    @Test
    void findAllPublic_ShouldReturnEmptyList_WhenNoResourcesExist() {
        List<ResourceEntity> emptyList = List.of();

        when(resourceRepository.findAll()).thenReturn(emptyList);
        when(resourceMapper.toListItemDTOs(emptyList)).thenReturn(List.of());

        List<ResourceItemResponseDTO> result = resourceService.findAllPublic();

        verify(resourceRepository).findAll();
        verify(resourceMapper).toListItemDTOs(emptyList);
        
        assertThat(result, is(notNullValue()));
        assertThat(result, is(empty()));
    }

    @Test
    void findAllPublic_ShouldCallRepositoryAndMapper_WhenExecuted() {
        List<ResourceEntity> entities = List.of(resourceEntity);

        when(resourceRepository.findAll()).thenReturn(entities);
        when(resourceMapper.toListItemDTOs(entities)).thenReturn(List.of(itemResponseDTO));

        resourceService.findAllPublic();

        verify(resourceRepository, times(1)).findAll();
        verify(resourceMapper, times(1)).toListItemDTOs(entities);
    }

    @Test
    void findByIdPublic_ShouldReturnResourceDetails_WhenResourceExists() {
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resourceEntity));
        when(resourceMapper.toDetailsResponseDTO(resourceEntity)).thenReturn(detailsResponseDTO);

        ResourceDetailsResponseDTO result = resourceService.findByIdPublic(1L);

        verify(resourceRepository).findById(1L);
        verify(resourceMapper).toDetailsResponseDTO(resourceEntity);
        
        assertThat(result, is(notNullValue()));
        assertThat(result.id(), is(equalTo(1L)));
        assertThat(result.name(), is(equalTo("El sistema")));
    }

    @Test
    void findByIdPublic_ShouldThrowException_WhenResourceNotFound() {
        when(resourceRepository.findById(999L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                resourceService.findByIdPublic(999L)
        );

        assertThat(exception.getMessage(), is("Recurso no encontrado con ID: 999"));
        verify(resourceRepository).findById(999L);
        verify(resourceMapper, never()).toDetailsResponseDTO(any());
    }
}
