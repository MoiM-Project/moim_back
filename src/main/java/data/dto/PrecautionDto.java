package data.dto;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("PrecautionDto")
public class PrecautionDto {
    private int num;
    private String pcontent;
    private int roomNum;
}
