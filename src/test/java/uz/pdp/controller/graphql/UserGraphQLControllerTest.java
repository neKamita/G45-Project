package uz.pdp.controller.graphql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import uz.pdp.entity.User;
import uz.pdp.enums.Role;
import uz.pdp.payload.EntityResponse;
import uz.pdp.service.UserService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for UserGraphQLController
 */
class UserGraphQLControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private UserGraphQLController userGraphQLController;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.USER);

        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void currentUser_ReturnsCurrentUser() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(testUser);

        // Act
        User result = userGraphQLController.currentUser();

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getName(), result.getName());
        verify(userService).getCurrentUser();
    }

    @Test
    void users_ReturnsAllUsers() {
        // Arrange
        List<User> userList = Arrays.asList(testUser);
        when(userService.getAllUsers()).thenReturn(EntityResponse.success("Users retrieved", userList));

        // Act
        EntityResponse<List<User>> result = userGraphQLController.users();

        // Assert
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        verify(userService).getAllUsers();
    }

    @Test
    void getUserById_ReturnsUser() {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(EntityResponse.success("User found", testUser));

        // Act
        User result = userGraphQLController.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        verify(userService).getUserById(1L);
    }

    @Test
    void requestSeller_Success() {
        // Arrange
        when(userService.requestSeller(1L)).thenReturn(EntityResponse.success("Request submitted", testUser));

        // Act
        User result = userGraphQLController.requestSeller(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        verify(userService).requestSeller(1L);
    }
}
