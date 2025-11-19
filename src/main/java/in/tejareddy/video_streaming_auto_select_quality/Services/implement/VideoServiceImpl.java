package in.tejareddy.video_streaming_auto_select_quality.Services.implement;

import in.tejareddy.video_streaming_auto_select_quality.Repositories.VideoRepository;
import in.tejareddy.video_streaming_auto_select_quality.Services.VideoService;
import in.tejareddy.video_streaming_auto_select_quality.entities.Video;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class VideoServiceImpl implements VideoService {

    private final VideoRepository videoRepository;

    // Injects the directory path from application properties (e.g., application.properties)
    @Value("${files.video}")
    String DIR;

    public VideoServiceImpl(VideoRepository videoRepository) {
        // Using constructor injection is preferred over @Autowired field injection
        this.videoRepository = videoRepository;
    }

    /**
     * Ensures the video upload directory exists when the service starts.
     */
    @PostConstruct
    public void init(){
        File file = new File(DIR);

        if (!file.exists()) {
            // Use mkdirs() for creating parent directories if they don't exist
            if (file.mkdirs()) {
                System.out.println("Video directory created: " + DIR);
            } else {
                System.err.println("Failed to create video directory: " + DIR);
            }
        } else {
            System.out.println("Video directory already exists: " + DIR);
        }
    }

    /**
     * Saves the video file to the file system and its metadata to the database.
     * @param video The Video entity to save.
     * @param file The uploaded MultipartFile.
     * @return The persisted Video entity, or null on failure.
     */
    @Override
    public Video save(Video video, MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            String contentType = file.getContentType();
            InputStream inputStream = file.getInputStream();

            // 1. Clean the filename to prevent path traversal issues
            String cleanFileName = StringUtils.cleanPath(filename);

            // 2. Define the target directory path using the injected DIR value
            Path targetDir = Paths.get(DIR);

            // 3. Construct the FINAL destination path by resolving the filename within the directory
            // This corrects the NoSuchFileException error.
            Path finalPath = targetDir.resolve(cleanFileName);

            System.out.println("Saving file to: " + finalPath);
            System.out.println("Content type: " + contentType);

            // Copy file to the folder, replacing it if it already exists
            Files.copy(inputStream, finalPath, StandardCopyOption.REPLACE_EXISTING);

            // Set video metadata and save to DB
            video.setContentType(contentType);
            video.setFilePath(finalPath.toString()); // Save the full file system path
            return videoRepository.save(video);

        } catch (Exception e) {
            System.err.println("Error saving video file or metadata: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // --- Other service methods (kept as stubs) ---

    @Override
    public Video get(String videoId) {
        // Implementation logic for fetching a video by ID
        return videoRepository.findById(videoId).orElse(null);
    }

    @Override
    public Video getByTitle(String title) {
        // Implementation logic for fetching a video by title
        // Requires a custom method in VideoRepository (e.g., findByTitle)
        return null;
    }

    @Override
    public List<Video> getAll() {
        // Implementation logic for fetching all videos
        return videoRepository.findAll();
    }
}