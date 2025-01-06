package uz.pdp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import uz.pdp.config.filtr.JwtProvider;
import uz.pdp.dto.SignInRequest;
import uz.pdp.dto.SignUpRequest;
import uz.pdp.entity.User;
import uz.pdp.repository.UserRepository;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSignIn_Success() {
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setName("testuser");
        signInRequest.setPassword("password");

        User user = new User();
        user.setName("testuser");
        user.setPassword("encodedPassword");

        when(userRepository.findByName("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtProvider.generateToken("testuser")).thenReturn("jwtToken");

        ResponseEntity<?> response = authService.signIn(signInRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Successfully signed in", ((Map<String, String>) response.getBody()).get("message"));
        assertEquals("jwtToken", ((Map<String, String>) response.getBody()).get("token"));
    }

    @Test
    public void testSignUp_Success() {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setName("newuser");
        signUpRequest.setLastname("lastname");
        signUpRequest.setEmail("newuser@example.com");
        signUpRequest.setPassword("password");

        when(userRepository.findByName("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(jwtProvider.generateToken("newuser")).thenReturn("jwtToken");

        ResponseEntity<?> response = authService.signUp(signUpRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("User registered successfully", ((Map<String, String>) response.getBody()).get("message"));
        assertEquals("jwtToken", ((Map<String, String>) response.getBody()).get("token"));
    }
}
