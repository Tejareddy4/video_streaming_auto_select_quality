package in.tejareddy.video_streaming_auto_select_quality.Services;

import in.tejareddy.video_streaming_auto_select_quality.entities.Video;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoService {

    Video save(Video video, MultipartFile file);

    Video get(String videoId);

    Video getByTitle(String title);

    List<Video> getAll();
}
