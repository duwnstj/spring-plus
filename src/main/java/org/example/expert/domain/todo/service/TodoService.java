package org.example.expert.domain.todo.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponseDto;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;
    private final WeatherClient weatherClient;

    @Transactional
    public TodoSaveResponse saveTodo(AuthUser authUser, TodoSaveRequest todoSaveRequest) {
        User user = User.fromAuthUser(authUser);

        String weather = weatherClient.getTodayWeather();

        Todo newTodo = new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                weather,
                user
        );
        Todo savedTodo = todoRepository.save(newTodo);

        return new TodoSaveResponse(
                savedTodo.getId(),
                savedTodo.getTitle(),
                savedTodo.getContents(),
                weather,
                new UserResponse(user.getId(), user.getEmail(), user.getNickName())
        );
    }

    public Page<TodoResponse> getTodos(int page, int size, String weather, LocalDateTime startDate, LocalDateTime endDate) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Todo> todos;
        if (weather == null || weather.isEmpty()) {
            todos = todoRepository.findAllByOrderByModifiedAtDesc(pageable);
        }else {
            todos = todoRepository.findAllByWeather(weather, pageable);
        }

        if (startDate !=null || endDate != null) {
            todos = todoRepository.findSchedulesBetweenDates(pageable,startDate,endDate);
        }


        return todos.map(todo -> new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(todo.getUser().getId(), todo.getUser().getEmail(), todo.getUser().getNickName()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        ));
    }

    public TodoResponse getTodo(long todoId) {
        Todo todo = todoRepository.findByIdWithUser(todoId)
                .orElseThrow(() -> new InvalidRequestException("Todo not found"));

        User user = todo.getUser();

        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(user.getId(), user.getEmail(), user.getNickName()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        );
    }

    // 일정 검색하는 기능

    public Page<TodoSearchResponseDto> searchTodos(int page, int size, String title, LocalDateTime startDate, LocalDateTime endDate, String managerNickname) {
        Pageable pageable = PageRequest.of(page-1,size);

        if(title != null){
            return todoRepository.searchTitle(pageable,title);
        }else if(startDate!=null || endDate != null){
            return todoRepository.searchCreateAt(pageable, startDate,endDate);
        } else if (managerNickname != null) {
            return todoRepository.searchManagers(pageable,managerNickname);
        }
        return null;
    }
}
