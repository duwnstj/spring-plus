package org.example.expert.domain.todo.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.response.TodoSearchResponseDto;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.comment.entity.QComment.comment;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class TodoQueryRepositoryImpl implements TodoQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        return Optional.ofNullable(jpaQueryFactory.select(todo)
                .from(todo)
                .leftJoin(todo.user, user).fetchJoin()
                .where(todoIdEq(todoId))
                .fetchOne());

    }

    @Override
    public Page<TodoSearchResponseDto> searchTitle(Pageable pageable, String title) {
        List<TodoSearchResponseDto> results = jpaQueryFactory.select(
                        Projections.constructor(
                                TodoSearchResponseDto.class,
                                todo.title,
                                todo.managers.size(),
                                todo.comments.size()))
                .distinct()
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(todo.comments, comment)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .where(todo.title.containsIgnoreCase(title))
                .fetch();

        Long totalCount = jpaQueryFactory
                .select(Wildcard.count)
                .from(todo)
                .where(todo.title.containsIgnoreCase(title))
                .fetchOne();

        return new PageImpl<>(results, pageable, totalCount);

    }

    @Override
    public Page<TodoSearchResponseDto> searchCreateAt(Pageable pageable, LocalDateTime startDate, LocalDateTime endDate) {

        List<TodoSearchResponseDto> results = jpaQueryFactory.select(
                        Projections.constructor(TodoSearchResponseDto.class,
                                todo.title,
                                todo.managers.size(),
                                todo.comments.size())
                )
                .distinct()
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(todo.comments, comment)
                .where(todo.createdAt.between(startDate, endDate))
                .fetch();

        Long totalCount = jpaQueryFactory.select(Wildcard.count)
                .from(todo)
                .where(todo.createdAt.between(startDate, endDate))
                .fetchOne();

        return new PageImpl<>(results, pageable, totalCount);
    }

    @Override
    public Page<TodoSearchResponseDto> searchManagers(Pageable pageable, String managerNickname) {
        List<TodoSearchResponseDto> results = jpaQueryFactory.select(
                        Projections.constructor(TodoSearchResponseDto.class,
                                todo.title,
                                todo.managers.size(),
                                todo.comments.size()))
                .from(todo)
                .leftJoin(todo.user,user).on(todo.user.id.eq(user.id))
                .leftJoin(todo.comments,comment).on(todo.id.eq(comment.todo.id))
                .where(todo.user.nickName.containsIgnoreCase(managerNickname))
                .fetch();

        Long totalCount = jpaQueryFactory.select(Wildcard.count)
                .from(todo)
                .where(todo.user.nickName.containsIgnoreCase(managerNickname))
                .fetchOne();

        return new PageImpl<>(results, pageable, totalCount);
    }

    private BooleanExpression todoIdEq(Long todoId) {
        return todoId != null ? todo.id.eq(todoId) : null;
    }

}
