package dev.sara.micos_color_code.means;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="resources")
public class ResourceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="image", nullable=false)
    private String image;

    @Column(name="name", nullable=false)
    private String name;

    @Column(name="intro", nullable =false)
    private String intro;

    @Column(name="description", nullable=false)
    private String description;

    @Column(name="pdf", nullable=false, unique = true)
    private String pdf;
    
}
