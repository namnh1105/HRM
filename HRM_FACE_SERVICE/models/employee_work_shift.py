from sqlalchemy import Column, String, Date, Boolean, ForeignKey, DateTime
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship
from database.session import Base
import uuid


class EmployeeWorkShift(Base):
    __tablename__ = "employee_work_shifts"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4, index=True)

    # Employee reference
    employee_id = Column(UUID(as_uuid=True), ForeignKey("employees.id"), nullable=False)

    # Work shift reference
    work_shift_id = Column(UUID(as_uuid=True), ForeignKey("work_shifts.id"), nullable=False)

    # Date (single day assignment)
    date = Column(Date, nullable=False)

    # Audit fields
    created_at = Column(DateTime, nullable=True)
    updated_at = Column(DateTime, nullable=True)
    created_by = Column(UUID(as_uuid=True), nullable=True)
    updated_by = Column(UUID(as_uuid=True), nullable=True)

    # Soft delete
    is_deleted = Column(Boolean, default=False)
    deleted_at = Column(DateTime, nullable=True)
    deleted_by = Column(UUID(as_uuid=True), nullable=True)

    # Relationships
    employee = relationship("Employee", back_populates="work_shifts")
    work_shift = relationship("WorkShift", back_populates="employee_work_shifts")
