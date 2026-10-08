from sqlalchemy import Column, String, Text, Date, Time, Float, Integer, Boolean, ForeignKey, DateTime
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship
from database.session import Base
import uuid
from datetime import datetime


class AttendanceStatus:
    PRESENT = "PRESENT"
    ABSENT = "ABSENT"
    LATE = "LATE"
    EARLY_LEAVE = "EARLY_LEAVE"
    HALF_DAY = "HALF_DAY"
    ON_LEAVE = "ON_LEAVE"
    HOLIDAY = "HOLIDAY"
    WEEKEND = "WEEKEND"


class Attendance(Base):
    __tablename__ = "attendances"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4, index=True)
    
    # Employee reference
    employee_id = Column(UUID(as_uuid=True), ForeignKey("employees.id"), nullable=False)
    
    # Work date
    work_date = Column(Date, nullable=False)
    
    # Check-in/out times
    check_in_time = Column(Time, nullable=True)
    check_out_time = Column(Time, nullable=True)
    
    # IP addresses for verification
    check_in_ip = Column(String(255), nullable=True)
    check_out_ip = Column(String(255), nullable=True)
    
    # GPS coordinates
    check_in_latitude = Column(Float, nullable=True)
    check_in_longitude = Column(Float, nullable=True)
    check_out_latitude = Column(Float, nullable=True)
    check_out_longitude = Column(Float, nullable=True)
    
    # Status
    status = Column(String(20), default=AttendanceStatus.PRESENT)
    
    # Hours tracking
    working_hours = Column(Float, nullable=True)
    overtime_hours = Column(Float, default=0.0)
    late_minutes = Column(Integer, default=0)
    early_leave_minutes = Column(Integer, default=0)
    
    # Note
    note = Column(Text, nullable=True)
    
    # Work shift reference
    work_shift_id = Column(UUID(as_uuid=True), ForeignKey("work_shifts.id"), nullable=True)
    
    # Timestamps
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Soft delete
    is_deleted = Column(Boolean, default=False)
    
    # Relationships
    employee = relationship("Employee", back_populates="attendances")
    work_shift = relationship("WorkShift", back_populates="attendances")
