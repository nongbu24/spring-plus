package org.example.expert.domain.todo.controller;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TodoController.class)
@AutoConfigureMockMvc(addFilters = false)
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TodoService todoService;

    @Test
    void todo_목록_조회_시_검색_조건_없이_조회할_수_있다() throws Exception {
        // given
        when(todoService.getTodos(1, 10, null, null, null))
                .thenReturn(new PageImpl<>(List.of()));

        // when & then
        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk());

        verify(todoService).getTodos(
                eq(1),
                eq(10),
                isNull(),
                isNull(),
                isNull()
        );
    }

    @Test
    void todo_목록_조회_시_날씨만으로_검색할_수_있다() throws Exception {
        // given
        String weather = "Sunny";

        when(todoService.getTodos(1, 10, weather, null, null))
                .thenReturn(new PageImpl<>(List.of()));

        // when & then
        mockMvc.perform(get("/todos")
                        .param("weather", weather))
                .andExpect(status().isOk());

        verify(todoService).getTodos(
                eq(1),
                eq(10),
                eq(weather),
                isNull(),
                isNull()
        );
    }

    @Test
    void todo_목록_조회_시_수정일_시작일만으로_검색할_수_있다() throws Exception {
        // given
        LocalDate startDate = LocalDate.of(2026, 6, 1);

        when(todoService.getTodos(1, 10, null, startDate, null))
                .thenReturn(new PageImpl<>(List.of()));

        // when & then
        mockMvc.perform(get("/todos")
                        .param("startDate", "2026-06-01"))
                .andExpect(status().isOk());

        verify(todoService).getTodos(
                eq(1),
                eq(10),
                isNull(),
                eq(startDate),
                isNull()
        );
    }

    @Test
    void todo_목록_조회_시_수정일_종료일만으로_검색할_수_있다() throws Exception {
        // given
        LocalDate endDate = LocalDate.of(2026, 6, 16);

        when(todoService.getTodos(1, 10, null, null, endDate))
                .thenReturn(new PageImpl<>(List.of()));

        // when & then
        mockMvc.perform(get("/todos")
                        .param("endDate", "2026-06-16"))
                .andExpect(status().isOk());

        verify(todoService).getTodos(
                eq(1),
                eq(10),
                isNull(),
                isNull(),
                eq(endDate)
        );
    }

    @Test
    void todo_목록_조회_시_날씨와_수정일_기간으로_검색할_수_있다() throws Exception {
        // given
        String weather = "Sunny";
        LocalDate startDate = LocalDate.of(2026, 6, 1);
        LocalDate endDate = LocalDate.of(2026, 6, 16);

        when(todoService.getTodos(1, 10, weather, startDate, endDate))
                .thenReturn(new PageImpl<>(List.of()));

        // when & then
        mockMvc.perform(get("/todos")
                        .param("weather", weather)
                        .param("startDate", "2026-06-01")
                        .param("endDate", "2026-06-16"))
                .andExpect(status().isOk());

        verify(todoService).getTodos(
                eq(1),
                eq(10),
                eq(weather),
                eq(startDate),
                eq(endDate)
        );
    }

    @Test
    void todo_단건_조회에_성공한다() throws Exception {
        // given
        long todoId = 1L;
        String title = "title";
        String nickname = "nickname";
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER, nickname);
        User user = User.fromAuthUser(authUser);
        UserResponse userResponse = new UserResponse(user.getId(), user.getEmail(), user.getNickname());
        TodoResponse response = new TodoResponse(
                todoId,
                title,
                "contents",
                "Sunny",
                userResponse,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // when
        when(todoService.getTodo(todoId)).thenReturn(response);

        // then
        mockMvc.perform(get("/todos/{todoId}", todoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(todoId))
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.user.nickname").value(nickname));
    }

    @Test
    void todo_단건_조회_시_todo가_존재하지_않아_예외가_발생한다() throws Exception {
        // given
        long todoId = 1L;

        // when
        when(todoService.getTodo(todoId))
                .thenThrow(new InvalidRequestException("Todo not found"));

        // then
        mockMvc.perform(get("/todos/{todoId}", todoId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Todo not found"));
    }
}
