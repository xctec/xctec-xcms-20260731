import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import {
  PageHeader,
  TableCard,
  Table,
  Pagination,
  Button,
  Modal,
  Form,
  FormItem,
  Input,
  StatusBadge,
  toast,
  type Column,
} from '@/components/ui';
import { workflowApi } from '@/api/workflow';
import type { Schemas } from '@/types/api-helpers';

type WorkflowTaskDTO = Schemas['WorkflowTaskDTO'];

export default function PortalWorkflow() {
  const [page, setPage] = useState(1);
  const [size] = useState(10);
  const [task, setTask] = useState<WorkflowTaskDTO | null>(null);
  const [comment, setComment] = useState('');
  const queryClient = useQueryClient();

  const { data, isFetching } = useQuery({
    queryKey: ['wf-tasks', page],
    queryFn: () => workflowApi.getMyTasks({ page, size }),
  });
  const list = (data?.list ?? []) as WorkflowTaskDTO[];
  const total = data?.total ?? 0;

  const handleComplete = async () => {
    if (!task) return;
    await workflowApi.completeTask(String(task.id!), comment);
    toast.success('任务已完成');
    setTask(null);
    queryClient.invalidateQueries({ queryKey: ['wf-tasks'] });
  };

  const columns: Column<WorkflowTaskDTO>[] = [
    { key: 'taskName', title: '任务名称' },
    { key: 'assigneeName', title: '处理人' },
    { key: 'candidateGroup', title: '候选组' },
    {
      key: 'status',
      title: '状态',
      render: (r) => <StatusBadge status={r.status ?? ''} label={r.status} />,
    },
    { key: 'claimTime', title: '领取时间' },
    { key: 'completeTime', title: '完成时间' },
    {
      key: 'actions',
      title: '操作',
      render: (r) => (
        <Button size="sm" variant="ghost" onClick={() => { setTask(r); setComment(''); }}>
          办理
        </Button>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <PageHeader title="流程中心" description="我的待办任务" />
      <TableCard>
        <Table rowKey={(r) => r.id ?? 0} columns={columns} data={list} loading={isFetching} emptyText="暂无待办" />
        <div className="flex justify-end pt-3">
          <Pagination page={page} size={size} total={total} onChange={setPage} />
        </div>
      </TableCard>

      <Modal open={!!task} onClose={() => setTask(null)} title="办理任务">
        <div className="space-y-4">
          <div>任务：{task?.taskName}</div>
          <FormItem label="审批意见">
            <Input value={comment} onChange={(e) => setComment(e.target.value)} placeholder="请输入审批意见" />
          </FormItem>
          <div className="flex justify-end gap-2">
            <Button type="button" variant="ghost" onClick={() => setTask(null)}>
              取消
            </Button>
            <Button type="button" onClick={handleComplete}>
              提交
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
