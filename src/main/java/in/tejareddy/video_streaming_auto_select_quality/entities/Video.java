package in.tejareddy.video_streaming_auto_select_quality.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name ="videos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Video {
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String title;
    private String description;
    private String author;
    private String contentType;
    private String filePath;

}
