package com.goon.rideservice.repository;

import com.goon.rideservice.entity.RideOffer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RideOfferRepository extends JpaRepository<RideOffer, UUID> {
}
