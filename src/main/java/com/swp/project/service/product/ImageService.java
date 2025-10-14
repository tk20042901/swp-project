package com.swp.project.service.product;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.swp.project.entity.product.Product;
import com.swp.project.entity.product.SubImage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ImageService {
    private static final String IMAGES_TEMPORARY_PATH = "src/main/resources/static/images/temporary-products/";
    private static final String DISPLAY_TEMPORARY_PATH = "/images/temporary-products/";
    private static final String IMAGES_FINAL_PATH = "src/main/resources/static/images/products/";
    private static final String DISPLAY_FINAL_PATH = "/images/products/";
    private final SubImageService subImageService;

    private void deleteDirectory(Path directory) {
        try {
            if (Files.exists(directory)) {
                Files.walk(directory)
                        .sorted((a, b) -> b.compareTo(a))
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (Exception e) {
                            }
                        });
            }
        } catch (Exception e) {
        }
    }

    private List<SubImage> createAndSaveSubImages(List<String> imagePaths, Product product) {
        List<SubImage> subImages = new ArrayList<>();

        for (String path : imagePaths) {
            SubImage subImage = new SubImage();
            subImage.setProduct(product);
            subImage.setSub_image_url(path);
            SubImage savedSubImage = subImageService.save(subImage);
            subImages.add(savedSubImage);
        }

        return subImages;
    }

    public String saveImageFromTemporaryToFinal(String displayPath, Long productID) throws Exception {
        String[] split = displayPath.split("/");
        String fileName = split[split.length - 1];
        String folderName = split[split.length - 2];
        Path src = Path.of(IMAGES_TEMPORARY_PATH + "/" + folderName + "/" + fileName);
        Path dest = Path.of(IMAGES_FINAL_PATH + "/" + productID + "/" + fileName);
        try {
            Files.createDirectories(Path.of(IMAGES_FINAL_PATH + folderName));
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
        String relativePath = displayPath.substring(DISPLAY_TEMPORARY_PATH.length());
        String folderName = relativePath.split("/")[0];
        Path directory = Paths.get(IMAGES_TEMPORARY_PATH, folderName);
        deleteDirectory(directory);
    }
}
