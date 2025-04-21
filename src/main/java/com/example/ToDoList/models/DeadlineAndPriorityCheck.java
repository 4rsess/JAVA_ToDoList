package com.example.ToDoList.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DeadlineAndPriorityCheck {
    public static Priority detectPriority(String title) {
        if (title.contains("!1")) return Priority.Critical;
        if (title.contains("!2")) return Priority.High;
        if (title.contains("!3")) return Priority.Medium;
        if (title.contains("!4")) return Priority.Low;
        return Priority.Medium;
    }

    public static LocalDateTime extractDeadline(String title) {
        if (title.contains("!before")) {
            String[] parts = title.split("!before ");
            if (parts.length > 1) {
                String dateStr = parts[1].split(" ")[0].replace("-", ".");
                try {
                    LocalDate deadline = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                    LocalDateTime deadlineDateTime = deadline.atTime(23, 59, 59);

                    if (deadlineDateTime.isBefore(LocalDateTime.now())) {
                        throw new IllegalArgumentException("Дедлайн уже прошел");
                    }

                    return deadlineDateTime;
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Неверный формат даты");
                }
            }
        }
        return null;
    }

    public String cleanTitle(String title) {
        title = title.replaceAll("!1", "")
                .replaceAll("!2", "")
                .replaceAll("!3", "")
                .replaceAll("!4", "");

        title = title.replaceAll("!before\\s\\d{2}[.-]\\d{2}[.-]\\d{4}", "");

        return title.trim().replaceAll("\\s{2,}", " ");
    }
}
