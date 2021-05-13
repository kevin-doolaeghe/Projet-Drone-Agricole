package fr.rostand.project.drone.repository;

import fr.rostand.project.drone.model.FlightPlan;
import fr.rostand.project.drone.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    Image findById(long id);

    @Query("SELECT img FROM Image img WHERE img.flightPlan = :flightPlan")
    List<Image> findByFlightPlan(@Param("flightPlan") FlightPlan flightPlan);

    @Modifying
    @Transactional
    @Query("DELETE FROM Image img WHERE img.flightPlan = :flightPlan")
    void deleteByFlightPlan(@Param("flightPlan") FlightPlan flightPlan);
}
