package in.tejareddy.video_streaming_auto_select_quality.Controllers;


import in.tejareddy.video_streaming_auto_select_quality.AppConstants;
import in.tejareddy.video_streaming_auto_select_quality.Payload.CustomMessage;
import in.tejareddy.video_streaming_auto_select_quality.Services.VideoService;
import in.tejareddy.video_streaming_auto_select_quality.entities.Video;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/videos")
@CrossOrigin("http://localhost:5173")
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

    @GetMapping("/stream/range/{videoId}")
    public ResponseEntity<Resource> StreamVideoRange(
            @PathVariable String videoId,
            @RequestParam(required = false) String range
    ){
        System.out.println(range);
        Video video = videoService.get(videoId);
        String contentType = video.getContentType();
        Path path = Paths.get(video.getFilePath());
        Resource resource = new FileSystemResource(path);
        if(contentType == null){
            contentType = "application/octet-stream";
        }

        //Total file length
        long fileLength = path.toFile().length();

        //If the range is null we will return these for the first time and when it is null
        if(range == null){
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        }

        long rangeStart;
        long rangeEnd;

        String[] ranges=range.replace("bytes=","").split("-");
        rangeStart = Long.parseLong(range);

        rangeEnd = rangeStart + AppConstants.CHUNK_SIZE-1;

        if(rangeEnd >= fileLength){
            rangeEnd = fileLength-1;
        }

        /*
        if (ranges.length >1){
            rangeEnd = Long.parseLong(ranges[0]);
        }else {
            rangeEnd = fileLength-1;
        }

        if(rangeEnd >fileLength -1){
            rangeEnd = fileLength -1;
        }

         */
        InputStream inputStream;


        try {
            inputStream = Files.newInputStream(path);
            inputStream.skip(rangeStart);
            long contentLength = rangeEnd - rangeStart + 1;

            byte[] data = new byte[(int)contentLength];
            inputStream.read(data,0,data.length);
            System.out.println("read data no of bytes: "+new String(data));
            inputStream.close();


            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentLength(contentLength);
            httpHeaders.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
            httpHeaders.add("Cache-Control", "no-cache, no-store, must-revalidate");
            httpHeaders.add("Pragma", "no-cache");
            httpHeaders.add("Expires", "0");
            httpHeaders.add("X-Content-Type-Options", "nosniff");

            return ResponseEntity
                    .status(HttpStatus.PARTIAL_CONTENT)
                    .headers(httpHeaders)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new ByteArrayResource(data)) ;


        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }


    }


}
