import asyncio
import json
import numpy as np
import faiss
import os
from sqlalchemy.future import select
from database import AsyncSessionLocal
from models.student import Student

async def initialize_faiss_index():
    print("Initializing FAISS index...")
    
    embedding_dim = 512
    res = faiss.StandardGpuResources()
    index = faiss.GpuIndexFlatL2(res, embedding_dim)
    
    student_id_map = {}
    
    async with AsyncSessionLocal() as db:
        result = await db.execute(select(Student).where(Student.embedding != None))
        students = result.scalars().all()
        
        count = 0
        for student in students:
            try:
                if isinstance(student.embedding, str):
                    try:
                        embeddings_dict = json.loads(student.embedding)
                    except json.JSONDecodeError:
                        print(f"Lỗi: embedding của sinh viên {student.id} không phải định dạng JSON hợp lệ")
                        continue
                else:
                    embeddings_dict = student.embedding
                
                if not isinstance(embeddings_dict, dict):
                    print(f"Lỗi: embedding của sinh viên {student.id} không phải là từ điển: {type(embeddings_dict)}")
                    continue
                    
                for angle, embedding in embeddings_dict.items():
                    vector = np.array([embedding], dtype=np.float32)
                    index.add(vector)
                    student_id_map[count] = (student.id, angle)
                    count += 1
                    print(f"Added {angle} embedding for student {student.id}")
            except Exception as e:
                print(f"Error processing student {student.id}: {str(e)}")
        
        print(f"Built index with {index.ntotal} vectors from {len(students)} students")
    
    if count > 0:
        print("Saving index to disk...")
        cpu_index = faiss.index_gpu_to_cpu(index)
        faiss.write_index(cpu_index, "face_index.bin")
        np.save("student_id_map.npy", student_id_map)
    else:
        print("Không có dữ liệu embedding hợp lệ để lưu")
    
    print("FAISS index initialization complete!")

if __name__ == "__main__":
    asyncio.run(initialize_faiss_index())
