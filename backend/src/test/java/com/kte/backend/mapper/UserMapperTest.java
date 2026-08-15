package com.kte.backend.mapper;

import com.kte.backend.models.dto.request.RegisterRequest;
import com.kte.backend.models.dto.response.UserResponse;
import com.kte.backend.models.entity.User;
import com.kte.backend.models.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("UserMapper Test")
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    @DisplayName("Test entity to DTO mapping")
    void entity_To_Dto() {
        //Given
        User user = User.builder()
                .id("1L")
                .username("John Doe")
                .email("john.doe@example.com")
                .password("secret")
                .phoneNumber("0600000000")
                .role(UserRole.ADMIN)
                .build();

        //When
        UserResponse dto = userMapper.entityToDto(user);

        //Then
        assertNotNull(dto);
        assertEquals("John Doe", dto.username());
        assertEquals("john.doe@example.com", dto.email());
        assertEquals("0600000000", dto.phoneNumber());
        assertEquals(UserRole.ADMIN, dto.role());
    }

    @Test
    @DisplayName("Test DTO to entity mapping")
    void dto_To_Entity() {
        //Given
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("John Doe")
                .email("john.doe@example.com")
                .password("secret")
                .phoneNumber("0600000000")
                .role(UserRole.ADMIN)
                .build();

        //When
        User entity = userMapper.dtoToEntity(registerRequest);

        //Then
        assertNotNull(entity);

    }

    @Test
    @DisplayName("Test list of entities to list of DTOs mapping")
    void to_Dto_List() {
        //Given
        User user1 = User.builder()
                .username("John Doe")
                .email("john.doe@example.com")
                .role(UserRole.ADMIN)
                .build();

        User user2 = User.builder()
                .username("Jane Smith")
                .email("jane.smith@example.com")
                .role(UserRole.MANAGER)
                .build();

        //When
        List<UserResponse> dtoList = userMapper.toDtoList(List.of(user1, user2));

        //Then
        assertNotNull(dtoList);

    }

    @Test
    @DisplayName("Test updating entity from DTO")
    void update_Entity_From_Dto() {
        //Given
        User user = User.builder()
                .id("1L")
                .username("John Doe")
                .email("john.doe@example.com")
                .role(UserRole.ADMIN)
                .build();

        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("John Updated")
                .email("john.updated@example.com")
                .password("newSecret")
                .phoneNumber("0611111111")
                .role(UserRole.MANAGER)
                .build();

        //When
        userMapper.updateEntityFromDto(registerRequest, user);

        //Then
        assertEquals("John Updated", user.getUsername());
        assertEquals("john.updated@example.com", user.getEmail());
        assertEquals("newSecret", user.getPassword());
        assertEquals("0611111111", user.getPhoneNumber());
        assertEquals(UserRole.MANAGER, user.getRole());
        assertEquals("1L", user.getId());
    }
}