package dev.sara.micos_color_code.means;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface  ResourceRepository extends JpaRepository<ResourceEntity, Long> {
    List<ResourceEntity> findByName(String name);
    Optional<ResourceEntity> findById(Long id);
}
