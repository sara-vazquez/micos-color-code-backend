package dev.sara.micos_color_code.means;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/resources")
@RequiredArgsConstructor
public class ResourceController {
    
    private final ResourceService resourceService;

    @GetMapping()
    public ResponseEntity<List<ResourceItemResponseDTO>> getAllResources(){
        List<ResourceItemResponseDTO> resources = resourceService.findAllPublic();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceDetailsResponseDTO> getResourceDetails(@PathVariable Long id) {
        ResourceDetailsResponseDTO details = resourceService.findByIdPublic(id);
        return ResponseEntity.ok(details);
    }
}
