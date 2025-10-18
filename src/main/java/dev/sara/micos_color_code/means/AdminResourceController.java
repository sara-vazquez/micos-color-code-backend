package dev.sara.micos_color_code.means;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/resources")
@RequiredArgsConstructor
public class AdminResourceController {

    private final AdminResourceService adminResourceService;

    @GetMapping
    public ResponseEntity<List<ResourceItemResponseDTO>> getAllAdminResources() {
        List<ResourceItemResponseDTO> resources = adminResourceService.findAllAdmin();
        return ResponseEntity.ok(resources);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResourceDetailsResponseDTO> createResource(
    @RequestParam("name") String name,
    @RequestParam("intro") String intro,
    @RequestParam("description") String description,
    @RequestParam("image") MultipartFile imageFile,

    @RequestParam("pdf") MultipartFile pdfFile) {
    
    ResourceRequestDTO requestDTO = new ResourceRequestDTO(
        null, // path generated in service
        name,
        intro,
        description,
        null  // path generated in service
    );
    
    ResourceDetailsResponseDTO createdResource = adminResourceService.create(requestDTO, imageFile, pdfFile);
    return new ResponseEntity<>(createdResource, HttpStatus.CREATED); 
}

@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<ResourceDetailsResponseDTO> updateResource(
    @PathVariable Long id,
    @RequestParam("name") String name,
    @RequestParam("intro") String intro,
    @RequestParam("description") String description,
    @RequestParam(value = "image", required = false) MultipartFile imageFile,
    @RequestParam(value = "pdf", required = false) MultipartFile pdfFile) {

    ResourceRequestDTO requestDTO = new ResourceRequestDTO(
        null,
        name,
        intro,
        description,
        null 
    );

    ResourceDetailsResponseDTO updatedResource = adminResourceService.update(id, requestDTO, imageFile, pdfFile);
    return ResponseEntity.ok(updatedResource);
}

@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
    adminResourceService.delete(id);
    return ResponseEntity.noContent().build();
}
    
}
