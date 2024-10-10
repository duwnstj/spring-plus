package org.example.expert.domain.todo.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TodoSearchResponseDto {
    private final String title;
    private final int managerCount;
    private final int commentCount;
}



