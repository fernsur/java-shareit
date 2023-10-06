package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;

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

    @JsonIgnore
    private Item item;

    private String authorName;

    private LocalDateTime created;
}
