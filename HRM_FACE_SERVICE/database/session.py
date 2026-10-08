from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
import asyncio

from database.config import settings

Base = declarative_base()

engine = create_async_engine(
    settings.database_url, 
    echo=settings.DEBUG
)

AsyncSessionLocal = sessionmaker(
    bind=engine, 
    class_=AsyncSession, 
    expire_on_commit=False
)


async def init_db():
    """Initialize database tables"""
    from models.employee import Employee
    from models.work_shift import WorkShift
    from models.attendance import Attendance

    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)


async def get_db():
    """Dependency for getting database session"""
    async with AsyncSessionLocal() as session:
        yield session


if __name__ == "__main__":
    asyncio.run(init_db())
