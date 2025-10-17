package dev.sara.micos_color_code.means;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
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

    @PostMapping
    public ResponseEntity<ResourceDetailsResponseDTO> createResource(
        @RequestBody @Valid ResourceRequestDTO requestDTO) {
        
        ResourceDetailsResponseDTO createdResource = adminResourceService.create(requestDTO);
        return new ResponseEntity<>(createdResource, HttpStatus.CREATED); 
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResourceDetailsResponseDTO> updateResource(
        @PathVariable Long id,
        @RequestBody @Valid ResourceRequestDTO requestDTO) {

        ResourceDetailsResponseDTO updatedResource = adminResourceService.update(id, requestDTO);
        return ResponseEntity.ok(updatedResource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        adminResourceService.delete(id);
        
        return ResponseEntity.noContent().build();
    }
    
}
