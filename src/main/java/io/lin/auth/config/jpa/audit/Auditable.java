package io.lin.auth.config.jpa.audit;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public abstract class Auditable extends CommonAuditFields{

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    protected Long createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    protected LocalDateTime createdAt;

}
