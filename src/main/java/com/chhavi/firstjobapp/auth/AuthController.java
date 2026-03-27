package com.chhavi.firstjobapp.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        if (request.getDateOfBirth() == null)
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, "Date of birth is required.", null));

        User temp = new User();
        temp.setDateOfBirth(request.getDateOfBirth());
        if (temp.getAge() < 18)
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AuthResponse(null, "You must be 18 or older to register.", null));

        if (userRepository.existsByUsername(request.getUsername()))
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new AuthResponse(null, "Username already taken.", null));

        // Default role is ROLE_USER unless ROLE_ADMIN explicitly passed
        String role = "ROLE_ADMIN".equals(request.getRole()) ? "ROLE_ADMIN" : "ROLE_USER";

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDateOfBirth(request.getDateOfBirth());
        user.setRole(role);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername(), role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, "Account created successfully.", role));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword()))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, "Invalid username or password.", null));

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return ResponseEntity.ok(new AuthResponse(token, "Login successful.", user.getRole()));
    }
}