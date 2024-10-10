package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.dto.response.TodoSearchResponseDto;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TodoQueryRepository {

    Optional<Todo> findByIdWithUser(Long todoId);

    Page<TodoSearchResponseDto> searchTitle(Pageable pageable, String title);

    Page<TodoSearchResponseDto> searchCreateAt(Pageable pageable, LocalDateTime startDate, LocalDateTime endDate);

    Page<TodoSearchResponseDto> searchManagers(Pageable pageable, String managerNickname);
}
