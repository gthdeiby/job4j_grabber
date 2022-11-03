package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final int PAGE_COUNT = 5;

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    private String retrieveDescription(String link) {
        String desc;
        try {
            desc = Jsoup.connect(link).get().select(".style-ugc").text();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        return desc;
    }

    private Post postParse(Element element) {
        Element titleElement = element.select(".vacancy-card__title").first();
        Element linkElement = titleElement.child(0);
        Element dateElement = element.select(".basic-date").first();
        String vacancyName = titleElement.text();
        String vacancyLink = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
        String description = retrieveDescription(vacancyLink);
        String date = dateElement.attr("datetime");
        LocalDateTime dateTime = dateTimeParser.parse(date);
        return new Post(vacancyName, vacancyLink, description, dateTime);
    }

    @Override
    public List<Post> list(String link) {
        List<Post> list = new ArrayList<>();
        for (int page = 1; page <= PAGE_COUNT; page++) {
            Connection connection = Jsoup.connect(String.format("%s%d", link, page));
            try {
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> list.add(postParse(row)));
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return list;
    }
}