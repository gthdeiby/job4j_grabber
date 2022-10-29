package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {
    public static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream in = AlertRabbit.class.getClassLoader()
                .getResourceAsStream("rabbit.properties")) {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public static void main(String[] args) throws Exception {
        Properties properties = loadProperties();
        int interval = Integer.parseInt(properties.getProperty("rabbit.interval"));
        Class.forName(properties.getProperty("jdbc.driver"));
        try (Connection connection = DriverManager.getConnection(
                    properties.getProperty("jdbc.url"),
                    properties.getProperty("jdbc.username"),
                    properties.getProperty("jdbc.password")
            )) {
            /**
             *  Создание класса, управляющего всеми работами.
             *  В объект Scheduler будут добавляться задачи.
             */
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            /**
             * Создание объекта для передачи в Job при старте.
             */
            JobDataMap data = new JobDataMap();
            data.put("connection", connection);
            /**
             * Создание задачи.
             */
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            /**
             * Создание расписания.
             */
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(interval)
                    .repeatForever();
            /**
             * Задача выполняется через триггер.
             * Указывается, когда и с каким расписанием производить запуск.
             */
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            /**
             * Загрузка задачи и триггера в планировщик.
             */
            scheduler.scheduleJob(job, trigger);
            /**
             * Завершение работы.
             */
            Thread.sleep(10000);
            scheduler.shutdown();

        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public static class Rabbit implements Job {

        public Rabbit() {
            System.out.println(hashCode());
        }

        @Override
        public void execute(JobExecutionContext context) {
            System.out.println("Rabbit runs here ...");
            Connection cn = (Connection) context.getJobDetail().getJobDataMap().get("connection");
            try (PreparedStatement ps = cn.prepareStatement(
                    "insert into rabbit (created_date) values (?)"
            )) {
                ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                ps.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}