package io.lin.auth.common.audit;

import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.SoftDelete;

@MappedSuperclass
@SoftDelete(columnName = "is_deleted")
public abstract class Auditable extends UserAuditable {

}
