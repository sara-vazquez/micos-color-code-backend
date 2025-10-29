package dev.sara.micos_color_code.unit;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import dev.sara.micos_color_code.means.AdminResourceService;
import dev.sara.micos_color_code.means.ResourceDetailsResponseDTO;
import dev.sara.micos_color_code.means.ResourceEntity;
import dev.sara.micos_color_code.means.ResourceMapper;
import dev.sara.micos_color_code.means.ResourceRepository;
import dev.sara.micos_color_code.means.ResourceRequestDTO;

@ExtendWith(MockitoExtension.class)
public class AdminResourceServiceTest {

    @InjectMocks
    private AdminResourceService adminResourceService;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private ResourceMapper resourceMapper;

    private ResourceRequestDTO requestDTO;
    private ResourceEntity resourceEntity;
    private ResourceDetailsResponseDTO responseDTO;

    private MockMultipartFile imageFile;
    private MockMultipartFile pdfFile;

    @BeforeEach
    void setUp() {
        requestDTO = new ResourceRequestDTO("El sistema", "Póster", "Cómo se construye el sistema", null, null);

        resourceEntity = new ResourceEntity();
        resourceEntity.setName("El sistema");
        resourceEntity.setIntro("Póster");
        resourceEntity.setDescription("Cómo se construye el sistema");
        resourceEntity.setImageFile("/uploads/images/img.png");
        resourceEntity.setPdfFile("/uploads/pdfs/doc.pdf");

        responseDTO = new ResourceDetailsResponseDTO(1L, "El sistema", "Póster", "Cómo se construye el sistema", "/uploads/images/img.png", "/uploads/pdfs/");

        imageFile = new MockMultipartFile("imageFile", "img.png", "image/png", "fake image content".getBytes());
        pdfFile = new MockMultipartFile("pdfFile", "doc.pdf", "application/pdf", "fake pdf content".getBytes());

        adminResourceService = new AdminResourceService(resourceRepository, resourceMapper);
        try {
            var field = AdminResourceService.class.getDeclaredField("uploadPath");
            field.setAccessible(true);
            field.set(adminResourceService, "src/test/resources/uploads");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void create_ShouldSaveResource_WhenDataIsValid() {
        when(resourceMapper.toEntity(any(ResourceRequestDTO.class))).thenReturn(resourceEntity);
        when(resourceRepository.save(any(ResourceEntity.class))).thenReturn(resourceEntity);
        when(resourceMapper.toDetailsResponseDTO(resourceEntity)).thenReturn(responseDTO);

        ResourceDetailsResponseDTO result = adminResourceService.create(requestDTO, imageFile, pdfFile);

        verify(resourceRepository).save(any(ResourceEntity.class));
        assertThat(result.name(), is("El sistema"));
        assertThat(result.imageFile(), containsString("/uploads/images/"));
        assertThat(result.pdfFile(), containsString("/uploads/pdfs/"));
    }

    @Test
    void create_ShouldThrowException_WhenImageIsMissing() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                adminResourceService.create(requestDTO, null, pdfFile)
        );

        assertThat(exception.getMessage(), is("La imagen es obligatoria"));
        verify(resourceRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowException_WhenPdfIsMissing() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                adminResourceService.create(requestDTO, imageFile, null)
        );

        assertThat(exception.getMessage(), is("El PDF es obligatorio"));
        verify(resourceRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowRuntimeException_WhenIOExceptionOccurs() throws IOException {
        MockMultipartFile brokenFile = mock(MockMultipartFile.class);
        when(brokenFile.isEmpty()).thenReturn(false);
        when(brokenFile.getContentType()).thenReturn("image/png");
        when(brokenFile.getOriginalFilename()).thenReturn("img.png");
        when(brokenFile.getInputStream()).thenThrow(new IOException("Simulated I/O error"));

        Exception exception = assertThrows(RuntimeException.class, () ->
                adminResourceService.create(requestDTO, brokenFile, pdfFile)
        );

        assertThat(exception.getMessage(), containsString("Error al guardar archivos"));
    }

    @Test
    void update_ShouldUpdateResource_WhenDataIsValid() {
        Long resourceId = 1L;

        ResourceRequestDTO updateDTO = new ResourceRequestDTO(
            "Nuevo nombre",
            "Nueva intro",
            "Nueva descripción",
            null,
            null
        );

        MockMultipartFile newImage = new MockMultipartFile("imageFile", "new.png", "image/png", "fake".getBytes());
        MockMultipartFile newPdf = new MockMultipartFile("pdfFile", "new.pdf", "application/pdf", "fake".getBytes());

        ResourceEntity existingEntity = new ResourceEntity();
        existingEntity.setId(resourceId);
        existingEntity.setName("Antiguo nombre");
        existingEntity.setIntro("Antigua intro");
        existingEntity.setDescription("Antigua descripción");
        existingEntity.setImageFile("/uploads/images/old.png");
        existingEntity.setPdfFile("/uploads/pdfs/old.pdf");

        ResourceEntity savedEntity = new ResourceEntity();
        savedEntity.setId(resourceId);
        savedEntity.setName("Nuevo nombre");
        savedEntity.setIntro("Nueva intro");
        savedEntity.setDescription("Nueva descripción");
        savedEntity.setImageFile("/uploads/images/new.png");
        savedEntity.setPdfFile("/uploads/pdfs/new.pdf");

        ResourceDetailsResponseDTO expectedResponse = new ResourceDetailsResponseDTO(
            resourceId,
            "Nuevo nombre",
            "Nueva intro",
            "Nueva descripción",
            "/uploads/images/new.png",
            "/uploads/pdfs/new.pdf"
        );

        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(existingEntity));
        when(resourceRepository.save(any(ResourceEntity.class))).thenReturn(savedEntity);
        when(resourceMapper.toDetailsResponseDTO(savedEntity)).thenReturn(expectedResponse);

        ResourceDetailsResponseDTO result = adminResourceService.update(resourceId, updateDTO, newImage, newPdf);

        verify(resourceRepository).save(any(ResourceEntity.class));
        assertThat(result.name(), is("Nuevo nombre"));
        assertThat(result.imageFile(), containsString("/uploads/images/"));
        assertThat(result.pdfFile(), containsString("/uploads/pdfs/"));
    }

    @Test
    void update_ShouldKeepExistingFiles_WhenNoNewFilesProvided() {
        Long id = 1L;

        ResourceEntity existingEntity = new ResourceEntity();
        existingEntity.setId(id);
        existingEntity.setName("Antiguo");
        existingEntity.setIntro("Intro antigua");
        existingEntity.setDescription("Desc antigua");
        existingEntity.setImageFile("/uploads/images/old.png");
        existingEntity.setPdfFile("/uploads/pdfs/old.pdf");

        ResourceRequestDTO dto = new ResourceRequestDTO("Nuevo", "Intro nueva", "Desc nueva", "/uploads/images/old.png", "/uploads/pdfs/old.pdf");

        when(resourceRepository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(resourceRepository.save(any(ResourceEntity.class))).thenReturn(existingEntity);
        when(resourceMapper.toDetailsResponseDTO(existingEntity)).thenReturn(
            new ResourceDetailsResponseDTO(id, "Nuevo", "Intro nuevA", "Desc nueva", "/uploads/images/old.png", "/uploads/pdfs/old.pdf")
        );

        ResourceDetailsResponseDTO result = adminResourceService.update(id, dto, null, null);

        verify(resourceRepository).save(any(ResourceEntity.class));
        assertThat(result.imageFile(), is("/uploads/images/old.png"));
        assertThat(result.pdfFile(), is("/uploads/pdfs/old.pdf"));
    }



    @Test
    void delete_ShouldRemoveResource_WhenExists() {
        Long id = 1L;

        ResourceEntity entity = new ResourceEntity();
        entity.setId(id);
        entity.setImageFile("/uploads/images/delete.png");
        entity.setPdfFile("/uploads/pdfs/delete.pdf");

        when(resourceRepository.findById(id)).thenReturn(Optional.of(entity));

        adminResourceService.delete(id);

        verify(resourceRepository).deleteById(id);
    }

}