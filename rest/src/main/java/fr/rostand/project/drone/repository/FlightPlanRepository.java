package fr.rostand.project.drone.repository;

import fr.rostand.project.drone.model.FlightPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlightPlanRepository extends JpaRepository<FlightPlan, Long> {
    FlightPlan findById(long id);
}
