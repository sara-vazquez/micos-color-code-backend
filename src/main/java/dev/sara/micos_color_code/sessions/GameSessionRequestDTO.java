package dev.sara.micos_color_code.sessions;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GameSessionRequestDTO(
    @NotNull(message = "Los puntos son obligatorios")
    @Min(value = 0, message = "Los puntos no pueden ser negativos")
    int points,
    
    int timeCompleted,
    
    @NotNull(message = "El nivel es obligatorio")
    @Min(value = 1, message = "El nivel debe ser al menos 1")
    int level, 
    
    @NotNull(message = "Debe indicar si completó el nivel")
    Boolean completed 
) {}