package com.spring.file.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.spring.file.model.FileEntity;
import com.spring.file.repository.FileRepository;


@Service
public class FileService {
	@Autowired
    private  FileRepository fileRepository;
	
	private final Path storageFolder = Paths.get("uploads");
	
	public FileService() {
        try {
            Files.createDirectories(storageFolder);
        }catch (IOException exception) {
            throw new RuntimeException("Cannot initialize storage", exception);
        }
    }
	
	private boolean isImageFile(MultipartFile file) {
        //Let install FileNameUtils
        String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        return Arrays.asList(new String[] {"png","jpg","jpeg", "bmp"})
                .contains(fileExtension.trim().toLowerCase());
    }
	
	
    public void save(MultipartFile file) throws IOException {
    	
    	if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file.");
        }
        //check file is image ?
        if(!isImageFile(file)) {
            throw new RuntimeException("You can only upload image file");
        }
        //file must be <= 5Mb
        float fileSizeInMegabytes = file.getSize() / 1_000_000.0f;
        if(fileSizeInMegabytes > 5.0f) {
            throw new RuntimeException("File must be <= 5Mb");
        }
        String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        String generatedFileName = UUID.randomUUID().toString().replace("-", "");
        generatedFileName = generatedFileName+"."+fileExtension;
        
        // save database
        
        FileEntity fileEntity = new FileEntity();
        fileEntity.setId(generatedFileName);
        fileEntity.setName(StringUtils.cleanPath(file.getOriginalFilename()));
        fileEntity.setContentType(file.getContentType());
        fileEntity.setData(file.getBytes());
        fileEntity.setSize(file.getSize());
        fileRepository.save(fileEntity);
        
        // save direct
        
        Path destinationFilePath = this.storageFolder.resolve(
                Paths.get(generatedFileName))
        .normalize().toAbsolutePath();
		if (!destinationFilePath.getParent().equals(this.storageFolder.toAbsolutePath())) {
		    throw new RuntimeException(
		            "Cannot store file outside current directory.");
		}
		try (InputStream inputStream = file.getInputStream()) {
		    Files.copy(inputStream, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
		}
    }

    public Optional<FileEntity> getFile(String id) {
        return fileRepository.findById(id);
    }
    

    public byte[] readFileContent(String fileName) {
        try {
            Path file = storageFolder.resolve(fileName);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                byte[] bytes = StreamUtils.copyToByteArray(resource.getInputStream());
                return bytes;
            }
            else {
                throw new RuntimeException(
                        "Could not read file: " + fileName);
            }
        }
        catch (IOException exception) {
            throw new RuntimeException("Could not read file: " + fileName, exception);
        }
    }
    
    public List<FileEntity> getAllFiles() {
        return fileRepository.findAll();
    }
}