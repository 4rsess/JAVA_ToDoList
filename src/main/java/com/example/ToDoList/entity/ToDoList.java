package com.example.ToDoList.entity;

import com.example.ToDoList.models.Priority;
import com.example.ToDoList.models.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "todolist")
@NoArgsConstructor
public class ToDoList {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Обязательно заполнить название")
    @Size(min = 4, message = "Название должно содержать минимум 4 символа")
    private String title;

    private String description = "";
    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.Active;

    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.Medium;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createDate = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updateDate = LocalDateTime.now();
}
