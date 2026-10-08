package com.hainam.worksphere.shared.service;

import com.cloudinary.Cloudinary;
import com.hainam.worksphere.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of(
                            "folder", folder,
                            "resource_type", "auto"
                    )
            );
            Object url = result.get("secure_url");
            return url != null ? url.toString() : null;
        } catch (IOException e) {
            throw new ValidationException("Upload file thất bại");
        }
    }
}
