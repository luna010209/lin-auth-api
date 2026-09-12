package io.lin.auth.config.jpa.audit;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class CommonAuditFields {

    @LastModifiedBy
    @Column(name = "updated_by")
    protected Long updatedBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    protected LocalDateTime updatedAt;

//    @Column(name = "is_deleted", nullable = false)
//    protected boolean isDeleted = false;
//
//    public boolean isDeleted() {
//        return isDeleted;
//    }
//
//    public void delete() {
//        this.isDeleted = true;
//    }
}
