import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import {
  PageHeader,
  TableCard,
  Table,
  Pagination,
  Button,
  StatusBadge,
  toast,
  type Column,
} from '@/components/ui';
import { messageApi } from '@/api/message';
import type { Schemas } from '@/types/api-helpers';

type MessageDTO = Schemas['MessageDTO'];

export default function Message() {
  const [page, setPage] = useState(1);
  const [size] = useState(10);
  const queryClient = useQueryClient();

  const { data, isFetching } = useQuery({
    queryKey: ['portal-message-list', page],
    queryFn: () => messageApi.list({ page, size }),
  });
  const list = (data?.list ?? []) as MessageDTO[];
  const total = data?.total ?? 0;
  const refresh = () => queryClient.invalidateQueries({ queryKey: ['portal-message-list'] });

  const handleRead = async (row: MessageDTO) => {
    await messageApi.read([row.id!]);
    toast.success('已标记已读');
    refresh();
  };
  const handleReadAll = async () => {
    const ids = list.map((m) => m.id!).filter(Boolean);
    if (!ids.length) return;
    await messageApi.read(ids);
    toast.success('已全部已读');
    refresh();
  };
  const handleDelete = async (row: MessageDTO) => {
    await messageApi.delete([row.id!]);
    toast.success('删除成功');
    refresh();
  };

  const columns: Column<MessageDTO>[] = [
    { key: 'id', title: 'ID', width: 64 },
    { key: 'title', title: '标题' },
    {
      key: 'content',
      title: '内容',
      render: (r) => ((r.content?.length ?? 0) > 30 ? `${r.content?.slice(0, 30)}…` : r.content) || '-',
    },
    { key: 'senderName', title: '发送者' },
    { key: 'msgType', title: '类型' },
    { key: 'priority', title: '优先级' },
    {
      key: 'read',
      title: '状态',
      render: (r) => <StatusBadge status={r.read ? 'READ' : 'UNREAD'} label={r.read ? '已读' : '未读'} />,
    },
    { key: 'sendTime', title: '发送时间' },
    {
      key: 'actions',
      title: '操作',
      render: (r) => (
        <>
          {!r.read && (
            <Button size="sm" variant="ghost" onClick={() => handleRead(r)}>
              标记已读
            </Button>
          )}
          <Button size="sm" variant="ghost" className="text-danger-500" onClick={() => handleDelete(r)}>
            删除
          </Button>
        </>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <PageHeader
        title="消息中心"
        description="我的消息"
        actions={<Button onClick={handleReadAll}>全部已读</Button>}
      />
      <TableCard>
        <Table rowKey={(r) => r.id ?? 0} columns={columns} data={list} loading={isFetching} emptyText="暂无消息" />
        <div className="flex justify-end pt-3">
          <Pagination page={page} size={size} total={total} onChange={setPage} />
        </div>
      </TableCard>
    </div>
  );
}
