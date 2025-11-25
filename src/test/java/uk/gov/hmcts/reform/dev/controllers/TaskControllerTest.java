package uk.gov.hmcts.reform.dev.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.dev.dto.CreateTaskRequest;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.services.TaskService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @DisplayName("Should create task and return 201 status")
    @Test
    void createTask_WithValidRequest_Returns201() throws Exception {
        // Arrange
        LocalDateTime dueDate = LocalDateTime.now().plusDays(7);
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Test Task")
            .description("Test Description")
            .dueDateTime(dueDate)
            .build();

        Task savedTask = Task.builder()
            .id(1L)
            .title("Test Task")
            .description("Test Description")
            .status(Task.TaskStatus.PENDING)
            .dueDateTime(dueDate)
            .createdAt(LocalDateTime.now())
            .build();

        when(taskService.createTask(any(CreateTaskRequest.class))).thenReturn(savedTask);

        // Act & Assert
        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Test Task"))
            .andExpect(jsonPath("$.description").value("Test Description"))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @DisplayName("Should return 400 when title is missing")
    @Test
    void createTask_WithMissingTitle_Returns400() throws Exception {
        // Arrange
        CreateTaskRequest request = CreateTaskRequest.builder()
            .description("Description without title")
            .dueDateTime(LocalDateTime.now().plusDays(7))
            .build();

        // Act & Assert
        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors.title").value("Title is required"));
    }

    @DisplayName("Should return 400 when title is blank")
    @Test
    void createTask_WithBlankTitle_Returns400() throws Exception {
        // Arrange
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("   ")
            .dueDateTime(LocalDateTime.now().plusDays(7))
            .build();

        // Act & Assert
        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors.title").exists());
    }

    @DisplayName("Should return 400 when due date is missing")
    @Test
    void createTask_WithMissingDueDate_Returns400() throws Exception {
        // Arrange
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Task Title")
            .build();

        // Act & Assert
        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors.dueDateTime").value("Due date is required"));
    }

    @DisplayName("Should return 400 when due date is in the past")
    @Test
    void createTask_WithPastDueDate_Returns400() throws Exception {
        // Arrange
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Task Title")
            .dueDateTime(LocalDateTime.now().minusDays(1))
            .build();

        // Act & Assert
        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors.dueDateTime").value("Due date must be in the future"));
    }

    @DisplayName("Should create task with only required fields")
    @Test
    void createTask_WithOnlyRequiredFields_Returns201() throws Exception {
        // Arrange
        LocalDateTime dueDate = LocalDateTime.now().plusDays(7);
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Minimal Task")
            .dueDateTime(dueDate)
            .build();

        Task savedTask = Task.builder()
            .id(1L)
            .title("Minimal Task")
            .status(Task.TaskStatus.PENDING)
            .dueDateTime(dueDate)
            .createdAt(LocalDateTime.now())
            .build();

        when(taskService.createTask(any(CreateTaskRequest.class))).thenReturn(savedTask);

        // Act & Assert
        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Minimal Task"))
            .andExpect(jsonPath("$.description").doesNotExist());
    }

    @DisplayName("Should return 400 when title exceeds 255 characters")
    @Test
    void createTask_WithTitleExceedingMaxLength_Returns400() throws Exception {
        // Arrange
        String longTitle = "a".repeat(256);
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title(longTitle)
            .dueDateTime(LocalDateTime.now().plusDays(7))
            .build();

        // Act & Assert
        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors.title").value("Title must not exceed 255 characters"));
    }

    @DisplayName("Should return 400 when description exceeds 1000 characters")
    @Test
    void createTask_WithDescriptionExceedingMaxLength_Returns400() throws Exception {
        // Arrange
        String longDescription = "a".repeat(1001);
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Valid Title")
            .description(longDescription)
            .dueDateTime(LocalDateTime.now().plusDays(7))
            .build();

        // Act & Assert
        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors.description").value("Description must not exceed 1000 characters"));
    }
}
