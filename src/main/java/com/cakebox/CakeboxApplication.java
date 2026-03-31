package com.cakebox;

// =============================================================
// FILE: CakeboxApplication.java
// LOCATION: src/main/java/com/cakebox/CakeboxApplication.java
//
// WHAT THIS FILE DOES:
// This is the STARTING POINT of your entire backend application.
// When you run the server, Java looks for a class with
// @SpringBootApplication + main() method — this is it.
//
// @SpringBootApplication tells Spring Boot to:
//   1. Auto-configure everything (database, security, etc.)
//   2. Scan all classes in the "com.cakebox" package
//   3. Start an embedded Tomcat web server on port 8080
// =============================================================

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CakeboxApplication {

    public static void main(String[] args) {
        SpringApplication.run(CakeboxApplication.class, args);

        // After this runs, you'll see in the terminal:
        // "Started CakeboxApplication in X.X seconds"
        // Your API is now live at http://localhost:8080
        System.out.println("🎂 CakeBox Backend is running at http://localhost:8080");
        System.out.println("📦 API base URL: http://localhost:8080/api");
    }
}