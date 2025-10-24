package com.swp.project.service.product;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;

import javax.imageio.ImageIO;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ImageService {
    private static final String IMAGES_TEMPORARY_PATH = "src/main/resources/static/images/temporary-products/";
    private static final String DISPLAY_TEMPORARY_PATH = "/images/temporary-products/";
    public static final String IMAGES_FINAL_PATH = "src/main/resources/static/images/products/";
    public static final String DISPLAY_FINAL_PATH = "/images/products/";

    /**
     * Recursively deletes a directory and all its contents.
     * This method safely handles the deletion of directories by first deleting all
     * files
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
            BufferedImage image = ImageIO.read(src.toFile());
            ImageIO.write(image, "jpg", dest.toFile());
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

    @Async  
    public CompletableFuture<String> saveTemporaryImageAsync(MultipartFile uploadFile, String folderName, String fileName) {
        try (InputStream inputStream = uploadFile.getInputStream()) {
            if (uploadFile == null || uploadFile.isEmpty()) {
                throw new IllegalArgumentException("File upload không được để trống");
            }
            Path folderPath = Paths.get(IMAGES_FINAL_PATH + folderName);
            Files.createDirectories(folderPath);
            BufferedImage image = ImageIO.read(inputStream);
            Path filePath = folderPath.resolve(fileName);
            ImageIO.write(image, "jpg", filePath.toFile());
            return CompletableFuture.completedFuture(DISPLAY_FINAL_PATH + folderName + "/" + fileName);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(new Exception("Lỗi lưu ảnh: " + e.getMessage(), e));
        }
    }

    public String copyImageFromStorageToTemporaryFile(String tempFolderName, String fileName, String storageFolderName)
            throws Exception {
        Path tempFolderPath = Path.of(IMAGES_TEMPORARY_PATH + tempFolderName);
        Path desFolderPath = Path.of(IMAGES_FINAL_PATH + storageFolderName);
        Path tempFilePath = tempFolderPath.resolve(fileName + ".jpg");
        Path desFilePath = desFolderPath.resolve(fileName + ".jpg");
        BufferedImage image = ImageIO.read(desFilePath.toFile());
        if (image == null) {
            throw new Exception("File ko phai la anh");
        } else {
            Files.createDirectories(tempFolderPath);
            ImageIO.write(image, "jpg", tempFilePath.toFile());
            return DISPLAY_TEMPORARY_PATH + tempFolderName + "/" + fileName + ".jpg";
        }
    }

    public void deleteTemporaryDirectory(String displayPath) {

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

    /**
     * Renames a specific file from "temp-[number].jpg" format to "[number].jpg"
     * format,
     * replacing existing file if it exists, and returns the display path.
     * 
     * @param directoryPath The path to the directory containing the file
     * @param fileName      The name of the temp file to rename (e.g., "temp-1.jpg")
     * @return The display path of the renamed file, or null if operation failed
     * @throws Exception if there's an error during the file renaming process
     */
    public String renameTempFileToFinalName(String directoryPath, String fileName) throws Exception {
        try {
            Path directory = Paths.get(directoryPath);

            if (!Files.exists(directory) || !Files.isDirectory(directory)) {
                throw new Exception("Directory does not exist or is not a directory: " + directoryPath);
            }

            // Validate file name format
            if (!fileName.matches("temp-\\d+\\.jpg")) {
                throw new Exception("Invalid file name format. Expected format: temp-[number].jpg");
            }

            Path tempFile = directory.resolve(fileName);

            if (!Files.exists(tempFile)) {
                throw new Exception("File does not exist: " + tempFile.toString());
            }

            // Extract the number from "temp-[number].jpg"
            String numberPart = fileName.substring(5, fileName.lastIndexOf(".jpg"));
            String newFileName = numberPart + ".jpg";

            Path newFilePath = tempFile.getParent().resolve(newFileName);

            // Replace existing file or create new one
            Files.move(tempFile, newFilePath, StandardCopyOption.REPLACE_EXISTING);

            if (directoryPath.contains(IMAGES_TEMPORARY_PATH)) {
                String relativePath = directoryPath.replace(IMAGES_TEMPORARY_PATH, "");
                return DISPLAY_TEMPORARY_PATH + relativePath + "/" + newFileName;
            } else if (directoryPath.contains(IMAGES_FINAL_PATH)) {
                String relativePath = directoryPath.replace(IMAGES_FINAL_PATH, "");
                return DISPLAY_FINAL_PATH + relativePath + "/" + newFileName;
            } else {
                return directoryPath + "/" + newFileName;
            }

        } catch (Exception e) {
            throw new Exception("Error renaming temp file: " + e.getMessage(), e);
        }
    }
    public String saveTemporaryImage(MultipartFile uploadFile, String folderName, String fileName)
            throws Exception {
                System.out.println(fileName);
        if (uploadFile == null || uploadFile.isEmpty()) {
            return null;
        }
        try (InputStream inputStream = uploadFile.getInputStream()) {
            Path folderPath = Paths.get(IMAGES_FINAL_PATH + folderName);
            Files.createDirectories(folderPath);
            BufferedImage image = ImageIO.read(inputStream);
            Path filePath = folderPath.resolve(fileName);
            ImageIO.write(image, "jpg", filePath.toFile());
            return DISPLAY_FINAL_PATH + folderName + "/" + fileName;
        } catch (Exception e) {
            throw new Exception("Upload ảnh lỗi " + e.getMessage(), e);
        }
    }
}
