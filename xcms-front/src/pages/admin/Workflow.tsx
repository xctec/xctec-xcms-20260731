import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import {
  PageHeader,
  FilterBar,
  TableCard,
  Table,
  Button,
  Input,
  Form,
  FormItem,
  useForm,
  Modal,
  StatusBadge,
  toast,
  type Column,
} from '@/components/ui';
import { workflowApi } from '@/api/workflow';
import type { Schemas } from '@/types/api-helpers';

type WorkflowDefinitionDTO = Schemas['WorkflowDefinitionDTO'];

export default function Workflow() {
  const [processKey, setProcessKey] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const queryClient = useQueryClient();

  const defQ = useQuery({
    queryKey: ['wf-def', processKey],
    queryFn: () => workflowApi.getProcessDefinition(processKey),
    enabled: !!processKey,
  });
  const def = defQ.data;

  const form = useForm<{ processKey: string; variables: string }>(
    { processKey: '', variables: '{}' },
    { processKey: [{ required: '请输入流程定义Key' }] },
  );

  const openStart = () => {
    form.setValues({ processKey, variables: '{}' });
    setModalOpen(true);
  };

  const handleStart = async () => {
    if (!form.validate()) return;
    const v = form.values;
    let variables: Record<string, unknown> = {};
    try {
      variables = v.variables ? JSON.parse(v.variables) : {};
    } catch {
      toast.error('变量 JSON 格式错误');
      return;
    }
    await workflowApi.startProcess(v.processKey, variables);
    toast.success('流程已发起');
    setModalOpen(false);
    queryClient.invalidateQueries({ queryKey: ['wf-def'] });
  };

  const columns: Column<WorkflowDefinitionDTO>[] = [
    { key: 'defKey', title: '流程Key' },
    { key: 'defName', title: '流程名称' },
    { key: 'version', title: '版本' },
    { key: 'categoryId', title: '分类' },
    {
      key: 'status',
      title: '状态',
      render: (r) => <StatusBadge status={r.status ?? ''} label={r.status} />,
    },
    { key: 'scope', title: '范围' },
  ];

  return (
    <div className="space-y-4">
      <PageHeader title="流程管理" description="查看流程定义并发起流程" />
      <TableCard>
        <FilterBar>
          <Input
            placeholder="流程定义Key，如 leave"
            value={processKey}
            onChange={(e) => setProcessKey(e.target.value)}
          />
        </FilterBar>
        {def && (
          <Table
            rowKey={(r) => r.id ?? 0}
            columns={columns}
            data={[def]}
            loading={defQ.isFetching}
            emptyText="未找到流程定义"
          />
        )}
        <div className="pt-3">
          <Button type="button" variant="primary" onClick={openStart} disabled={!processKey}>
            发起流程
          </Button>
        </div>
      </TableCard>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="发起流程">
        <Form onSubmit={handleStart} className="space-y-4">
          <FormItem label="流程定义Key" required error={form.errors.processKey}>
            <Input value={form.values.processKey} onChange={(e) => form.setField('processKey', e.target.value)} />
          </FormItem>
          <FormItem label="流程变量(JSON)">
            <Input
              value={form.values.variables}
              onChange={(e) => form.setField('variables', e.target.value)}
              placeholder='{"days":3}'
            />
          </FormItem>
          <div className="flex justify-end gap-2 pt-2">
            <Button type="button" variant="ghost" onClick={() => setModalOpen(false)}>
              取消
            </Button>
            <Button type="submit">发起</Button>
          </div>
        </Form>
      </Modal>
    </div>
  );
}
