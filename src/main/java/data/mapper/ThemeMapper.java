package data.mapper;

import data.dto.RoomDto;
import data.dto.ThemeDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface ThemeMapper {
    void insertTheme(ThemeDto dto);
    List<ThemeDto> selectThemeList();
    ThemeDto selectTheme(int num);
    List<RoomDto> selectThemeRoomList(HashMap<String,Object> map);
    void updateTheme(ThemeDto dto);
    void deleteTheme(int num);
    int selectThemeCount(int num);
}