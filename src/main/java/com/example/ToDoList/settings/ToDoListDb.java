package com.example.ToDoList.settings;

import com.example.ToDoList.entity.ToDoList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ToDoListDb extends JpaRepository<ToDoList, UUID> {
}
