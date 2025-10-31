package dev.sara.micos_color_code.integration;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.sara.micos_color_code.means.ResourceEntity;
import dev.sara.micos_color_code.means.ResourceRepository;
import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@WithMockUser(username = "admin", roles = {"ADMIN"})
class AdminResourceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceRepository resourceRepository;

    private MockMultipartFile imageFile;
    private MockMultipartFile pdfFile;

    @BeforeEach
    void setUp() {
        resourceRepository.deleteAll();

        imageFile = new MockMultipartFile(
            "image",
            "test-image.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "fake image content".getBytes()
        );

        pdfFile = new MockMultipartFile(
            "pdf",
            "test-document.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "fake pdf content".getBytes()
        );
    }

    @Test
    void testGetAllAdminResources() throws Exception {
        ResourceEntity resource1 = ResourceEntity.builder()
            .name("Animalario")
            .intro("Pinta los animales según el código")
            .description("Aprende los colores de los animales con Micos")
            .imageFile("/uploads/images/animalario.jpg")
            .pdfFile("/uploads/pdfs/animalario.pdf")
            .build();

        ResourceEntity resource2 = ResourceEntity.builder()
            .name("Sistema")
            .intro("Póster o tríptico")
            .description("Póster explicativo del sistema")
            .imageFile("/uploads/images/sistema.jpg")
            .pdfFile("/uploads/pdfs/sistema.pdf")
            .build();

        resourceRepository.saveAll(List.of(resource1, resource2));

        mockMvc.perform(get("/admin/resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Animalario")))
                .andExpect(jsonPath("$[1].name", is("Sistema")));
    }

    @Test
    void testGetAllAdminResources_EmptyList() throws Exception {
        mockMvc.perform(get("/admin/resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testCreateResource_Success() throws Exception {
        mockMvc.perform(multipart("/admin/resources")
                .file(imageFile)
                .file(pdfFile)
                .param("name", "Nuevo recurso")
                .param("intro", "Intro del recurso")
                .param("description", "Descripción del recurso"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Nuevo recurso")))
                .andExpect(jsonPath("$.intro", is("Intro del recurso")))
                .andExpect(jsonPath("$.description", is("Descripción del recurso")));

        List<ResourceEntity> resources = resourceRepository.findAll();
        assertThat(resources, hasSize(1));
        assertThat(resources.get(0).getName(), equalTo("Nuevo recurso"));
    }

    @Test
    void testUpdateResource_WithNewFiles() throws Exception {
        ResourceEntity existing = resourceRepository.save(ResourceEntity.builder()
            .name("Old")
            .intro("Old intro")
            .description("Old description")
            .imageFile("/uploads/images/old.jpg")
            .pdfFile("/uploads/pdfs/old.pdf")
            .build());

        mockMvc.perform(multipart(HttpMethod.PUT, "/admin/resources/" + existing.getId())
                .file(imageFile)
                .file(pdfFile)
                .param("name", "Actualizado")
                .param("intro", "Intro actualizada")
                .param("description", "Descripción actualizada"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Actualizado")))
                .andExpect(jsonPath("$.intro", is("Intro actualizada")));

        ResourceEntity updated = resourceRepository.findById(existing.getId()).orElseThrow();
        assertThat(updated.getName(), equalTo("Actualizado"));
    }

    @Test
    void testUpdateResource_WithoutNewFiles() throws Exception {
        ResourceEntity existing = resourceRepository.save(ResourceEntity.builder()
            .name("Old")
            .intro("Old intro")
            .description("Old description")
            .imageFile("/uploads/images/old.jpg")
            .pdfFile("/uploads/pdfs/old.pdf")
            .build());

        mockMvc.perform(multipart(HttpMethod.PUT, "/admin/resources/" + existing.getId())
                .param("name", "Actualizado")
                .param("intro", "Intro actualizada")
                .param("description", "Descripción actualizada")
                .param("existingImagePath", "/uploads/images/old.jpg")
                .param("existingPdfPath", "/uploads/pdfs/old.pdf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Actualizado")))
                .andExpect(jsonPath("$.imageFile", is("/uploads/images/old.jpg")))
                .andExpect(jsonPath("$.pdfFile", is("/uploads/pdfs/old.pdf")));
    }

    @Test
    void testDeleteResource_Success() throws Exception {
        ResourceEntity saved = resourceRepository.save(ResourceEntity.builder()
            .name("A borrar")
            .intro("Intro")
            .description("Desc")
            .imageFile("/uploads/images/delete.jpg")
            .pdfFile("/uploads/pdfs/delete.pdf")
            .build());

        mockMvc.perform(delete("/admin/resources/" + saved.getId()))
                .andExpect(status().isNoContent());

        assertThat(resourceRepository.existsById(saved.getId()), is(false));
    }

    @Test
    void testDeleteResource_NotFound() throws Exception {
        mockMvc.perform(delete("/admin/resources/999"))
                .andExpect(status().isNotFound());
    }
}
