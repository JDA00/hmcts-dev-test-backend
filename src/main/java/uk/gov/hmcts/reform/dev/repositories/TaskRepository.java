package uk.gov.hmcts.reform.dev.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dev.models.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // JpaRepository provides save() method - all we need for CREATE operation
}
