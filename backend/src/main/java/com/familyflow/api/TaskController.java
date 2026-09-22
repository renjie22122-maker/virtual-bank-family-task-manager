package com.familyflow.api;

import com.familyflow.api.ApiDtos.*;
import com.familyflow.domain.Models.Task;
import com.familyflow.service.AuthService;
import com.familyflow.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final AuthService auth; private final TaskService tasks;
    public TaskController(AuthService auth, TaskService tasks) { this.auth = auth; this.tasks = tasks; }
    @GetMapping public List<Task> list(@RequestHeader("Authorization") String token,
                                      @RequestParam(defaultValue = "All") String status,
                                      @RequestParam(defaultValue = "deadline") String sort) { return tasks.list(auth.requireUser(token), status, sort); }
    @GetMapping("/{id}") public Task get(@RequestHeader("Authorization") String token, @PathVariable String id) { return tasks.get(auth.requireUser(token), id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Task create(@RequestHeader("Authorization") String token, @Valid @RequestBody CreateTaskRequest request) { return tasks.create(auth.requireUser(token), request); }
    @PutMapping("/{id}") public Task update(@RequestHeader("Authorization") String token, @PathVariable String id,
                                            @Valid @RequestBody UpdateTaskRequest request) { return tasks.update(auth.requireUser(token), id, request); }
    @PatchMapping("/{id}/status") public Task status(@RequestHeader("Authorization") String token, @PathVariable String id,
                                                     @Valid @RequestBody StatusRequest request) { return tasks.changeStatus(auth.requireUser(token), id, request.status()); }
    @PostMapping("/{id}/confirm") public Task confirm(@RequestHeader("Authorization") String token, @PathVariable String id,
                                                      @Valid @RequestBody ConfirmTaskRequest request) { return tasks.confirm(auth.requireUser(token), id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader("Authorization") String token, @PathVariable String id) { tasks.delete(auth.requireUser(token), id); }
}
