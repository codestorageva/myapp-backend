package com.vaistra.service;

import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface CloudinaryImageService {

    public Map upload(MultipartFile file, Map<String, String> headers);
}
