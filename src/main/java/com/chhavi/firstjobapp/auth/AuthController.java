package com.chhavi.firstjobapp.auth;

import com.chhavi.firstjobapp.company.Company;
import com.chhavi.firstjobapp.company.CompanyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CompanyRepository companyRepository;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil,
                          CompanyRepository companyRepository) {
        this.userRepository    = userRepository;
        this.passwordEncoder   = passwordEncoder;
        this.jwtUtil           = jwtUtil;
        this.companyRepository = companyRepository;
    }

    // Get all companies for registration dropdown
    @GetMapping("/companies")
    public ResponseEntity<List<Company>> getCompanies() {
        return ResponseEntity.ok(companyRepository.findAll());
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        if (request.getDateOfBirth() == null)
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, "Date of birth is required.", null, null, null));

        User temp = new User();
        temp.setDateOfBirth(request.getDateOfBirth());
        if (temp.getAge() < 18)
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AuthResponse(null, "You must be 18 or older to register.", null, null, null));

        if (userRepository.existsByUsername(request.getUsername()))
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new AuthResponse(null, "Username already taken.", null, null, null));

        String role = "ROLE_ADMIN".equals(request.getRole()) ? "ROLE_ADMIN" : "ROLE_USER";

        // Link admin to company
        Company company = null;
        if ("ROLE_ADMIN".equals(role) && request.getCompanyId() != null) {
            company = companyRepository.findById(request.getCompanyId()).orElse(null);
            if (company == null)
                return ResponseEntity.badRequest()
                        .body(new AuthResponse(null, "Selected company not found.", null, null, null));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDateOfBirth(request.getDateOfBirth());
        user.setRole(role);
        user.setCompany(company);
        userRepository.save(user);

        Long   companyId   = company != null ? company.getId()   : null;
        String companyName = company != null ? company.getName() : null;
        String token = jwtUtil.generateToken(user.getUsername(), role, companyId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, "Account created successfully.", role, companyId, companyName));
    }

    // Add this endpoint inside AuthController
    @PostMapping("/companies/create")
    public ResponseEntity<Company> createCompany(@RequestBody Company company) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(companyRepository.save(company));
    }
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword()))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, "Invalid username or password.", null, null, null));

        Long   companyId   = user.getCompany() != null ? user.getCompany().getId()   : null;
        String companyName = user.getCompany() != null ? user.getCompany().getName() : null;
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole(), companyId);

        return ResponseEntity.ok(
                new AuthResponse(token, "Login successful.", user.getRole(), companyId, companyName));
    }
}