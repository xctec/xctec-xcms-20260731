import { useState } from 'react';
import { Upload, FolderOpen, FileText, Image, File, Download, Trash2, MoreHorizontal, type LucideIcon } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Pagination } from '@/components/ui/Pagination';
import { PageHeader, TableCard } from '@/components/ui/PageHeader';

const mockFiles = [
  { id: 1, name: '2025年度运营报告.pdf', type: 'pdf', size: '2.4 MB', folder: '运营报表', uploader: '张三', uploadTime: '2025-07-25 10:30' },
  { id: 2, name: '组织架构图.png', type: 'image', size: '856 KB', folder: '公共资源', uploader: '李四', uploadTime: '2025-07-24 16:00' },
  { id: 3, name: '流程设计文档.docx', type: 'doc', size: '1.2 MB', folder: '流程中心', uploader: '王五', uploadTime: '2025-07-24 14:20' },
  { id: 4, name: '用户数据导出.xlsx', type: 'excel', size: '3.8 MB', folder: '数据导出', uploader: '赵六', uploadTime: '2025-07-23 11:00' },
  { id: 5, name: '系统架构图.png', type: 'image', size: '1.5 MB', folder: '公共资源', uploader: '钱七', uploadTime: '2025-07-22 09:30' },
  { id: 6, name: '采购合同模板.pdf', type: 'pdf', size: '640 KB', folder: '合同管理', uploader: '孙八', uploadTime: '2025-07-21 15:00' },
  { id: 7, name: '月度财务报表.xlsx', type: 'excel', size: '2.1 MB', folder: '财务报表', uploader: '周九', uploadTime: '2025-07-20 08:00' },
  { id: 8, name: '项目计划书.docx', type: 'doc', size: '980 KB', folder: '项目管理', uploader: '吴十', uploadTime: '2025-07-19 14:00' },
];

const fileIcons: Record<string, { icon: LucideIcon; color: string; bg: string }> = {
  pdf: { icon: FileText, color: 'text-danger-500', bg: 'bg-danger-50' },
  image: { icon: Image, color: 'text-info-500', bg: 'bg-info-50' },
  doc: { icon: FileText, color: 'text-primary-500', bg: 'bg-primary-50' },
  excel: { icon: FileText, color: 'text-success-500', bg: 'bg-success-50' },
};

export default function FileManagementPage() {
  const [page, setPage] = useState(1);
  const [view, setView] = useState<'list' | 'grid'>('list');

  return (
    <div>
      <PageHeader title="文件管理" description="文件上传、下载、文件夹管理" actions={
        <>
          <Button variant="secondary" icon={FolderOpen}>新建文件夹</Button>
          <Button variant="primary" icon={Upload}>上传文件</Button>
        </>
      } />

      <div className="grid grid-cols-5 gap-4">
        {/* Folder Tree */}
        <TableCard>
          <div className="border-b border-gray-100 px-4 py-3"><h3 className="text-sm font-semibold text-gray-800">文件夹</h3></div>
          <div className="p-2">
            {['全部文件', '公共资源', '运营报表', '流程中心', '数据导出', '合同管理', '财务报表', '项目管理'].map((f, i) => (
              <button key={f} className={`flex w-full items-center gap-2 rounded-md px-3 py-2 text-left text-[13px] ${i === 0 ? 'bg-primary-50 text-primary-700 font-medium' : 'text-gray-600 hover:bg-gray-50'}`}>
                <FolderOpen size={15} className={i === 0 ? 'text-primary-500' : 'text-gray-400'} />
                {f}
              </button>
            ))}
          </div>
        </TableCard>

        {/* File List */}
        <div className="col-span-4">
          <TableCard>
            <div className="flex items-center justify-between border-b border-gray-100 px-4 py-3">
              <div className="flex gap-2">
                {['全部', '图片', '文档', '表格', 'PDF'].map((f, i) => (
                  <button key={f} className={`rounded-full px-3 py-1 text-xs font-medium ${i === 0 ? 'bg-primary-500 text-white' : 'bg-gray-100 text-gray-500'}`}>{f}</button>
                ))}
              </div>
              <div className="flex gap-1">
                <button onClick={() => setView('list')} className={`flex h-7 w-7 items-center justify-center rounded ${view === 'list' ? 'bg-primary-50 text-primary-500' : 'text-gray-400'}`}><FileText size={14} /></button>
                <button onClick={() => setView('grid')} className={`flex h-7 w-7 items-center justify-center rounded ${view === 'grid' ? 'bg-primary-50 text-primary-500' : 'text-gray-400'}`}><Image size={14} /></button>
              </div>
            </div>

            {view === 'list' ? (
              <>
                <div className="overflow-x-auto">
                  <table className="w-full text-[13px]">
                    <thead><tr className="border-b border-gray-200 bg-gray-50">
                      {['文件名', '大小', '文件夹', '上传者', '上传时间', '操作'].map((h, i) => (
                        <th key={h} className={`px-4 py-2.5 text-xs font-medium text-gray-500 ${i === 5 ? 'text-right' : 'text-left'}`}>{h}</th>
                      ))}
                    </tr></thead>
                    <tbody>
                      {mockFiles.map(f => {
                        const fc = fileIcons[f.type] || { icon: File, color: 'text-gray-400', bg: 'bg-gray-100' };
                        return (
                          <tr key={f.id} className="border-b border-gray-100 hover:bg-primary-50/50">
                            <td className="px-4 py-2.5">
                              <div className="flex items-center gap-2">
                                <div className={`flex h-7 w-7 items-center justify-center rounded-md ${fc.bg}`}><fc.icon size={14} className={fc.color} /></div>
                                <span className="font-medium text-gray-900">{f.name}</span>
                              </div>
                            </td>
                            <td className="px-4 text-gray-500">{f.size}</td>
                            <td className="px-4 text-gray-500">{f.folder}</td>
                            <td className="px-4 text-gray-500">{f.uploader}</td>
                            <td className="px-4 text-[11px] text-gray-400">{f.uploadTime}</td>
                            <td className="px-4 text-right">
                              <button className="text-gray-400 hover:text-primary-500"><Download size={14} /></button>
                              <button className="ml-3 text-gray-400 hover:text-danger-500"><Trash2 size={14} /></button>
                              <button className="ml-2 text-gray-400 hover:text-gray-600"><MoreHorizontal size={14} /></button>
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
                <Pagination page={page} total={8} size={10} onChange={setPage} />
              </>
            ) : (
              <div className="grid grid-cols-4 gap-3 p-4">
                {mockFiles.map(f => {
                  const fc = fileIcons[f.type] || { icon: File, color: 'text-gray-400', bg: 'bg-gray-100' };
                  return (
                    <div key={f.id} className="flex flex-col items-center rounded-lg border border-gray-200 p-4 hover:border-primary-300 hover:shadow-card cursor-pointer">
                      <div className={`mb-2 flex h-12 w-12 items-center justify-center rounded-lg ${fc.bg}`}><fc.icon size={24} className={fc.color} /></div>
                      <span className="truncate text-center text-xs font-medium text-gray-700 w-full" title={f.name}>{f.name}</span>
                      <span className="mt-1 text-[10px] text-gray-400">{f.size}</span>
                    </div>
                  );
                })}
              </div>
            )}
          </TableCard>
        </div>
      </div>
    </div>
  );
}
