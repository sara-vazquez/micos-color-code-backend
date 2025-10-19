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
            System.out.println("=== CREATE - INICIO ===");
            System.out.println("DTO recibido - name: [" + requestDTO.name() + "]");
            System.out.println("DTO recibido - intro: [" + requestDTO.intro() + "]");
            System.out.println("DTO recibido - description: [" + requestDTO.description() + "]");
            System.out.println("DTO recibido - imageFile: [" + requestDTO.imageFile() + "]");
            System.out.println("DTO recibido - pdfFile: [" + requestDTO.pdfFile() + "]");
            
            if (imageFile == null || imageFile.isEmpty()) {
                throw new IllegalArgumentException("La imagen es obligatoria");
            }
            if (pdfFile == null || pdfFile.isEmpty()) {
                throw new IllegalArgumentException("El PDF es obligatorio");
            }
            
            System.out.println("✅ Archivos validados");
            
            String imagePath = saveFile(imageFile, "images");
            System.out.println("✅ Imagen guardada en: " + imagePath);
            
            String pdfPath = saveFile(pdfFile, "pdfs");
            System.out.println("✅ PDF guardado en: " + pdfPath);
            
            ResourceRequestDTO dtoWithPaths = new ResourceRequestDTO(
                requestDTO.name(),        
                requestDTO.intro(),       
                requestDTO.description(), 
                imagePath,                
                pdfPath                  
            );
            
            System.out.println("=== DTO CON PATHS CREADO ===");
            System.out.println("name: [" + dtoWithPaths.name() + "]");
            System.out.println("intro: [" + dtoWithPaths.intro() + "]");
            System.out.println("description: [" + dtoWithPaths.description() + "]");
            System.out.println("imageFile: [" + dtoWithPaths.imageFile() + "]");
            System.out.println("pdfFile: [" + dtoWithPaths.pdfFile() + "]");
            
            System.out.println("🔄 Mapeando a Entity...");
            ResourceEntity newEntity = resourceMapper.toEntity(dtoWithPaths);
            
            System.out.println("=== ENTITY MAPEADA ===");
            System.out.println("name: [" + newEntity.getName() + "]");
            System.out.println("intro: [" + newEntity.getIntro() + "]");
            System.out.println("description: [" + newEntity.getDescription() + "]");
            System.out.println("imageFile: [" + newEntity.getImageFile() + "]");
            System.out.println("pdfFile: [" + newEntity.getPdfFile() + "]");
            
            System.out.println("💾 Guardando en BD...");
            ResourceEntity savedEntity = resourceRepository.save(newEntity);
            System.out.println("✅ Guardado con ID: " + savedEntity.getId());
            
            return resourceMapper.toDetailsResponseDTO(savedEntity);
            
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar archivos: " + e.getMessage(), e);
        }
    }

    public ResourceDetailsResponseDTO update(Long id, ResourceRequestDTO requestDTO, MultipartFile imageFile, MultipartFile pdfFile) {
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
    
    
    private String saveFile(MultipartFile file, String folder) throws IOException {
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

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        Path filePath = uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
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