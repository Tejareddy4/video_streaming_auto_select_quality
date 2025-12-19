package in.tejareddy.video_streaming_auto_select_quality.Controllers;


import in.tejareddy.video_streaming_auto_select_quality.Payload.CustomMessage;
import in.tejareddy.video_streaming_auto_select_quality.Services.VideoService;
import in.tejareddy.video_streaming_auto_select_quality.entities.Video;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/videos")
public class VideoController {


    private final VideoService videoService;

    public VideoController(VideoService videoService){
        this.videoService = videoService;

    }

    //upload
    @PostMapping
    public ResponseEntity<?> create(
        @RequestParam("file") MultipartFile file,
        @RequestParam("title") String title,
        @RequestParam("description") String description
    ){

        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setId(UUID.randomUUID().toString());
        Video savedVideo = videoService.save(video, file);

        if (!(savedVideo == null)) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(video);
        } else{
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CustomMessage.builder()
                            .message("Video not uploaded")
                            .success(false)
                            .build()
                    );
        }
    }

    //Get all Videos
    @GetMapping
    public List<Video> getAll(){
        return videoService.getAll();
    }


    //Get video stream
    @GetMapping("/stream/{videoId}")
    public ResponseEntity<Resource> stream(
            @PathVariable String videoId
    ){
        Video video = videoService.get(videoId);

        String contentType= video.getContentType();

        String filePath = video.getFilePath();

        Resource resource = new FileSystemResource(filePath);

        if(contentType == null){
            contentType = "application/octet-stream";
        }

        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }


}
