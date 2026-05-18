package ru.mephi.vikingdemo.model;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Модель викинга")
public record Viking(
        @Schema(description = "Идентификатор викинга", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        Integer id,
        @Schema(description = "Имя викинга", example = "Bjorn")
        String name,
        @Schema(description = "Возраст", example = "31")
        int age,
        @Schema(description = "Рост в сантиметрах", example = "184")
        int heightCm,
        @Schema(description = "Цвет волос", example = "Blond")
        HairColor hairColor,
        @Schema(description = "Форма бороды")
        BeardStyle beardStyle,
        @ArraySchema(schema = @Schema(implementation = EquipmentItem.class), arraySchema = @Schema(description = "Снаряжение викинга"))
        List<EquipmentItem> equipment
) {
}
