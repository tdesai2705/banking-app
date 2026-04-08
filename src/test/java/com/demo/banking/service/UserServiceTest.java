package com.demo.banking.service;

import com.demo.banking.model.User;
import com.demo.banking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("johndoe");
        testUser.setEmail("john@example.com");
        testUser.setPassword("hashedPassword123");
        testUser.setFullName("John Doe");
    }

    @Test
    void testCreateUser_Success() {
        // Arrange
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User createdUser = userService.createUser("johndoe", "john@example.com", "password123", "John Doe");

        // Assert
        assertNotNull(createdUser);
        assertEquals("johndoe", createdUser.getUsername());
        assertEquals("john@example.com", createdUser.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUser_UsernameAlreadyExists() {
        // Arrange
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser("johndoe", "john@example.com", "password123", "John Doe");
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCreateUser_EmailAlreadyExists() {
        // Arrange
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser("johndoe", "john@example.com", "password123", "John Doe");
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetUserById_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        User user = userService.getUserById(1L);

        // Assert
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("johndoe", user.getUsername());
    }

    @Test
    void testGetUserById_NotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserById(1L);
        });
    }

    @Test
    void testGetUserByUsername_Success() {
        // Arrange
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));

        // Act
        User user = userService.getUserByUsername("johndoe");

        // Assert
        assertNotNull(user);
        assertEquals("johndoe", user.getUsername());
    }

    @Test
    void testGetUserByUsername_NotFound() {
        // Arrange
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserByUsername("johndoe");
        });
    }

    @Test
    void testUpdateUser_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User updatedUser = userService.updateUser(1L, "newemail@example.com", "Jane Doe");

        // Assert
        assertNotNull(updatedUser);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testDeleteUser_Success() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteUser_NotFound() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.deleteUser(1L);
        });

        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void testGetAllUsers_Success() {
        // Arrange
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("janedoe");
        user2.setEmail("jane@example.com");

        List<User> users = Arrays.asList(testUser, user2);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("johndoe", result.get(0).getUsername());
        assertEquals("janedoe", result.get(1).getUsername());
    }
}
