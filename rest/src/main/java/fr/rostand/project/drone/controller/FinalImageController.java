package fr.rostand.project.drone.controller;

import fr.rostand.project.drone.model.FlightPlan;
import fr.rostand.project.drone.model.file.FileStorageService;
import fr.rostand.project.drone.model.FinalImage;
import fr.rostand.project.drone.repository.FinalImageRepository;
import fr.rostand.project.drone.repository.FlightPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.List;

@RestController
public class FinalImageController {
    @Autowired
    private FinalImageRepository finalImageRepository;

    @Autowired
    private FlightPlanRepository flightPlanRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping(value = "/final-image/info/list")
    public List<FinalImage> getFinalImageList() {
        return finalImageRepository.findAll();
    }

    @GetMapping(value = "/final-image/info/{id}")
    public FinalImage getFinalImage(@PathVariable("id") long id) {
        return finalImageRepository.findById(id);
    }

    @GetMapping(value = "/final-image/info/by-flight-plan/{flightPlanId}")
    public FinalImage getFinalImageByFlightPlan(@PathVariable("flightPlanId") long flightPlanId) {
        if (flightPlanRepository.existsById(flightPlanId)) {
            FlightPlan flightPlan = flightPlanRepository.findById(flightPlanId);
            return finalImageRepository.findByFlightPlan(flightPlan);
        }
        return null;
    }

    @DeleteMapping("/final-image/delete/by-flight-plan/{flightPlanId}")
    public List<FinalImage> deleteFinalImageFromPlan(@PathVariable("flightPlanId") long flightPlanId) {
        if (flightPlanRepository.existsById(flightPlanId)) {
            FlightPlan flightPlan = flightPlanRepository.findById(flightPlanId);
            FinalImage finalImage = finalImageRepository.findByFlightPlan(flightPlan);
            fileStorageService.deleteFile(finalImage.getName());
            finalImageRepository.deleteByFlightPlan(flightPlan);
        }
        return getFinalImageList();
    }

    @PostMapping("/final-image/upload/{flightPlanId}")
    public FinalImage uploadFinalImage(@RequestParam("file") MultipartFile file, @PathVariable("flightPlanId") long flightPlanId) {
        if (flightPlanRepository.existsById(flightPlanId)) {
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            FinalImage finalImage = new FinalImage(fileName, flightPlanRepository.findById(flightPlanId));

            if (!fileStorageService.isFileExisting(fileName)) {
                finalImageRepository.save(finalImage);
                fileStorageService.saveFile(file);
                return finalImage;
            }
        }
        return null;
    }

    @GetMapping("/final-image/download/by-file-name/{fileName:.+}")
    public ResponseEntity<InputStreamResource> downloadFinalImageByFileName(@PathVariable("fileName") String fileName, HttpServletRequest request) {
        File file = fileStorageService.getFile(fileName);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        InputStreamResource image = null;
        try {
            image = new InputStreamResource(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(file.length())
                .body(image);
    }

    @GetMapping("/final-image/download/by-flight-plan/{flightPlanId}")
    public ResponseEntity<InputStreamResource> downloadFinalImageByFlightPlan(@PathVariable("flightPlanId") long flightPlanId, HttpServletRequest request) {
        String fileName = finalImageRepository.findByFlightPlan(flightPlanRepository.findById(flightPlanId)).getName();
        return downloadFinalImageByFileName(fileName, request);
    }
}
