import { useState } from 'react';
import { Upload, FolderOpen, FileText, Image, File, Download, Trash2, Share2, MoreHorizontal, HardDrive } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Pagination } from '@/components/ui/Pagination';
import { PageHeader, FilterBar, TableCard } from '@/components/ui/PageHeader';

const mockFolders = [
  { id: 1, name: '我的文档', count: 12 },
  { id: 2, name: '图片素材', count: 28 },
  { id: 3, name: '工作汇报', count: 8 },
  { id: 4, name: '项目文档', count: 15 },
  { id: 5, name: '回收站', count: 3, isTrash: true },
];

const mockFiles = [
  { id: 1, name: '季度工作总结.docx', type: 'doc', size: '1.2 MB', folder: '工作汇报', modified: '2025-07-25 10:30', shared: false },
  { id: 2, name: '产品架构图.png', type: 'image', size: '856 KB', folder: '图片素材', modified: '2025-07-24 16:00', shared: true },
  { id: 3, name: '项目计划书.pdf', type: 'pdf', size: '2.4 MB', folder: '项目文档', modified: '2025-07-24 14:20', shared: false },
  { id: 4, name: '团队建设方案.docx', type: 'doc', size: '680 KB', folder: '我的文档', modified: '2025-07-23 11:00', shared: false },
  { id: 5, name: '会议记录.xlsx', type: 'excel', size: '320 KB', folder: '我的文档', modified: '2025-07-22 09:30', shared: false },
  { id: 6, name: '宣传海报.png', type: 'image', size: '3.5 MB', folder: '图片素材', modified: '2025-07-21 15:00', shared: true },
  { id: 7, name: '需求文档.pdf', type: 'pdf', size: '1.8 MB', folder: '项目文档', modified: '2025-07-20 08:00', shared: false },
  { id: 8, name: '报销明细.xlsx', type: 'excel', size: '210 KB', folder: '我的文档', modified: '2025-07-19 14:00', shared: false },
];

const fileIcons: Record<string, { icon: typeof File; color: string; bg: string }> = {
  pdf: { icon: FileText, color: 'text-danger-500', bg: 'bg-danger-50' },
  image: { icon: Image, color: 'text-info-500', bg: 'bg-info-50' },
  doc: { icon: FileText, color: 'text-primary-500', bg: 'bg-primary-50' },
  excel: { icon: FileText, color: 'text-success-500', bg: 'bg-success-50' },
};

export default function PortalFilePage() {
  const [view, setView] = useState<'list' | 'grid'>('list');
  const [page, setPage] = useState(1);

  return (
    <div>
      <PageHeader title="文件管理" description="我的文件、文件夹管理、文件分享" actions={
        <>
          <Button variant="secondary" icon={FolderOpen}>新建文件夹</Button>
          <Button variant="primary" icon={Upload}>上传文件</Button>
        </>
      } />

      {/* Storage Usage */}
      <div className="mb-4 rounded-lg border border-gray-200 bg-white p-4 shadow-card">
        <div className="flex items-center gap-3">
          <HardDrive size={20} className="text-gray-400" />
          <div className="flex-1">
            <div className="flex items-center justify-between mb-1">
              <span className="text-sm font-medium text-gray-700">存储空间</span>
              <span className="text-xs text-gray-500">8.2 GB / 10 GB</span>
            </div>
            <div className="h-2 rounded-full bg-gray-100 overflow-hidden">
              <div className="h-full rounded-full bg-gradient-to-r from-primary-500 to-primary-600" style={{ width: '82%' }} />
            </div>
          </div>
          <span className="rounded-full bg-warning-50 px-2 py-1 text-xs font-medium text-warning-500">空间不足</span>
        </div>
      </div>

      <div className="grid grid-cols-5 gap-4">
        {/* Folder Sidebar */}
        <TableCard>
          <div className="border-b border-gray-100 px-4 py-3"><h3 className="text-sm font-semibold text-gray-800">文件夹</h3></div>
          <div className="p-2">
            {mockFolders.map(f => (
              <button key={f.id} className="flex w-full items-center gap-2 rounded-md px-3 py-2 text-left text-[13px] text-gray-600 hover:bg-gray-50">
                <FolderOpen size={15} className={f.isTrash ? 'text-danger-400' : 'text-gray-400'} />
                <span className="flex-1 truncate">{f.name}</span>
                <span className="text-[11px] text-gray-400">{f.count}</span>
              </button>
            ))}
          </div>
        </TableCard>

        {/* File Area */}
        <div className="col-span-4">
          <TableCard>
            <div className="flex items-center justify-between border-b border-gray-100 px-4 py-3">
              <div className="flex gap-2">
                {['全部', '文档', '图片', '表格', 'PDF'].map((f, i) => (
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
                      {['文件名', '大小', '文件夹', '修改时间', '分享', '操作'].map((h, i) => (
                        <th key={h} className={`px-4 py-2.5 text-xs font-medium text-gray-500 ${[4].includes(i) ? 'text-center' : i === 5 ? 'text-right' : 'text-left'}`}>{h}</th>
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
                            <td className="px-4 text-[11px] text-gray-400">{f.modified}</td>
                            <td className="px-4 text-center">{f.shared && <Share2 size={14} className="inline text-primary-500" />}</td>
                            <td className="px-4 text-right">
                              <button className="text-gray-400 hover:text-primary-500"><Download size={14} /></button>
                              <button className="ml-3 text-gray-400 hover:text-primary-500"><Share2 size={14} /></button>
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
                    <div key={f.id} className="group flex flex-col items-center rounded-lg border border-gray-200 p-4 hover:border-primary-300 hover:shadow-card cursor-pointer">
                      <div className="mb-2 flex h-12 w-12 items-center justify-center rounded-lg bg-gray-50"><fc.icon size={24} className={fc.color} /></div>
                      <span className="w-full truncate text-center text-xs font-medium text-gray-700" title={f.name}>{f.name}</span>
                      <span className="mt-1 text-[10px] text-gray-400">{f.size}</span>
                      {f.shared && <Share2 size={12} className="mt-1 text-primary-400" />}
                      <div className="mt-2 hidden group-hover:flex gap-2">
                        <button className="text-gray-400 hover:text-primary-500"><Download size={13} /></button>
                        <button className="text-gray-400 hover:text-primary-500"><Share2 size={13} /></button>
                        <button className="text-gray-400 hover:text-danger-500"><Trash2 size={13} /></button>
                      </div>
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
