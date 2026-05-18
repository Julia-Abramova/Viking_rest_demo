package ru.mephi.vikingdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.mephi.vikingdemo.model.Viking;
import ru.mephi.vikingdemo.service.VikingService;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/vikings")
@Tag(name = "Vikings", description = "Операции с викингами")
public class VikingController {

    private final VikingService vikingService;
    private final VikingListener vikingListener;

    public VikingController(VikingService vikingService, VikingListener vikingListener) {
        this.vikingService = vikingService;
        this.vikingListener = vikingListener;
    }
    
    @GetMapping
    @Operation(summary = "Получить список созданных викингов", 
            operationId = "getAllVikings")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список успешно получен")
    })
    public List<Viking> getAllVikings() {
        System.out.println("GET /api/vikings called");
        return vikingService.findAll();
    }

    @PostMapping
    @Operation(summary = "Создать конкретного викинга",
            operationId = "createViking")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Викинг успешно создан")
    })
    public ResponseEntity<Viking> createViking(@RequestBody Viking viking) {
        System.out.println("POST /api/vikings called");
        Viking created = vikingService.create(viking);
        vikingListener.addViking(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить викинга по идентификатору",
            operationId = "deleteViking")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Викинг успешно удалён"),
            @ApiResponse(responseCode = "404", description = "Викинг не найден")
    })
    public ResponseEntity<Void> deleteViking(@PathVariable int id) {
        System.out.println("DELETE /api/vikings/" + id + " called");
        boolean deleted = vikingService.deleteById(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        vikingListener.removeViking(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Перезаписать параметры викинга",
            operationId = "updateViking")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Викинг успешно обновлён"),
            @ApiResponse(responseCode = "404", description = "Викинг не найден")
    })
    public ResponseEntity<Viking> updateViking(@PathVariable int id, @RequestBody Viking viking) {
        System.out.println("PUT /api/vikings/" + id + " called");
        return vikingService.update(id, viking)
                .map(updated -> {
                    vikingListener.updateViking(updated);
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/test")
    @Operation(summary = "Получить список тестовых викингов", 
            operationId = "getTest")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список успешно получен")
    })
    public List<String> test() {
        System.out.println("GET /api/vikings/test called");
        return List.of("Ragnar", "Bjorn");
    }
    
    @PostMapping("/post")
    @Operation(summary = "Создать викинга со случайными параметрами", 
            operationId = "post")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Викинг успешно создан")
    })
    public Viking addViking(){
        System.out.println("POST api/vikings/post called");
        return vikingListener.createRandomViking();
    }
}
