'use client';
import React from 'react';
import Link from 'next/link';

const Navbar = () => {
  return (
    <nav className="bg-white shadow-md">
      <div className="container mx-auto px-6 py-4">
        <div className="flex justify-between items-center">
          <div className="flex items-center">
            <Link href="/" className="text-xl font-bold text-blue-600">
              HRM System
            </Link>
          </div>
          
          <div className="hidden md:flex items-center space-x-6">
            <Link href="/employees" className="text-gray-700 hover:text-blue-600 transition-colors">
              Nhân viên
            </Link>
            <Link href="/attendance" className="text-gray-700 hover:text-blue-600 transition-colors">
              Chấm công
            </Link>
            <Link href="/payroll" className="text-gray-700 hover:text-blue-600 transition-colors">
              Tính lương
            </Link>
            <Link href="/reports" className="text-gray-700 hover:text-blue-600 transition-colors">
              Báo cáo
            </Link>
          </div>
          
          <div className="flex items-center space-x-4">
            <Link href="/login" className="text-gray-700 hover:text-blue-600 transition-colors">
              Đăng nhập
            </Link>
            <Link 
              href="/register" 
              className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
            >
              Đăng ký
            </Link>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
