package com.spring.file.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.spring.file.model.FileEntity;
import com.spring.file.model.FileResponse;
import com.spring.file.service.FileService;

@RestController
public class FileController {
	@Autowired
    private FileService fileService;

    @PostMapping("/files")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            fileService.save(file);

            return ResponseEntity.status(HttpStatus.OK)
                                 .body(String.format("File uploaded successfully: %s", file.getOriginalFilename()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(String.format("Could not upload the file: %s!", file.getOriginalFilename()));
        }
    }

    @GetMapping("/files")
    public List<FileResponse> list() {
        return fileService.getAllFiles()
                          .stream()
                          .map(this::mapToFileResponse)
                          .collect(Collectors.toList());
    }

    @GetMapping("/files/{fileName:.+}")
    // /files/06a290064eb94a02a58bfeef36002483.png
    public ResponseEntity<byte[]> readDetailFile(@PathVariable String fileName) {
       try {
           byte[] bytes = fileService.readFileContent(fileName);
           return ResponseEntity
                   .ok()
                   .contentType(MediaType.IMAGE_JPEG)
                   .body(bytes);
       }catch (Exception exception) {
           return ResponseEntity.noContent().build();
       }
    }
    
    private FileResponse mapToFileResponse(FileEntity fileEntity) {
        String downloadURL = ServletUriComponentsBuilder.fromCurrentContextPath()
                                                        .path("/files/")
                                                        .path(fileEntity.getId())
                                                        .toUriString();
        FileResponse fileResponse = new FileResponse();
        fileResponse.setId(fileEntity.getId());
        fileResponse.setName(fileEntity.getName());
        fileResponse.setContentType(fileEntity.getContentType());
        fileResponse.setSize(fileEntity.getSize());
        fileResponse.setUrl(downloadURL);

        return fileResponse;
    }

//    @GetMapping("/files/{id}")
//    public ResponseEntity<byte[]> getFile(@PathVariable String id) {
//        Optional<FileEntity> fileEntityOptional = fileService.getFile(id);
//
//        if (!fileEntityOptional.isPresent()) {
//            return ResponseEntity.notFound()
//                                 .build();
//        }
//
//        FileEntity fileEntity = fileEntityOptional.get();
//        return ResponseEntity.ok()
//                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntity.getName() + "\"")
//                             .contentType(MediaType.valueOf(fileEntity.getContentType()))
//                             .body(fileEntity.getData());
//    }
}