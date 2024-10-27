package com.bbinnick.gamestack.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.annotation.PostConstruct;

@Service
public class ImageService {

	private final Path rootLocation = Paths.get("uploads");

	@PostConstruct
	public void init() {
		try {
			if (!Files.exists(rootLocation)) {
				Files.createDirectories(rootLocation);
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not initialize storage directory", e);
		}
	}

	public String saveImage(MultipartFile file) throws IOException {
		if (file == null || file.isEmpty()) {
			return null;
		}

		String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
		Path targetLocation = this.rootLocation.resolve(filename);

		try {
			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
			return filename;
		} catch (IOException e) {
			throw new IOException("Failed to store image file " + filename, e);
		}
	}

	public Path loadImage(String filename) throws NoResourceFoundException {
		Path imagePath = rootLocation.resolve(filename);
		if (Files.exists(imagePath)) {
			return imagePath;
		} else {
			throw new NoResourceFoundException(null, "Resource not found for " + filename);
		}
	}
}
