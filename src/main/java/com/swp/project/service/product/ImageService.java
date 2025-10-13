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

    public List<SubImage> getTemporarySubImageList(List<MultipartFile> extraImages, String productName, Product product)
            throws Exception {
        List<SubImage> subImages = new ArrayList<>();
        List<String> extraImagePaths = saveTemporaryExtraImages(productName, extraImages);
        if (extraImagePaths != null) {
            for (String path : extraImagePaths) {
                SubImage subImage = new SubImage();
                subImage.setProduct(product);
                subImage.setSub_image_url(path);
                subImages.add(subImage);
            }
        }
        return subImages;
    }

    private List<String> saveTemporaryExtraImages(String productName, List<MultipartFile> extraImages)
            throws Exception {
        if (extraImages == null || extraImages.size() != 3) {
            throw new IllegalArgumentException("Chỉ có 3 ảnh phụ");
        }
        String folderName = ProductService.toSlugName(productName);
        Path uploadDir = Paths.get(IMAGES_TEMPORARY_PATH + folderName);
        List<String> savedPaths = new ArrayList<>();
        try {
            Files.createDirectories(uploadDir);
            for (int i = 0; i < 3; i++) {
                MultipartFile file = extraImages.get(i);
                try (InputStream inputStream = file.getInputStream()) {
                    BufferedImage image = ImageIO.read(inputStream);
                    String fileName = String.format("%s-%d.jpg", folderName, i + 1);
                    Path filePath = uploadDir.resolve(fileName);
                    ImageIO.write(image, "jpg", filePath.toFile());
                    savedPaths.add(DISPLAY_TEMPORARY_PATH + folderName + "/" + fileName);
                }
            }
            return savedPaths;
        } catch (Exception e) {
            deleteDirectory(uploadDir);
            throw new Exception("Upload ảnh lỗi " + e.getMessage(), e);
        }
    }

    public String saveTemporaryMainImage(String productName, MultipartFile file) throws Exception {
        String folderName = ProductService.toSlugName(productName);
        Path uploadDir = Paths.get(IMAGES_TEMPORARY_PATH + folderName);
        try (InputStream inputStream = file.getInputStream()) {
            Files.createDirectories(uploadDir);
            BufferedImage image = ImageIO.read(inputStream);
            String fileName = folderName + ".jpg";
            Path filePath = uploadDir.resolve(fileName);
            ImageIO.write(image, "jpg", filePath.toFile());
            return DISPLAY_TEMPORARY_PATH + folderName + "/" + fileName;
        } catch (Exception e) {
            deleteDirectory(uploadDir);
            throw new Exception("Upload ảnh lỗi " + e.getMessage(), e);
        }
    }

    private String moveImageToFinalPath(String temporaryImagePath, String productName) throws Exception {

        String relativePath = temporaryImagePath.substring(DISPLAY_TEMPORARY_PATH.length());
        Path sourceFile = Paths.get(IMAGES_TEMPORARY_PATH, relativePath);

        String folderName = ProductService.toSlugName(productName);
        Path destinationDir = Paths.get(IMAGES_FINAL_PATH, folderName);

        Path destinationFile = destinationDir.resolve(sourceFile.getFileName());

        Files.createDirectories(destinationDir);
        Files.move(sourceFile, destinationFile, StandardCopyOption.REPLACE_EXISTING);

        return DISPLAY_FINAL_PATH + folderName + "/" + sourceFile.getFileName().toString();
    }

    private List<String> moveSubImageToFinalPath(List<String> temporarySubImagePaths, String productName)
            throws Exception {
        if (temporarySubImagePaths == null || temporarySubImagePaths.isEmpty()) {
            throw new IllegalArgumentException("No sub-images to move");
        }

        List<String> finalPaths = new ArrayList<>();
        String folderName = ProductService.toSlugName(productName);
        Path destinationDir = Paths.get(IMAGES_FINAL_PATH, folderName);
        List<Path> movedFiles = new ArrayList<>();

        try {
            Files.createDirectories(destinationDir);

            for (String temporaryImagePath : temporarySubImagePaths) {
                if (temporaryImagePath == null || !temporaryImagePath.startsWith(DISPLAY_TEMPORARY_PATH)) {
                    throw new IllegalArgumentException("Invalid temporary sub-image path: " + temporaryImagePath);
                }

                String relativePath = temporaryImagePath.substring(DISPLAY_TEMPORARY_PATH.length());
                Path sourceFile = Paths.get(IMAGES_TEMPORARY_PATH, relativePath);

                String fileName = sourceFile.getFileName().toString();
                Path destinationFile = destinationDir.resolve(fileName);

                Files.move(sourceFile, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                movedFiles.add(destinationFile);

                finalPaths.add(Paths.get(DISPLAY_FINAL_PATH, folderName, fileName).toString());
            }

            return finalPaths;

        } catch (Exception e) {
            deleteDirectory(destinationDir);
            throw new Exception("Failed to move sub-images to final path: " + e.getMessage(), e);
        }
    }

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

    public Pair<String, List<SubImage>> getAllFinalImage(List<SubImage> extraImages, String productName,
            Product product) throws Exception {
        String folderName = ProductService.toSlugName(productName);
        String temporaryImagePath = DISPLAY_TEMPORARY_PATH + folderName + "/" + folderName + ".jpg";

        // Move main image to final destination
        String mainImageFinalPath = moveImageToFinalPath(temporaryImagePath, productName);

        // Process extra images
        List<SubImage> processedSubImages = processExtraImages(extraImages, productName, product);

        return Pair.of(mainImageFinalPath, processedSubImages);
    }

    private List<SubImage> processExtraImages(List<SubImage> extraImages, String productName, Product product)
            throws Exception {
        if (extraImages == null || extraImages.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> temporaryImagePaths = extraImages.stream()
                .map(SubImage::getSub_image_url)
                .toList();

        List<String> finalImagePaths = moveSubImageToFinalPath(temporaryImagePaths, productName);

        return createAndSaveSubImages(finalImagePaths, product);
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

    public String saveImageToTemporaryFile(String productName,MultipartFile uploadFile) throws Exception {
        if(uploadFile == null){
            return null;
        }
        String folderName = ProductService.toSlugName(productName);
        Path folderPath = Paths.get(IMAGES_TEMPORARY_PATH + folderName);
        try (InputStream inputStream = uploadFile.getInputStream()) {
            Files.createDirectories(folderPath);
            BufferedImage image = ImageIO.read(inputStream);
            String fileName = folderName + ".jpg";
            Path filePath = folderPath.resolve(fileName);
            ImageIO.write(image, "jpg", filePath.toFile());
            return DISPLAY_TEMPORARY_PATH + folderName + "/" + fileName;
        } catch (Exception e) {
            throw new Exception("Upload ảnh lỗi " + e.getMessage(), e);
        }
    }
}
