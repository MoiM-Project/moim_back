package data.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.sql.Timestamp;

@Data
@Alias("BookingDetailDto")
public class BookingDetailDto {
    private int num;
    private String bookingDate;
    private String bookingTime;
    private int headCount;
    private String name;
    private String phone;
    private String email;
    private String purpose;
    private String request;
    private int totalPrice;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private Timestamp createdAt;
    private int bookingStatus;
    private String cancelReason;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private Timestamp cancelDate;
    private int roomNum;
    private int userNum;
    private String roomOption;

    // room
    private String roomName;
    private String thumbnailImage;
    private String address;
    private String address2;
    private String lat;
    private String lng;
}
