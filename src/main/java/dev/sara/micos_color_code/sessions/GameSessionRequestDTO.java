package dev.sara.micos_color_code.sessions;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GameSessionRequestDTO(@NotNull(message = "Los puntos son obligatorios") @Min(value = 0, message = "Los puntos no pueden ser negativos")int points,int timeCompleted, int levels, int currentLevel ) {}
