package com.example.ToDoList.controller;

import com.example.ToDoList.entity.ToDoList;
import com.example.ToDoList.models.*;
import com.example.ToDoList.settings.ToDoListDb;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ToDoList")
@Tag(name = "ToDoList")
@RequiredArgsConstructor
public class ToDoListController {

    private final ToDoListDb toDoListDb;

    @PostMapping("/taskCreate")
    @Operation(
            summary = "Создание задания",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseModel.class))),
                    @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content()),
                    @ApiResponse(responseCode = "500", description = "InternalServerError", content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseModel.class)))
            }
    )
    public ResponseEntity<?> taskCreate(@RequestParam String title,
                                            @RequestParam(required = false) String description,
                                            @Parameter(example = "2025-07-27T11:55:22")
                                            @RequestParam(required = false) LocalDateTime deadline,
                                            @RequestParam(required = false) Priority priority){
        try{
            ToDoList task = new ToDoList();
            task.setTitle(title);
            task.setDescription(description != null ? description : "");

            if (deadline == null) {
                deadline = extractDeadline(title);
            }
            if (deadline != null && deadline.isBefore(LocalDateTime.now())) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new ResponseModel(400, "Дедлайн нельзя указывать в прошлом"));
            }
            task.setDeadline(deadline);

            if (priority == null){
                priority = detectPriority(title);
            }
            task.setPriority(priority);

            toDoListDb.save(task);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ResponseModel(200, "Задание успешно создано"));

        } catch (IllegalArgumentException error) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ResponseModel(400, "Ошибка: " + error.getMessage()));
        } catch (Exception error){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ResponseModel(500, "Ошибка: " + error.getMessage()));
        }
    }


    @GetMapping("/taskList")
    @Operation(
            summary = "Просмотр списка задач",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ToDoList.class))),
                    @ApiResponse(responseCode = "500", description = "InternalServerError", content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseModel.class)))
            }
    )
    public ResponseEntity<?> taskList(@RequestParam(required = false) Status status,
                                      @RequestParam(required = false) Priority priority,
                                      @RequestParam SortDirection sortDirection){
        try {

            List<ToDoList> tasks = toDoListDb.findAll();

            if (status != null){
                tasks = tasks.stream().filter(task -> task.getStatus() == status).collect(Collectors.toList());
            }
            if (priority != null){
                tasks = tasks.stream().filter(task -> task.getPriority() == priority).collect(Collectors.toList());
            }
            if(sortDirection.equals(SortDirection.DESC)){
                tasks.sort((t1, t2) -> t2.getCreateDate().compareTo(t1.getCreateDate()));
            } else {
                tasks.sort((t1, t2) -> t1.getCreateDate().compareTo(t2.getCreateDate()));
            }

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(tasks);

        } catch (Exception error){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ResponseModel(500, "Ошибка: " + error.getMessage()));
        }
    }


    @PutMapping("/taskEdit")
    @Operation(
            summary = "Редактирование задачи",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseModel.class))),
                    @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content()),
                    @ApiResponse(responseCode = "404", description = "Not Found", content = @Content()),
                    @ApiResponse(responseCode = "500", description = "InternalServerError", content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseModel.class)))
            }
    )
    public ResponseEntity<?> taskEdit (@RequestParam UUID id,
                                       @RequestParam(required = false) String title,
                                       @RequestParam(required = false) String description,
                                       @Parameter(example = "2025-07-27T11:55:22")
                                       @RequestParam(required = false) LocalDateTime deadline,
                                       @RequestParam(required = false) Priority priority){
        try {
            ToDoList task = toDoListDb.findById(id).orElse(null);
            if (task == null){
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new ResponseModel(404, "Указанное задание не найдено"));
            }

            if(title != null) {
                task.setTitle(title);

                if (priority == null){
                    priority = detectPriority(title);
                }

                if (deadline == null) {
                    deadline = extractDeadline(title);
                }
            }

            if(description != null) task.setDescription(description);

            if (deadline != null) {
                if (deadline.isBefore(LocalDateTime.now())) {
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(new ResponseModel(400, "Дедлайн нельзя указывать в прошлом"));
                }
                task.setDeadline(deadline);
            }

            task.setPriority(priority);

            task.setUpdateDate(LocalDateTime.now());
            toDoListDb.save(task);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ResponseModel(200, "Задание успешно отредактирована"));

        } catch (Exception error){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ResponseModel(500, "Ошибка: " + error.getMessage()));
        }
    }


    @DeleteMapping("/taskDelete")
    @Operation(
            summary = "Удаление задачи",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseModel.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found", content = @Content()),
                    @ApiResponse(responseCode = "500", description = "InternalServerError", content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseModel.class)))
            }
    )
    public ResponseEntity<?> teskDelete (@RequestParam UUID id){

        try {

            ToDoList task = toDoListDb.findById(id).orElse(null);
            if (task == null){
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new ResponseModel(404, "Указанное задание не найдено"));
            }

            toDoListDb.delete(task);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ResponseModel(200, "Задание успешно удалено"));

        } catch (Exception error){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ResponseModel(500, "Ошибка: " + error.getMessage()));
        }
    }


    @GetMapping("/getSpecificTask")
    @Operation(
            summary = "Просмотр конкретной задачи",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ToDoList.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found", content = @Content()),
                    @ApiResponse(responseCode = "500", description = "InternalServerError", content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseModel.class)))
            }
    )
    public ResponseEntity<?> getSpecificTask(@RequestParam UUID id){
        try {
            ToDoList task = toDoListDb.findById(id).orElse(null);
            if (task == null){
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new ResponseModel(404, "Указанное задание не найдено"));
            }

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(task);

        } catch (Exception error){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ResponseModel(500, "Ошибка: " + error.getMessage()));
        }
    }


    @PutMapping("/taskChangeStatus")
    @Operation(
            summary = "Маркирование задачи",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseModel.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found", content = @Content()),
                    @ApiResponse(responseCode = "500", description = "InternalServerError", content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseModel.class)))
            }
    )
    public ResponseEntity<?> taskChangeStatus (@RequestParam UUID id,
                                               @RequestParam boolean completed){
        try {

            ToDoList task = toDoListDb.findById(id).orElse(null);
            if (task == null){
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new ResponseModel(404, "Указанное задание не найдено"));
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime deadline = task.getDeadline();

            if (task.getStatus() == Status.Active && deadline != null && now.isAfter(deadline)) {
                task.setStatus(Status.Overdue);
            }

            if (completed) {
                if (deadline != null && now.isAfter(deadline)) {
                    task.setStatus(Status.Late);
                } else {
                    task.setStatus(Status.Completed);
                }
            }
            else {
                if (deadline != null && now.isAfter(deadline)) {
                    task.setStatus(Status.Overdue);
                } else {
                    task.setStatus(Status.Active);
                }
            }

            task.setUpdateDate(now);
            toDoListDb.save(task);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ResponseModel(200, "Статус успешно изменен"));

        } catch (Exception error){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ResponseModel(500, "Ошибка: " + error.getMessage()));
        }
    }

    private Priority detectPriority (String titel){
        if (titel.contains("!1")) return Priority.Critical;
        if (titel.contains("!2")) return Priority.High;
        if (titel.contains("!3")) return Priority.Medium;
        if (titel.contains("!4")) return Priority.Low;
        return Priority.Medium;
    }

    private LocalDateTime extractDeadline(String title) {
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

}