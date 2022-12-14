package data.dto;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("ThemeDto")
public class ThemeDto {
    private int num;
    private String title;
    private String description;
    private String bannerImage;
}
