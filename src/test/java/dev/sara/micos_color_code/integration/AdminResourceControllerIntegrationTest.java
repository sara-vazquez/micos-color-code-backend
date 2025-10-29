package dev.sara.micos_color_code.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import dev.sara.micos_color_code.means.ResourceEntity;
import dev.sara.micos_color_code.means.ResourceRepository;
import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class AdminResourceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceRepository resourceRepository;

    @Value("${upload.path}")
    private String uploadPath;

    private MockMultipartFile imageFile;
    private MockMultipartFile pdfFile;
    private Path testUploadDir;

    @BeforeEach
    void setUp() throws Exception {
        resourceRepository.deleteAll();

        testUploadDir = Paths.get(uploadPath);
        if (!Files.exists(testUploadDir)) {
            Files.createDirectories(testUploadDir);
        }

        imageFile = new MockMultipartFile(
            "image",
            "test-image.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );

        pdfFile = new MockMultipartFile(
            "pdf",
            "test-document.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "test pdf content".getBytes()
        );
    }

    @Test
    void testGetAllAdminResources() throws Exception {
        ResourceEntity resource1 = ResourceEntity.builder()
            .name("Animalario")
            .intro("Pinta los animales según el código")
            .description("Aprende a identificar los colores de los animales con micos")
            .imageFile("/uploads/images/animalario.jpg")
            .pdfFile("/uploads/pdfs/animalario.pdf")
            .build();

        ResourceEntity resource2 = ResourceEntity.builder()
            .name("Sistema")
            .intro("Póster o trípitco")
            .description("Póster explicativo del sistema")
            .imageFile("/uploads/images/sistema.jpg")
            .pdfFile("/uploads/pdfs/sistema.pdf")
            .build();

        resourceRepository.saveAll(List.of(resource1, resource2));

        mockMvc.perform(get("/admin/resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Resource 1")))
                .andExpect(jsonPath("$[1].name", is("Resource 2")));
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
                .param("name", "New Resource")
                .param("intro", "Resource intro")
                .param("description", "Resource description"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("New Resource")))
                .andExpect(jsonPath("$.intro", is("Resource intro")))
                .andExpect(jsonPath("$.description", is("Resource description")))
                .andExpect(jsonPath("$.imageFile", notNullValue()))
                .andExpect(jsonPath("$.pdfFile", notNullValue()));

                List<ResourceEntity> resources = resourceRepository.findAll();
                assertThat(resources.size(), is(equalTo(1)));
                assertThat(resources.get(0).getName(), is(equalTo("New Resource")));
                
                String imagePath = resources.get(0).getImageFile();
                String pdfPath = resources.get(0).getPdfFile();
                assertThat(imagePath, notNullValue());
                assertThat(pdfPath, notNullValue());
    }

    @Test
    void testUpdateResource_WithNewFiles() throws Exception {
        ResourceEntity existingResource = ResourceEntity.builder()
            .name("Old Resource")
            .intro("Old intro")
            .description("Old description")
            .imageFile("/uploads/images/old-image.jpg")
            .pdfFile("/uploads/pdfs/old-document.pdf")
            .build();
        ResourceEntity saved = resourceRepository.save(existingResource);

        mockMvc.perform(multipart(HttpMethod.PUT, "/admin/resources/" + saved.getId())
                .file(imageFile)
                .file(pdfFile)
                .param("name", "Updated Resource")
                .param("intro", "Updated intro")
                .param("description", "Updated description"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Resource")))
                .andExpect(jsonPath("$.intro", is("Updated intro")))
                .andExpect(jsonPath("$.description", is("Updated description")));

        ResourceEntity updated = resourceRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getName(), is(equalTo("Updated Resource")));
        assertThat(updated.getIntro(), is(equalTo("Updated intro")));
        
        assertThat(updated.getImageFile(), is(notNullValue()));
        assertThat(updated.getPdfFile(), is(notNullValue()));
    }

    @Test
    void testUpdateResource_WithoutNewFiles() throws Exception {
            ResourceEntity existingResource = ResourceEntity.builder()
                .name("Old Resource")
                .intro("Old intro")
                .description("Old description")
                .imageFile("/uploads/images/old-image.jpg")
                .pdfFile("/uploads/pdfs/old-document.pdf")
                .build();
            ResourceEntity saved = resourceRepository.save(existingResource);
    
            mockMvc.perform(multipart(HttpMethod.PUT, "/admin/resources/" + saved.getId())
                    .param("name", "Updated Resource")
                    .param("intro", "Updated intro")
                    .param("description", "Updated description")
                    .param("existingImagePath", "/uploads/images/old-image.jpg")
                    .param("existingPdfPath", "/uploads/pdfs/old-document.pdf"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Updated Resource")))
                    .andExpect(jsonPath("$.imageFile", is("/uploads/images/old-image.jpg")))
                    .andExpect(jsonPath("$.pdfFile", is("/uploads/pdfs/old-document.pdf")));

        ResourceEntity updated = resourceRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getName(), is(equalTo("Updated Resource")));
        assertThat(updated.getImageFile(), is(equalTo("/uploads/images/old-image.jpg")));
        assertThat(updated.getPdfFile(), is(equalTo("/uploads/pdfs/old-document.pdf")));
    }
    

    @Test
    void testDeleteResource_Success() throws Exception {
        ResourceEntity resource = ResourceEntity.builder()
        .name("Resource to delete")
        .intro("Intro")
        .description("Description")
        .imageFile("/uploads/images/delete-me.jpg")
        .pdfFile("/uploads/pdfs/delete-me.pdf")
        .build();
        ResourceEntity saved = resourceRepository.save(resource);

        mockMvc.perform(delete("/admin/resources/" + saved.getId()))
            .andExpect(status().isNoContent());

        assertThat(resourceRepository.findById(saved.getId()).isPresent(), is(false));
    }

    @Test
    void testDeleteResource_NotFound() throws Exception {
        mockMvc.perform(delete("/admin/resources/999"))
                .andExpect(status().isNotFound());
    }

    @AfterEach
    void tearDown() throws Exception {
        resourceRepository.deleteAll();
        
        deleteDirectory(new File(uploadPath));
    }

    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
    }
}