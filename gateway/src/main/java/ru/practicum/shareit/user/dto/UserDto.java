package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
public class UserDto {
    private int id;

    @NotBlank(message = "Имя не может быть пустым.")
    private String name;

    @Email(message = "Получена некорректная почта.")
    @NotEmpty(message = "Почта не может быть пустой.")
    private String email;
}
