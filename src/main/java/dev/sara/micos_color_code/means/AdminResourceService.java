package dev.sara.micos_color_code.means;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminResourceService {

    private final ResourceRepository resourceRepository;
    private final ResourceMapper resourceMapper;

    @Value("${upload.path}")
    private String uploadPath;
    
    public ResourceDetailsResponseDTO create(ResourceRequestDTO requestDTO, MultipartFile imageFile, MultipartFile pdfFile) {
        try {
            if (imageFile == null || imageFile.isEmpty()) {
                throw new IllegalArgumentException("La imagen es obligatoria");
            }
            if (pdfFile == null || pdfFile.isEmpty()) {
                throw new IllegalArgumentException("El PDF es obligatorio");
            }
            
            String imagePath = saveFile(imageFile, "images");
            
            String pdfPath = saveFile(pdfFile, "pdfs");
            
            ResourceRequestDTO dtoWithPaths = new ResourceRequestDTO(
                imagePath,
                requestDTO.name(),
                requestDTO.intro(),
                requestDTO.description(),
                pdfPath
            );
            
            ResourceEntity newEntity = resourceMapper.toEntity(dtoWithPaths);
            ResourceEntity savedEntity = resourceRepository.save(newEntity);
            
            return resourceMapper.toDetailsResponseDTO(savedEntity);
            
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar archivos: " + e.getMessage(), e);
        }
    }

    public ResourceDetailsResponseDTO update(Long id, ResourceRequestDTO requestDTO, 
                                        MultipartFile imageFile, MultipartFile pdfFile) {
    try {
        ResourceEntity existingEntity = resourceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado con ID: " + id));

        existingEntity.setName(requestDTO.name());
        existingEntity.setIntro(requestDTO.intro());
        existingEntity.setDescription(requestDTO.description());
        
        if (imageFile != null && !imageFile.isEmpty()) {
            deleteFile(existingEntity.getImageFile());
            String newImagePath = saveFile(imageFile, "images");
            existingEntity.setImageFile(newImagePath);
        } else if (requestDTO.imageFile() != null && !requestDTO.imageFile().isBlank()) {
            existingEntity.setImageFile(requestDTO.imageFile());
        }
        
        if (pdfFile != null && !pdfFile.isEmpty()) {
            deleteFile(existingEntity.getPdfFile());
            String newPdfPath = saveFile(pdfFile, "pdfs");
            existingEntity.setPdfFile(newPdfPath);
        } else if (requestDTO.pdfFile() != null && !requestDTO.pdfFile().isBlank()) {
            existingEntity.setPdfFile(requestDTO.pdfFile());
        }
        
        ResourceEntity updatedEntity = resourceRepository.save(existingEntity);
        return resourceMapper.toDetailsResponseDTO(updatedEntity);
        
    } catch (IOException e) {
        throw new RuntimeException("Error al actualizar archivos: " + e.getMessage(), e);
    }
}
    public void delete(Long id) {
        ResourceEntity entity = resourceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Recurso a borrar no encontrado con ID: " + id));
        
        deleteFile(entity.getImageFile());
        deleteFile(entity.getPdfFile());
        
        resourceRepository.deleteById(id);
    }

    public List<ResourceItemResponseDTO> findAllAdmin() {
        List<ResourceEntity> entities = resourceRepository.findAll();
        return resourceMapper.toListItemDTOs(entities);
    }
    
    // handleFiles 
    
    private String saveFile(MultipartFile file, String folder) throws IOException {
        System.out.println("🔍 Guardando archivo en folder: " + folder);
        System.out.println("🔍 uploadPath: " + uploadPath);

        if (folder.equals("images") && !isValidImage(file)) {
            throw new IllegalArgumentException("El archivo debe ser una imagen válida (jpg, jpeg, png, gif)");
        }
        if (folder.equals("pdfs") && !isValidPdf(file)) {
            throw new IllegalArgumentException("El archivo debe ser un PDF válido");
        }
        
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + extension;
        
        Path uploadDir = Paths.get(uploadPath, folder);
        System.out.println("📂 Directorio completo: " + uploadDir.toAbsolutePath());

        if (!Files.exists(uploadDir)) {
            System.out.println("⚠️ Directorio no existe, creándolo...");
            Files.createDirectories(uploadDir);System.out.println("✅ Directorio creado");
        } else {
            System.out.println("✅ Directorio ya existe");
        }
        
        // Save file in directory
        Path filePath = uploadDir.resolve(fileName);
        System.out.println("💾 Guardando archivo en: " + filePath.toAbsolutePath());

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("✅ Archivo guardado correctamente");

        
        return "/uploads/" + folder + "/" + fileName;
    }
    
    private void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }
        
        try {
            String relativePath = filePath.replace("/uploads/", "");
            Path fileToDelete = Paths.get(uploadPath, relativePath);
            
            if (Files.exists(fileToDelete)) {
                Files.delete(fileToDelete);
            }
        } catch (IOException e) {
            System.err.println("Error al eliminar archivo: " + filePath + " - " + e.getMessage());
        }
    }
    
    private boolean isValidImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
            contentType.equals("image/jpeg") ||
            contentType.equals("image/jpg") ||
            contentType.equals("image/png") ||
            contentType.equals("image/gif")
        );
    }
    
    private boolean isValidPdf(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.equals("application/pdf");
    }
}