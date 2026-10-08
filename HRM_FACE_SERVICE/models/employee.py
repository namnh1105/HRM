from sqlalchemy import Column, String, Text, Date, DateTime, Boolean
from sqlalchemy.dialects.postgresql import UUID, JSONB
from sqlalchemy.orm import relationship
from database.session import Base
import uuid


class Gender:
    MALE = "MALE"
    FEMALE = "FEMALE"
    OTHER = "OTHER"


class EmploymentStatus:
    ACTIVE = "ACTIVE"
    INACTIVE = "INACTIVE"
    RESIGNED = "RESIGNED"
    ON_LEAVE = "ON_LEAVE"


class Employee(Base):
    __tablename__ = "employees"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4, index=True)
    employee_code = Column(String(20), nullable=False, unique=True)

    # User link
    user_id = Column(UUID(as_uuid=True), nullable=True, unique=True)

    # Personal info
    first_name = Column(String(50), nullable=False)
    last_name = Column(String(50), nullable=False)
    full_name = Column(String(100), nullable=False)
    email = Column(String(100), nullable=False, unique=True)
    phone = Column(String(20), nullable=True)
    date_of_birth = Column(Date, nullable=True)
    gender = Column(String(10), nullable=True)

    # ID Card info
    id_card_number = Column(String(20), nullable=True)
    id_card_issued_date = Column(Date, nullable=True)
    id_card_issued_place = Column(String(100), nullable=True)

    # Address
    permanent_address = Column(Text, nullable=True)
    current_address = Column(Text, nullable=True)

    # Avatar
    avatar_url = Column(Text, nullable=True)

    # Work info
    department_id = Column(UUID(as_uuid=True), nullable=True)
    position = Column(String(100), nullable=True)
    join_date = Column(Date, nullable=True)
    leave_date = Column(Date, nullable=True)
    employment_status = Column(String(20), default=EmploymentStatus.ACTIVE)

    # Financial / Insurance info
    bank_account_number = Column(String(30), nullable=True)
    bank_name = Column(String(100), nullable=True)
    tax_code = Column(String(20), nullable=True)
    social_insurance_number = Column(String(20), nullable=True)
    health_insurance_number = Column(String(20), nullable=True)

    # Face embedding for face recognition
    embedding = Column(JSONB, nullable=True)

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
    attendances = relationship("Attendance", back_populates="employee")
    work_shifts = relationship("EmployeeWorkShift", back_populates="employee")
