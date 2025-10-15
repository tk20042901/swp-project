package com.swp.project.service.product;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ImageService {
    private static final String IMAGES_TEMPORARY_PATH = "src/main/resources/static/images/temporary-products/";
    private static final String DISPLAY_TEMPORARY_PATH = "/images/temporary-products/";
    private static final String IMAGES_FINAL_PATH = "src/main/resources/static/images/products/";
    private static final String DISPLAY_FINAL_PATH = "/images/products/";

    /**
     * Recursively deletes a directory and all its contents.
     * This method safely handles the deletion of directories by first deleting all files
     * and subdirectories before deleting the parent directory.
     * 
     * @param directory The Path of the directory to delete
     */
    private void deleteDirectory(Path directory) {
        try {
            if (Files.exists(directory)) {
                System.out.println("Starting to delete directory: " + directory);
                Files.walk(directory)
                        .sorted((a, b) -> b.compareTo(a)) // Sort in reverse order to delete files before directories
                        .forEach(path -> {
                            try {
                                System.out.println("Deleting: " + path);
                                Files.deleteIfExists(path);
                            } catch (Exception e) {
                                System.err.println("Failed to delete: " + path + " - " + e.getMessage());
                            }
                        });
                System.out.println("Finished deleting directory: " + directory);
            } else {
                System.out.println("Directory does not exist: " + directory);
            }
        } catch (Exception e) {
            System.err.println("Error deleting directory: " + directory + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    

    public String saveImageFromTemporaryToFinal(String displayPath, Long productID) throws Exception {
        String[] split = displayPath.split("/");
        String fileName = split[split.length - 1];
        String folderName = split[split.length - 2];
        Path src = Path.of(IMAGES_TEMPORARY_PATH + folderName + "/" + fileName);
        Path dest = Path.of(IMAGES_FINAL_PATH + productID + "/" + fileName);
        try {
            Files.createDirectories(Path.of(IMAGES_FINAL_PATH + productID));
            Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
            return DISPLAY_FINAL_PATH + productID + "/" + fileName;
        } catch (Exception e) {
            throw new Exception("Lỗi chuyển ảnh từ tạm thời sang chính thức: " + e.getMessage(), e);
        }
    }

    public String getTemporaryFolderName(String fileName) {
        return IMAGES_TEMPORARY_PATH + ProductService.toSlugName(fileName);
    }

    public String getFinalFolderName(String fileName) {
        return IMAGES_FINAL_PATH + ProductService.toSlugName(fileName);
    }

    public String saveImageToTemporaryFile(MultipartFile uploadFile, String folderName, String fileName) throws Exception {
        if (uploadFile == null || uploadFile.isEmpty()) {
            return null;
        }
        try (InputStream inputStream = uploadFile.getInputStream()) {
            Path folderPath = Paths.get(IMAGES_TEMPORARY_PATH + folderName);
            Files.createDirectories(folderPath);
            BufferedImage image = ImageIO.read(inputStream);
            Path filePath = folderPath.resolve(fileName + ".jpg");
            ImageIO.write(image, "jpg", filePath.toFile());
            return DISPLAY_TEMPORARY_PATH + folderName + "/" + fileName + ".jpg";
        } catch (Exception e) {
            throw new Exception("Upload ảnh lỗi " + e.getMessage(), e);
        }
    }

    public void deleteTemporaryDirectory(String displayPath) {
        System.out.println("Attempting to delete temporary directory for path: " + displayPath);
        
        if (displayPath == null || displayPath.isEmpty()) {
            return;
        }
        
        if (!displayPath.startsWith(DISPLAY_TEMPORARY_PATH)) {
            return;
        }
        
        try {
            String relativePath = displayPath.substring(DISPLAY_TEMPORARY_PATH.length());
            
            if (relativePath.isEmpty()) {
                return;
            }
            
            String[] pathParts = relativePath.split("/");
            if (pathParts.length == 0 || pathParts[0].isEmpty()) {
                return;
            }
            
            String folderName = pathParts[0];
            
            Path directory = Path.of(IMAGES_TEMPORARY_PATH + folderName);
            
            if (Files.exists(directory)) {
                deleteDirectory(directory);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
