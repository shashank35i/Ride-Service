package com.goon.rideservice.service;

import com.goon.rideservice.repository.DriverAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DriverAssignmentInitializer implements ApplicationRunner {

    private final DriverAssignmentRepository driverAssignmentRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        driverAssignmentRepository.seedCurrentAssignments();
    }
}
