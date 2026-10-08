'use client';
import React from 'react';
import Link from 'next/link';

const Footer = () => {
  return (
    <footer className="bg-gray-900 text-gray-400 py-8">
      <div className="container mx-auto px-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div>
            <h3 className="text-white text-lg font-semibold mb-4">HRM System</h3>
            <p className="mb-4">Hệ thống quản lý nhân sự hiện đại và toàn diện cho doanh nghiệp.</p>
          </div>
          
          <div>
            <h4 className="text-white text-sm font-semibold mb-4">LIÊN KẾT</h4>
            <ul className="space-y-2">
              <li><Link href="/" className="hover:text-white transition-colors">Trang chủ</Link></li>
              <li><Link href="/employees" className="hover:text-white transition-colors">Nhân viên</Link></li>
              <li><Link href="/attendance" className="hover:text-white transition-colors">Chấm công</Link></li>
              <li><Link href="/payroll" className="hover:text-white transition-colors">Tính lương</Link></li>
            </ul>
          </div>
          
          <div>
            <h4 className="text-white text-sm font-semibold mb-4">LIÊN HỆ</h4>
            <ul className="space-y-2">
              <li>hrm@company.vn</li>
              <li>+84 987 654 321</li>
              <li>TP. Hồ Chí Minh, Việt Nam</li>
            </ul>
          </div>
        </div>
        
        <div className="border-t border-gray-800 mt-8 pt-6 text-sm text-center">
          <p>&copy; {new Date().getFullYear()} HRM System. Tất cả các quyền được bảo lưu.</p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
