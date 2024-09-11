package com.saish.movie_ticket.helper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Component
public class CloudinaryHelper {

    @Value("${cloudinary.cloud_name}")
    private String my_cloud_name;
    @Value("${cloudinary.key}")
    private String my_api_key;
    @Value("${cloudinary.secret}")
    private String my_api_secret;

    public String saveMoviePosterToCloud(MultipartFile image) {
        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", my_cloud_name,
                "api_key", my_api_key,
                "api_secret", my_api_secret,
                "secure", true));

        Map<String, Object> resume = null;
        try {
            Map<String, Object> uploadOptions = new HashMap<>();
            uploadOptions.put("folder", "Movie Posters");
            resume = cloudinary.uploader().upload(image.getBytes(), uploadOptions);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (String) resume.get("url");
    }
}