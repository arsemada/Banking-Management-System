package com.example.bankingmanagement;

import com.example.bankingmanagement.model.ERole;
import com.example.bankingmanagement.model.Role;
import com.example.bankingmanagement.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * DataLoader class to populate initial roles into the database on application startup.
 * This ensures that roles like ROLE_CUSTOMER, ROLE_STAFF, and ROLE_ADMIN are always available.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    private final RoleRepository roleRepository;

    // Spring automatically injects RoleRepository
    public DataLoader(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Checking and populating roles...");

        // Check and save ROLE_CUSTOMER
        if (roleRepository.findByName(ERole.ROLE_CUSTOMER).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_CUSTOMER));
            logger.info("Added ROLE_CUSTOMER to database.");
        }

        // Check and save ROLE_STAFF
        if (roleRepository.findByName(ERole.ROLE_STAFF).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_STAFF));
            logger.info("Added ROLE_STAFF to database.");
        }

        // Check and save ROLE_ADMIN
        if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_ADMIN));
            logger.info("Added ROLE_ADMIN to database.");
        }

        logger.info("Role check complete.");
    }
}