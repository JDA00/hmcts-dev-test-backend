package uk.gov.hmcts.reform.dev.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.dev.dto.CreateTaskRequest;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.repositories.TaskRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @DisplayName("Should create task and return saved task with id")
    @Test
    void createTask_WithValidRequest_ReturnsSavedTask() {
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

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        // Act
        Task result = taskService.createTask(request);

        // Assert
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Task");
        assertThat(result.getDescription()).isEqualTo("Test Description");
        assertThat(result.getStatus()).isEqualTo(Task.TaskStatus.PENDING);
        assertThat(result.getDueDateTime()).isEqualTo(dueDate);
    }

    @DisplayName("Should map request fields correctly to entity")
    @Test
    void createTask_MapsRequestFieldsToEntity() {
        // Arrange
        LocalDateTime dueDate = LocalDateTime.now().plusDays(7);
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("My Task")
            .description("My Description")
            .dueDateTime(dueDate)
            .build();

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        taskService.createTask(request);

        // Assert - capture what was passed to repository
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());

        Task capturedTask = taskCaptor.getValue();
        assertThat(capturedTask.getTitle()).isEqualTo("My Task");
        assertThat(capturedTask.getDescription()).isEqualTo("My Description");
        assertThat(capturedTask.getDueDateTime()).isEqualTo(dueDate);
    }

    @DisplayName("Should handle null description")
    @Test
    void createTask_WithNullDescription_CreatesTaskSuccessfully() {
        // Arrange
        LocalDateTime dueDate = LocalDateTime.now().plusDays(7);
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Task Without Description")
            .dueDateTime(dueDate)
            .build();

        Task savedTask = Task.builder()
            .id(1L)
            .title("Task Without Description")
            .status(Task.TaskStatus.PENDING)
            .dueDateTime(dueDate)
            .createdAt(LocalDateTime.now())
            .build();

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        // Act
        Task result = taskService.createTask(request);

        // Assert
        assertThat(result.getTitle()).isEqualTo("Task Without Description");
        assertThat(result.getDescription()).isNull();
    }
}
