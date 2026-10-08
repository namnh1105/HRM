from sqlalchemy import Column, String, Text, Time, Float, Boolean
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship
from database.session import Base
import uuid


class WorkShift(Base):
    __tablename__ = "work_shifts"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4, index=True)
    name = Column(String(100), nullable=False)
    code = Column(String(20), nullable=False, unique=True)
    
    # Shift time
    start_time = Column(Time, nullable=False)
    end_time = Column(Time, nullable=False)
    
    # Break duration in hours
    break_duration = Column(Float, default=1.0)
    
    # Total working hours
    total_hours = Column(Float, nullable=True)
    
    # Description
    description = Column(Text, nullable=True)
    
    # Status
    is_active = Column(Boolean, default=True)
    is_night_shift = Column(Boolean, default=False)
    
    # Soft delete
    is_deleted = Column(Boolean, default=False)
    
    # Relationships
    attendances = relationship("Attendance", back_populates="work_shift")
    employee_work_shifts = relationship("EmployeeWorkShift", back_populates="work_shift")
