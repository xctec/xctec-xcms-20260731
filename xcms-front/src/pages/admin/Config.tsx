import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { PageHeader, TableCard, Table, Button, Input, toast, type Column } from '@/components/ui';
import { configApi } from '@/api/configuration';

export default function Config() {
  const queryClient = useQueryClient();
  const [active, setActive] = useState<'params' | 'dicts' | 'features'>('params');
  const [dictType, setDictType] = useState('');

  const paramsQ = useQuery({ queryKey: ['config-params'], queryFn: async () => (await configApi.listParams()) as any });
  const dictQ = useQuery({
    queryKey: ['config-dicts', dictType],
    queryFn: async () => (await configApi.getDictionaries(dictType || undefined)) as any,
  });
  const featureQ = useQuery({
    queryKey: ['config-features'],
    queryFn: async () => (await configApi.getFeatureConfig(undefined as any)) as any,
  });

  const params = (paramsQ.data?.data ?? paramsQ.data ?? []) as any[];
  const dicts = (dictQ.data?.data ?? dictQ.data ?? []) as any[];
  const features = (featureQ.data?.data ?? featureQ.data ?? []) as any[];

  const toggleFeature = async (code: string, enabled: boolean) => {
    try {
      await configApi.toggleFeature(undefined as any, code, enabled);
      toast.success('已更新');
      queryClient.invalidateQueries({ queryKey: ['config-features'] });
    } catch {
      toast.error('操作失败');
    }
  };

  const paramCols: Column<any>[] = [
    { key: 'paramKey', title: '参数键' },
    { key: 'paramValue', title: '参数值' },
    { key: 'description', title: '描述' },
  ];
  const dictCols: Column<any>[] = [
    { key: 'dictType', title: '字典类型' },
    { key: 'dictCode', title: '编码' },
    { key: 'dictLabel', title: '名称' },
    { key: 'description', title: '描述' },
  ];
  const featureCols: Column<any>[] = [
    { key: 'featureCode', title: '功能编码' },
    { key: 'featureName', title: '功能名称' },
    {
      key: 'enabled',
      title: '状态',
      render: (r: any) => (
        <Button size="sm" variant="ghost" onClick={() => toggleFeature(r.featureCode, !r.enabled)}>
          {r.enabled ? '禁用' : '启用'}
        </Button>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <PageHeader title="系统配置" description="参数、字典与功能开关管理" />
      <div className="flex gap-2">
        <Button variant={active === 'params' ? 'primary' : 'ghost'} onClick={() => setActive('params')}>
          参数配置
        </Button>
        <Button variant={active === 'dicts' ? 'primary' : 'ghost'} onClick={() => setActive('dicts')}>
          数据字典
        </Button>
        <Button variant={active === 'features' ? 'primary' : 'ghost'} onClick={() => setActive('features')}>
          功能开关
        </Button>
      </div>

      {active === 'params' && (
        <TableCard>
          <Table
            rowKey={(r: any) => r.id ?? r.paramKey ?? Math.random()}
            columns={paramCols}
            data={params}
            loading={paramsQ.isFetching}
            emptyText="暂无参数"
          />
        </TableCard>
      )}
      {active === 'dicts' && (
        <TableCard>
          <div className="mb-3">
            <Input
              placeholder="字典类型过滤"
              value={dictType}
              onChange={(e) => {
                setDictType(e.target.value);
                queryClient.invalidateQueries({ queryKey: ['config-dicts', e.target.value] });
              }}
            />
          </div>
          <Table
            rowKey={(r: any) => r.id ?? r.dictCode ?? Math.random()}
            columns={dictCols}
            data={dicts}
            loading={dictQ.isFetching}
            emptyText="暂无字典"
          />
        </TableCard>
      )}
      {active === 'features' && (
        <TableCard>
          <Table
            rowKey={(r: any) => r.featureCode ?? Math.random()}
            columns={featureCols}
            data={features}
            loading={featureQ.isFetching}
            emptyText="暂无功能"
          />
        </TableCard>
      )}
    </div>
  );
}
