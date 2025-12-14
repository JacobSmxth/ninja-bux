package dev.jsmitty.bux.system.config;

import dev.jsmitty.bux.system.domain.Admin;
import dev.jsmitty.bux.system.domain.Facility;
import dev.jsmitty.bux.system.domain.ShopItem;
import dev.jsmitty.bux.system.repository.AdminRepository;
import dev.jsmitty.bux.system.repository.FacilityRepository;
import dev.jsmitty.bux.system.repository.ShopItemRepository;
import dev.jsmitty.bux.system.service.NinjaService;
import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

  @Bean
  @Profile("!test")
  CommandLineRunner initData(
      FacilityRepository facilityRepository,
      AdminRepository adminRepository,
      ShopItemRepository shopItemRepository,
      NinjaService ninjaService,
      PasswordEncoder passwordEncoder) {
    return args -> {
      // Create a facility
      UUID facilityId = UUID.fromString("fcd4728c-afff-4a3c-8a39-05d2cd9d87ac");
      Facility facility = new Facility(facilityId, "Alpharetta");
      facilityRepository.save(facility);

      UUID otherFacilityId = UUID.fromString("fcd1234c-afff-4a3c-8a39-05d2cd9d87ac");
      Facility otherFacility = new Facility(otherFacilityId, "Vickery");
      facilityRepository.save(otherFacility);

      // Create an admin
      Admin admin = new Admin("admin", passwordEncoder.encode("password"), "admin@example.com");
      admin.addFacility(facility);
      admin.addFacility(otherFacility);
      adminRepository.save(admin);

      // Create some shop items
      shopItemRepository.save(
          new ShopItem(otherFacilityId, "Pizza Zone", "Entry to monthly pizza party", 50));
      shopItemRepository.save(
          new ShopItem(facilityId, "Extend Break Time", "Entry to monthly pizza party", 50));
      shopItemRepository.save(
          new ShopItem(facilityId, "Extra Lab Time", "30 minutes extra coding time", 25));
      shopItemRepository.save(new ShopItem(facilityId, "Candy Bar", "Choice of candy", 10));
      shopItemRepository.save(new ShopItem(facilityId, "Sticker Pack", "5 coding stickers", 15));
      shopItemRepository.save(
          new ShopItem(facilityId, "Code Ninjas T-Shirt", "Official t-shirt", 200));

      // Create some sample ninjas
      ninjaService.createOrUpdateNinja(
          facilityId,
          "53389e2e-c4f5-4e06-96bb-27e3a9427754",
          "Jinu",
          "Kim",
          "Yellow Belt",
          "Level 4",
          "Creating with Repeat and For Element loops!",
          "code-adventure");

      ninjaService.createOrUpdateNinja(
          facilityId,
          "a1b2c3d4-e5f6-4a5b-8c7d-9e0f1a2b3c4d",
          "Jacob",
          "Smith",
          "Orange Belt",
          "Level 2",
          "Building a Calculator",
          "build");

      ninjaService.createOrUpdateNinja(
          facilityId,
          "b2c3d4e5-f6a7-5b6c-9d8e-0f1a2b3c4d5e",
          "Lincoln",
          "Churchill",
          "White Belt",
          "Level 6",
          "Introduction to Variables",
          "code-adventure");

      System.out.println("Sample data initialized!");
      System.out.println("Login with: admin / password");
      System.out.println("Facility ID: " + facilityId);
    };
  }
}
