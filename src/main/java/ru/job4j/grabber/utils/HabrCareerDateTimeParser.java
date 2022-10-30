package ru.job4j.grabber.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HabrCareerDateTimeParser implements DateTimeParser {

    @Override
    public LocalDateTime parse(String parse) {
        LocalDateTime localDateTime = LocalDateTime.parse(parse);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-ddТhh:mm:ss");
        localDateTime.format(formatter);
        return localDateTime;
    }
}