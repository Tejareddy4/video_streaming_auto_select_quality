package in.tejareddy.video_streaming_auto_select_quality.Repositories;

import in.tejareddy.video_streaming_auto_select_quality.entities.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, String> {

    Optional<Video> findVideoByTitle(String title);
}
