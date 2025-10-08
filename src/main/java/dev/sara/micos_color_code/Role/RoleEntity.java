package dev.sara.micos_color_code.Role;

import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_role;

    @Column (unique = true)
    private String username;

    @ManyToMany(mappedBy="roles")
    private Set<UserEntity> users

}
