package com.basketball.referee.config;

import com.basketball.referee.entity.User;
import com.basketball.referee.entity.Role;
import com.basketball.referee.repository.UserRepository;
import com.basketball.referee.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Crear roles si no existen
        if (roleRepository.findByName("ROLE_ADMIN") == null) {
            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            roleRepository.save(adminRole);
        }

        if (roleRepository.findByName("ROLE_REFEREE") == null) {
            Role refereeRole = new Role();
            refereeRole.setName("ROLE_REFEREE");
            roleRepository.save(refereeRole);
        }

        // Crear usuario administrador por defecto
        if (!userRepository.findByUsername("admin").isPresent()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@basketball.com");
            admin.setFirstName("Administrador");
            admin.setLastName("Sistema");
            admin.setEnabled(true);
            admin.getRoles().add(roleRepository.findByName("ROLE_ADMIN"));
            userRepository.save(admin);
            System.out.println("Usuario admin creado");
        }

        if (!userRepository.findByUsername("referee").isPresent()) {
            User referee = new User();
            referee.setUsername("referee");
            referee.setPassword(passwordEncoder.encode("referee123"));
            referee.setEmail("referee@basketball.com");
            referee.setFirstName("Juan");
            referee.setLastName("Pérez");
            referee.setEnabled(true);
            referee.getRoles().add(roleRepository.findByName("ROLE_REFEREE"));
            userRepository.save(referee);
            System.out.println("Usuario referee creado");
        }
        System.out.println("listo el comand line runner" );
    }
}
