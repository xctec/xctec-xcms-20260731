import { useRef, useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import {
  PageHeader,
  TableCard,
  Table,
  Pagination,
  Button,
  toast,
  type Column,
} from '@/components/ui';
import { fileApi } from '@/api/file';
import type { Schemas } from '@/types/api-helpers';

type FileDTO = Schemas['FileDTO'];

const fmtSize = (n?: number) => {
  if (!n) return '-';
  if (n < 1024) return `${n} B`;
  if (n < 1024 * 1024) return `${(n / 1024).toFixed(1)} KB`;
  return `${(n / 1024 / 1024).toFixed(1)} MB`;
};

export default function FileManagement() {
  const [page, setPage] = useState(1);
  const [size] = useState(10);
  const fileRef = useRef<HTMLInputElement>(null);
  const queryClient = useQueryClient();

  const { data, isFetching } = useQuery({
    queryKey: ['portal-file-list', page],
    queryFn: () => fileApi.list({ page, size }),
  });
  const list = (data?.list ?? []) as FileDTO[];
  const total = data?.total ?? 0;

  const onUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const f = e.target.files?.[0];
    if (!f) return;
    try {
      await fileApi.upload(f);
      toast.success('上传成功');
      queryClient.invalidateQueries({ queryKey: ['portal-file-list'] });
    } catch {
      toast.error('上传失败');
    }
    e.target.value = '';
  };

  const handleDelete = async (row: FileDTO) => {
    await fileApi.delete(row.id!);
    toast.success('删除成功');
    queryClient.invalidateQueries({ queryKey: ['portal-file-list'] });
  };

  const columns: Column<FileDTO>[] = [
    { key: 'id', title: 'ID', width: 64 },
    { key: 'fileName', title: '文件名' },
    { key: 'fileType', title: 'MIME' },
    { key: 'fileSize', title: '大小', render: (r) => fmtSize(r.fileSize) },
    { key: 'storageType', title: '存储类型' },
    { key: 'ownerId', title: '上传者ID' },
    {
      key: 'actions',
      title: '操作',
      render: (r) => (
        <Button size="sm" variant="ghost" className="text-danger-500" onClick={() => handleDelete(r)}>
          删除
        </Button>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <PageHeader
        title="文件中心"
        description="我的文件"
        actions={<Button onClick={() => fileRef.current?.click()}>+ 上传文件</Button>}
      />
      <input type="file" ref={fileRef} className="hidden" onChange={onUpload} />
      <TableCard>
        <Table rowKey={(r) => r.id ?? 0} columns={columns} data={list} loading={isFetching} emptyText="暂无文件" />
        <div className="flex justify-end pt-3">
          <Pagination page={page} size={size} total={total} onChange={setPage} />
        </div>
      </TableCard>
    </div>
  );
}
