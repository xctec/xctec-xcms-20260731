import { useMemo, useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import {
  PageHeader,
  FilterBar,
  TableCard,
  Table,
  Pagination,
  Button,
  Modal,
  StatusBadge,
  Input,
  Select,
  Form,
  FormItem,
  useForm,
  toast,
  type Column,
} from '@/components/ui';
import { useConfirm } from '@/common/confirm';
import { Can } from '@/components/auth/Can';
import { taskApi } from '@/api/task';
import type { Schemas } from '@/types/api-helpers';

type TaskScheduleDTO = Schemas['TaskScheduleDTO'];
type TaskCreateRequest = Schemas['TaskCreateRequest'];

const STATUS_LABEL: Record<string, string> = {
  ENABLED: '启用',
  DISABLED: '禁用',
  PAUSED: '已暂停',
};

export default function TaskSchedule() {
  const [page, setPage] = useState(1);
  const [size] = useState(10);
  const [keyword, setKeyword] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const queryClient = useQueryClient();
  const { confirm, Confirm } = useConfirm();

  const { data, isFetching } = useQuery({
    queryKey: ['task-list', page],
    queryFn: () => taskApi.list({ page, size }),
  });
  const all = (data?.list ?? []) as TaskScheduleDTO[];
  const filtered = useMemo(
    () => (keyword ? all.filter((t) => (t.taskName ?? '').includes(keyword)) : all),
    [all, keyword],
  );
  const total = data?.total ?? 0;

  const openCreate = () => setModalOpen(true);

  const handleDelete = (row: TaskScheduleDTO) =>
    confirm({
      title: '确认删除',
      description: `确定要删除任务「${row.taskName}」吗？`,
      onOk: async () => {
        await taskApi.delete(row.id!);
        toast.success('删除成功');
        queryClient.invalidateQueries({ queryKey: ['task-list'] });
      },
    });

  const handlePause = (row: TaskScheduleDTO) =>
    confirm({
      title: '暂停任务',
      description: `确定要暂停任务「${row.taskName}」吗？`,
      onOk: async () => {
        await taskApi.pause(row.id!);
        toast.success('已暂停');
        queryClient.invalidateQueries({ queryKey: ['task-list'] });
      },
    });

  const handleResume = async (row: TaskScheduleDTO) => {
    await taskApi.resume(row.id!);
    toast.success('已恢复');
    queryClient.invalidateQueries({ queryKey: ['task-list'] });
  };

  const columns: Column<TaskScheduleDTO>[] = [
    { key: 'id', title: 'ID', width: 64 },
    { key: 'taskName', title: '任务名称' },
    { key: 'taskCode', title: '任务编码' },
    { key: 'taskType', title: '类型' },
    { key: 'cronExpression', title: 'Cron' },
    { key: 'handlerName', title: '处理器' },
    {
      key: 'status',
      title: '状态',
      render: (r) => <StatusBadge status={r.status ?? ''} label={STATUS_LABEL[r.status ?? ''] || r.status} />,
    },
    { key: 'nextExecAt', title: '下次执行' },
    {
      key: 'actions',
      title: '操作',
      align: 'center',
      render: (r) => (
        <>
          {r.status === 'PAUSED' || r.status === 'DISABLED' ? (
            <Can permission="task:resume">
              <Button size="sm" variant="ghost" onClick={() => handleResume(r)}>
                恢复
              </Button>
            </Can>
          ) : (
            <Can permission="task:pause">
              <Button size="sm" variant="ghost" onClick={() => handlePause(r)}>
                暂停
              </Button>
            </Can>
          )}
          <Can permission="task:delete">
            <Button size="sm" variant="ghost" className="text-danger-500" onClick={() => handleDelete(r)}>
              删除
            </Button>
          </Can>
        </>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <PageHeader
        title="任务调度"
        description="管理系统定时任务与异步任务"
        actions={
          <Can permission="task:create">
            <Button onClick={openCreate}>+ 新建任务</Button>
          </Can>
        }
      />
      <TableCard>
        <FilterBar>
          <Input
            placeholder="搜索任务名称"
            value={keyword}
            onChange={(e) => {
              setKeyword(e.target.value);
              setPage(1);
            }}
          />
        </FilterBar>
        <Table rowKey={(r) => r.id ?? 0} columns={columns} data={filtered} loading={isFetching} emptyText="暂无任务" />
        <div className="flex justify-end pt-3">
          <Pagination page={page} size={size} total={total} onChange={setPage} />
        </div>
      </TableCard>

      {modalOpen && (
        <TaskFormModal
          onClose={() => setModalOpen(false)}
          onSuccess={() => {
            setModalOpen(false);
            queryClient.invalidateQueries({ queryKey: ['task-list'] });
          }}
        />
      )}
      {Confirm}
    </div>
  );
}

type TaskFormValues = {
  taskName: string;
  taskCode: string;
  taskType: string;
  cronExpression: string;
  handlerName: string;
  maxRetry: string;
  retryInterval: string;
  handlerParams: string;
  description: string;
};

function TaskFormModal({ onClose, onSuccess }: { onClose: () => void; onSuccess: () => void }) {
  const form = useForm<TaskFormValues>(
    {
      taskName: '',
      taskCode: '',
      taskType: 'CRON',
      cronExpression: '',
      handlerName: '',
      maxRetry: '0',
      retryInterval: '0',
      handlerParams: '',
      description: '',
    },
    {
      taskName: [{ required: '请输入任务名称' }],
      taskCode: [{ required: '请输入任务编码' }],
      handlerName: [{ required: '请输入处理器名称' }],
    },
  );

  const handleSave = async () => {
    if (!form.validate()) return;
    const v = form.values;
    const payload: TaskCreateRequest = {
      taskName: v.taskName,
      taskCode: v.taskCode,
      taskType: v.taskType,
      cronExpression: v.cronExpression || undefined,
      handlerName: v.handlerName,
      maxRetry: v.maxRetry ? Number(v.maxRetry) : undefined,
      retryInterval: v.retryInterval ? Number(v.retryInterval) : undefined,
      handlerParams: v.handlerParams || undefined,
      description: v.description || undefined,
    };
    await taskApi.create(payload);
    toast.success('创建成功');
    onSuccess();
  };

  return (
    <Modal open onClose={onClose} title="新建任务">
      <Form onSubmit={handleSave} className="space-y-4">
        <FormItem label="任务名称" required error={form.errors.taskName}>
          <Input value={form.values.taskName} onChange={(e) => form.setField('taskName', e.target.value)} />
        </FormItem>
        <FormItem label="任务编码" required error={form.errors.taskCode}>
          <Input value={form.values.taskCode} onChange={(e) => form.setField('taskCode', e.target.value)} />
        </FormItem>
        <FormItem label="任务类型">
          <Select value={form.values.taskType} onChange={(e) => form.setField('taskType', e.target.value)}>
            <option value="CRON">CRON</option>
            <option value="FIXED_RATE">FIXED_RATE</option>
            <option value="FIXED_DELAY">FIXED_DELAY</option>
          </Select>
        </FormItem>
        <FormItem label="Cron 表达式">
          <Input
            value={form.values.cronExpression}
            onChange={(e) => form.setField('cronExpression', e.target.value)}
            placeholder="0 0/1 * * * ?"
          />
        </FormItem>
        <FormItem label="处理器名称" required error={form.errors.handlerName}>
          <Input value={form.values.handlerName} onChange={(e) => form.setField('handlerName', e.target.value)} />
        </FormItem>
        <FormItem label="最大重试次数">
          <Input value={form.values.maxRetry} onChange={(e) => form.setField('maxRetry', e.target.value)} />
        </FormItem>
        <FormItem label="重试间隔(ms)">
          <Input value={form.values.retryInterval} onChange={(e) => form.setField('retryInterval', e.target.value)} />
        </FormItem>
        <FormItem label="处理器参数(JSON)">
          <Input
            value={form.values.handlerParams}
            onChange={(e) => form.setField('handlerParams', e.target.value)}
            placeholder='{"key":"value"}'
          />
        </FormItem>
        <FormItem label="描述">
          <Input value={form.values.description} onChange={(e) => form.setField('description', e.target.value)} />
        </FormItem>
        <div className="flex justify-end gap-2 pt-2">
          <Button type="button" variant="ghost" onClick={onClose}>
            取消
          </Button>
          <Button type="submit">创建</Button>
        </div>
      </Form>
    </Modal>
  );
}
