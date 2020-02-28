package com.dxc.application.repository;

import com.dxc.application.domain.FlightDetails;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the FlightDetails entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FlightDetailsRepository extends JpaRepository<FlightDetails, Long> {

}
