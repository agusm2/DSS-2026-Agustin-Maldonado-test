package com.cinebuscador.controller;

import com.cinebuscador.model.Pelicula;
import com.cinebuscador.repository.PeliculaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.UUID;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

@Controller
public class PeliculaController {

    private final PeliculaRepository peliculaRepo;

    @Value("${app.upload-dir}")
    private String uploadDir;

    public PeliculaController(PeliculaRepository peliculaRepo) {
        this.peliculaRepo = peliculaRepo;
    }

    private boolean extensionPermitida(String filename) {
        if(filename == null || filename.isBlank()) {
            return false;
        }

        String[] extensionesPermitidas = {".jpg", ".jpeg", ".png"};
        for (String ext : extensionesPermitidas) {
            if (filename.toLowerCase().endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private boolean mimePermitido(MultipartFile archivo) {
        String contentType = archivo.getContentType();

        if(contentType == null) {
            return false;
        }

        String[] mimesPermitidos = {"image/jpeg", "image/png"};
        for (String mime : mimesPermitidos) {
            if (contentType.equalsIgnoreCase(mime)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean contenidoImagenValido(MultipartFile archivo) throws IOException {
        try (ImageInputStream imageStream = 
                ImageIO.createImageInputStream(archivo.getInputStream())) {
            
            if(imageStream == null) {
                return false;
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageStream);

            if(!readers.hasNext()) {
                return false;
            }

            ImageReader reader = readers.next();

            try{
                // Configura el ImageReader para leer la imagen
                reader.setInput(imageStream);
                reader.read(0); // Intenta leer la primera imagen
                return true; // Si no lanza excepción, es una imagen válida
            } catch (IOException e) {
                return false; // No es una imagen válida
            } finally {
                reader.dispose();
            }
        }
    }

    private String generarNombreSeguro(String filename){
        int ultimoPunto = filename.lastIndexOf('.');

        String extension = filename.substring(ultimoPunto).toLowerCase(Locale.ROOT);

        return UUID.randomUUID().toString() + extension;
    }


    @GetMapping("/")
    public String index(@RequestParam(required = false) String buscar,
                        @RequestParam(required = false, defaultValue = "nombre") String ordenarPor,
                        @RequestParam(required = false, defaultValue = "ASC") String sentido,
                        Model model) {

        if (buscar != null && !buscar.isBlank()) {
            List<Object[]> resultadosRaw;
            if ("DESC".equalsIgnoreCase(sentido)) {
                resultadosRaw = peliculaRepo.searchWithFuncionesDesc(buscar, ordenarPor);
            } else {
                resultadosRaw = peliculaRepo.searchWithFunciones(buscar, ordenarPor);
            }

            // Wrap Object[] in Maps for cleaner Thymeleaf access
            List<Map<String, Object>> resultados = new java.util.ArrayList<>();
            for (Object[] row : resultadosRaw) {
                Map<String, Object> map = new HashMap<>();
                map.put("id",        row[0]);
                map.put("nombre",    row[1]);
                map.put("fechaHora", row[2]);
                map.put("disponibles", row[3]);
                map.put("descripcion", row[4]);
                map.put("afichePath", row[5]);
                resultados.add(map);
            }
            model.addAttribute("resultados", resultados);
        }

        model.addAttribute("query", buscar != null ? buscar : "");
        model.addAttribute("sort_by", ordenarPor);
        model.addAttribute("sort_dir", sentido);
        return "index";
    }

    @GetMapping("/upload/{id}")
    public String uploadForm(@PathVariable Integer id, Model model) {
        Pelicula pelicula = peliculaRepo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Pelicula no encontrada"));
        model.addAttribute("pelicula", pelicula);
        return "upload";
    }

    @PostMapping("/upload/{id}")
    public String uploadFile(@PathVariable Integer id,
                             @RequestParam("afiche") MultipartFile archivo) throws IOException {
        Pelicula pelicula = peliculaRepo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Pelicula no encontrada"));

        String filename = archivo.getOriginalFilename();

        if(!extensionPermitida(filename)) {
            throw new IllegalArgumentException("Extensión de archivo no permitida");
        }

        if(!mimePermitido(archivo)) {
            throw new IllegalArgumentException("Tipo MIME no permitido");
        }

        if(!contenidoImagenValido(archivo)) {
            throw new IllegalArgumentException("Contenido del archivo no es una imagen válida");
        }

        String filenameSeguro = generarNombreSeguro(filename);

        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Files.copy(archivo.getInputStream(), uploadPath.resolve(filenameSeguro));

        pelicula.setAfichePath(filenameSeguro);
        peliculaRepo.save(pelicula);

        return "redirect:/";
    }

    @GetMapping("/uploads/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        MediaType mediaType = MediaTypeFactory.getMediaType(resource)
        .orElse(MediaType.APPLICATION_OCTET_STREAM);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
            .contentType(mediaType)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
    }

}