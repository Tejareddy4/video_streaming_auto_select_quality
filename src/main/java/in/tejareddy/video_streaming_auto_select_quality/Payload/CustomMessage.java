package in.tejareddy.video_streaming_auto_select_quality.Payload;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CustomMessage {

    private String message;

    private boolean success = false;

}
