package fr.rostand.project.drone.controller;

import fr.rostand.project.drone.model.FinalImage;
import fr.rostand.project.drone.model.Image;
import fr.rostand.project.drone.model.synthesis.ImageEditor;
import fr.rostand.project.drone.model.FlightPlan;
import fr.rostand.project.drone.model.file.FileStorageService;
import fr.rostand.project.drone.repository.FinalImageRepository;
import fr.rostand.project.drone.repository.FlightPlanRepository;
import fr.rostand.project.drone.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class FlightPlanController {
    @Autowired
    private FlightPlanRepository flightPlanRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private FinalImageRepository finalImageRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping(value = "/plan/list")
    public List<FlightPlan> getFlightPlanList() {
        return flightPlanRepository.findAll();
    }

    @GetMapping(value = "/plan/{id}")
    public FlightPlan getFlightPlan(@PathVariable("id") long id) {
        return flightPlanRepository.findById(id);
    }

    @PostMapping(value = "/plan/add")
    public List<FlightPlan> addFlightPlan(@Valid FlightPlan flightPlan) {
        flightPlanRepository.save(flightPlan);
        return getFlightPlanList();
    }

    @PutMapping(value = "/plan/update/{id}")
    public List<FlightPlan> updateFlightPlan(@Valid FlightPlan flightPlan, @PathVariable("id") long id) {
        if (flightPlanRepository.existsById(id)) {
            flightPlanRepository.save(flightPlan);
        }
        return getFlightPlanList();
    }

    @DeleteMapping(value = "/plan/delete/{id}")
    public List<FlightPlan> deleteFlightPlan(@PathVariable("id") long id) {
        if (flightPlanRepository.existsById(id)) {
            FlightPlan flightPlan = flightPlanRepository.findById(id);
            System.err.println(flightPlan.getId());

            try {
                List<Image> imageList = imageRepository.findByFlightPlan(flightPlan);
                for (Image image : imageList) {
                    fileStorageService.deleteFile(image.getName());
                }

                FinalImage finalImage = finalImageRepository.findByFlightPlan(flightPlan);
                if (finalImage != null) {
                    fileStorageService.deleteFile(finalImage.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            imageRepository.deleteByFlightPlan(flightPlan);
            finalImageRepository.deleteByFlightPlan(flightPlan);
            flightPlanRepository.delete(flightPlan);
        }
        return getFlightPlanList();
    }

    @PutMapping(value = "/plan/add-data/{id}")
    public List<FlightPlan> addDataToFlightPlan(long latImgNb, long lonImgNb, @PathVariable("id") long id) {
        if (flightPlanRepository.existsById(id)) {
            FlightPlan flightPlan = flightPlanRepository.findById(id);
            flightPlan.setLatImgNb(latImgNb);
            flightPlan.setLonImgNb(lonImgNb);

            flightPlanRepository.save(flightPlan);
        }
        return getFlightPlanList();
    }

    @GetMapping(value = "/plan/start-analysis/{id}")
    public ResponseEntity startAnalysis(@PathVariable("id") long id) {
        if (flightPlanRepository.existsById(id)) {
            new Thread(() -> new ImageEditor(id).makeAnalysis()).start();
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
