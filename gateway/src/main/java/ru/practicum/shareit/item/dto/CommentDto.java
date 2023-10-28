package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {
    private int id;

    @NotBlank(message = "Комментарий не может быть пустым.")
    @NotEmpty(message = "Комментарий не может быть пустым.")
    private String text;

    private String authorName;

    private LocalDateTime created;
}
