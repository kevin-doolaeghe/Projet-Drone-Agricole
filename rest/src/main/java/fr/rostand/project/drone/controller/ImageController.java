package fr.rostand.project.drone.controller;

import fr.rostand.project.drone.model.FlightPlan;
import fr.rostand.project.drone.model.file.FileStorageService;
import fr.rostand.project.drone.model.Image;
import fr.rostand.project.drone.repository.FlightPlanRepository;
import fr.rostand.project.drone.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ImageController {
    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private FlightPlanRepository flightPlanRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping(value = "/image/info/list")
    public List<Image> getImageList() {
        return imageRepository.findAll();
    }

    @GetMapping(value = "/image/info/{id}")
    public Image getImage(@PathVariable("id") long id) {
        return imageRepository.findById(id);
    }

    @GetMapping(value = "image/info/by-flight-plan/{flightPlanId}")
    public List<Image> getImageListByFlightPlan(@PathVariable("flightPlanId") long flightPlanId) {
        if (flightPlanRepository.existsById(flightPlanId)) {
            FlightPlan flightPlan = flightPlanRepository.findById(flightPlanId);
            return imageRepository.findByFlightPlan(flightPlan);
        }
        return new ArrayList<>();
    }

    @DeleteMapping("/image/delete/by-flight-plan/{flightPlanId}")
    public List<Image> deleteImageByFlightPlan(@PathVariable("flightPlanId") long flightPlanId) {
        List<Image> imageList = getImageListByFlightPlan(flightPlanId);

        for (Image image : imageList) {
            fileStorageService.deleteFile(image.getName());
            imageRepository.delete(image);
        }
        return getImageList();
    }

    @PostMapping("/image/upload/{flightPlanId}")
    public Image uploadImage(@RequestParam("file") MultipartFile file, long number, @PathVariable("flightPlanId") long flightPlanId) {
        if (flightPlanRepository.existsById(flightPlanId)) {
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            Image image = new Image(fileName, number, flightPlanRepository.findById(flightPlanId));

            if (!fileStorageService.isFileExisting(fileName)) {
                imageRepository.save(image);
                fileStorageService.saveFile(file);
                return image;
            }
        }
        return null;
    }

    @GetMapping("/image/download/{fileName:.+}")
    public ResponseEntity<InputStreamResource> downloadImage(@PathVariable("fileName") String fileName, HttpServletRequest request) {
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
}
