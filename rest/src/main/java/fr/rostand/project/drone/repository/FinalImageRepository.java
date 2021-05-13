package fr.rostand.project.drone.repository;

import fr.rostand.project.drone.model.FinalImage;
import fr.rostand.project.drone.model.FlightPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface FinalImageRepository extends JpaRepository<FinalImage, Long> {
    FinalImage findById(long id);

    @Query("SELECT img FROM FinalImage img WHERE img.flightPlan = :flightPlan")
    FinalImage findByFlightPlan(@Param("flightPlan") FlightPlan flightPlan);

    @Modifying
    @Transactional
    @Query("DELETE FROM FinalImage img WHERE img.flightPlan = :flightPlan")
    void deleteByFlightPlan(@Param("flightPlan") FlightPlan flightPlan);
}
