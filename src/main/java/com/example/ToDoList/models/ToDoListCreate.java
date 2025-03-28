package com.example.ToDoList.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ToDoListCreate {

    @NotBlank(message = "Обязательно заполнить название")
    @Size(min = 4, message = "Название должно содержать минимум 4 символа")
    private String title;

    private String description = "";
    private LocalDateTime deadline;

    private Priority priority = Priority.Medium;
}
