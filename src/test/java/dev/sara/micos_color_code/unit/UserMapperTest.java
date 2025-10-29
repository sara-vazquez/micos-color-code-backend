package dev.sara.micos_color_code.unit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not; 
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import dev.sara.micos_color_code.user.UserEntity;
import dev.sara.micos_color_code.user.UserMapper;
import dev.sara.micos_color_code.user.UserRequestDTO;
import dev.sara.micos_color_code.user.UserResponseDTO;

public class UserMapperTest {
    
    private UserMapper userMapper;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @Test
    void testToEntity() {
        UserRequestDTO dto = new UserRequestDTO("palomacuesta", "paloma@puf.com", "palomaUrbanFashion123", "c126793", 21);
        UserEntity entity = userMapper.toEntity(dto);

        assertThat(entity,is(notNullValue()));
        assertThat(entity.getUsername(),is(equalTo("palomacuesta")));
        assertThat(entity.getEmail(),is(equalTo("paloma@puf.com")));
        assertThat(entity.getPassword(), is(notNullValue()));
        assertThat(entity.getPassword(),is(not(equalTo("palomaUrbanFashion123"))));
    }

    @Test
    void testToResponse() {
        UserEntity entity = UserEntity.builder()
            .id(1L)
            .username("juancuesta")
            .email("jcuesta21@gmail.com")
            .password("presidenteComunidad123")
            .enabled(true)
            .build();

        UserResponseDTO response = userMapper.toResponse(entity);

        assertThat(response, is(notNullValue()));
        assertThat(response.id(), is(equalTo(1L)));
        assertThat(response.username(), is(equalTo("juancuesta")));
        assertThat(response.email(), is(equalTo("jcuesta21@gmail.com")));
    }

    @Test
    void testCompleteMapping() {
        UserRequestDTO dto = new UserRequestDTO("belenlopez", "belenlv@gmail.com","CompletePass123!", "c126793", 21);

        UserEntity entity = userMapper.toEntity(dto);

        assertThat(entity, is(notNullValue()));
        assertThat(entity.getId(), is(nullValue()));
        assertThat(entity.getUsername(), is(equalTo("belenlopez")));
        assertThat(entity.getEmail(), is(equalTo("belenlv@gmail.com")));
        assertThat(entity.getPassword(), is(notNullValue()));
        assertThat(passwordEncoder.matches("CompletePass123!", entity.getPassword()), is(true));
        assertThat(entity.isEnabled(), is(false));
    }
}
