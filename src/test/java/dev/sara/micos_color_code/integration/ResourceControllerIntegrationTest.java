package dev.sara.micos_color_code.integration;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.sara.micos_color_code.means.ResourceEntity;
import dev.sara.micos_color_code.means.ResourceRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "user", roles = {"USER"})
class ResourceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceRepository resourceRepository;

    @BeforeEach
    void setUp() {
        resourceRepository.deleteAll();
    }

    @Test
    void testGetAllResources_ReturnsList() throws Exception {
        ResourceEntity r1 = ResourceEntity.builder()
            .name("Animalario")
            .intro("Juego para aprender colores")
            .description("Aprende con micos")
            .imageFile("/uploads/images/animalario.jpg")
            .pdfFile("/uploads/pdfs/animalario.pdf")
            .build();

        ResourceEntity r2 = ResourceEntity.builder()
            .name("Sistema")
            .intro("Aprende los coleres")
            .description("Explicación del sistema")
            .imageFile("/uploads/images/sistema.jpg")
            .pdfFile("/uploads/pdfs/sistema.pdf")
            .build();

        resourceRepository.saveAll(List.of(r1, r2));

        mockMvc.perform(get("/users/resources"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].name", is("Animalario")))
            .andExpect(jsonPath("$[1].name", is("Sistema")));
    }

    @Test
    void testGetResourceDetails_ReturnsSingleResource() throws Exception {
        ResourceEntity resource = ResourceEntity.builder()
            .name("Animalario")
            .intro("Juego para aprender colores")
            .description("Aprende con micos")
            .imageFile("/uploads/images/animalario.jpg")
            .pdfFile("/uploads/pdfs/animalario.pdf")
            .build();

        ResourceEntity saved = resourceRepository.save(resource);

        mockMvc.perform(get("/users/resources/users/" + saved.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("Animalario")))
            .andExpect(jsonPath("$.pdfFile", is("/uploads/pdfs/animalario.pdf")));
    }

    @Test
    void testGetAllResources_Empty() throws Exception {
        resourceRepository.deleteAll();

        mockMvc.perform(get("/users/resources"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }
}