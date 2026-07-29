import { useQuery } from '@tanstack/react-query';
import { PageHeader, TableCard, Table, type Column } from '@/components/ui';
import { operationApi } from '@/api/operation';
import type { Schemas } from '@/types/api-helpers';

type DashboardDTO = Schemas['DashboardDTO'];

export default function Operation() {
  const dashQ = useQuery({ queryKey: ['operation-dashboard'], queryFn: () => operationApi.getDashboard() });
  const quotaQ = useQuery({
    queryKey: ['operation-quota'],
    queryFn: async () => (await operationApi.getQuotaReport({ page: 1, size: 10 })) as any,
  });
  const alertQ = useQuery({
    queryKey: ['operation-alerts'],
    queryFn: async () => (await operationApi.getAlertRules()) as any,
  });
  const tenantQ = useQuery({
    queryKey: ['operation-tenants'],
    queryFn: async () => (await operationApi.getTenantMetrics(undefined as any)) as any,
  });

  const dash = dashQ.data;
  const quota = (quotaQ.data?.data ?? quotaQ.data ?? []) as any[];
  const alerts = (alertQ.data?.data ?? alertQ.data ?? []) as any[];
  const tenants = (tenantQ.data?.data ?? tenantQ.data ?? []) as any[];

  const stats = [
    { label: '实例总数', value: dash?.healthTotal ?? 0 },
    { label: '健康实例', value: dash?.healthUp ?? 0 },
    { label: '指标总数', value: dash?.metricsTotal ?? 0 },
    { label: '今日操作日志', value: dash?.operationLogsToday ?? 0 },
    { label: '启用告警规则', value: dash?.alertRulesEnabled ?? 0 },
  ];

  const quotaCols: Column<any>[] = [
    { key: 'tenantId', title: '租户' },
    { key: 'quotaType', title: '配额类型' },
    { key: 'used', title: '已用' },
    { key: 'total', title: '总额' },
  ];
  const alertCols: Column<any>[] = [
    { key: 'ruleName', title: '规则名称' },
    { key: 'metric', title: '指标' },
    { key: 'threshold', title: '阈值' },
    { key: 'enabled', title: '启用', render: (r: any) => (r.enabled ? '是' : '否') },
  ];
  const tenantCols: Column<any>[] = [
    { key: 'tenantId', title: '租户' },
    { key: 'tenantName', title: '名称' },
    { key: 'status', title: '状态' },
  ];

  const metrics = dash?.latestMetrics
    ? Object.entries(dash.latestMetrics).map(([k, v]) => ({ id: k, k, v }))
    : [];

  return (
    <div className="space-y-4">
      <PageHeader title="运营看板" description="平台运行状态与配额监控" />
      <div className="grid grid-cols-2 gap-3 md:grid-cols-5">
        {stats.map((s) => (
          <div key={s.label} className="rounded-lg border border-gray-200 p-4">
            <div className="text-sm text-gray-500">{s.label}</div>
            <div className="mt-1 text-2xl font-semibold">{s.value}</div>
          </div>
        ))}
      </div>

      {metrics.length > 0 && (
        <TableCard>
          <div className="mb-2 font-medium">最新指标</div>
          <Table
            rowKey={(r: any) => r.id}
            columns={[
              { key: 'k', title: '指标' },
              { key: 'v', title: '值', render: (r: any) => JSON.stringify(r.v) },
            ]}
            data={metrics}
          />
        </TableCard>
      )}

      <TableCard>
        <div className="mb-2 font-medium">租户指标</div>
        <Table
          rowKey={(r: any) => r.tenantId ?? Math.random()}
          columns={tenantCols}
          data={tenants}
          loading={tenantQ.isFetching}
          emptyText="暂无数据"
        />
      </TableCard>

      <TableCard>
        <div className="mb-2 font-medium">配额报表</div>
        <Table
          rowKey={(r: any) => r.id ?? Math.random()}
          columns={quotaCols}
          data={quota}
          loading={quotaQ.isFetching}
          emptyText="暂无数据"
        />
      </TableCard>

      <TableCard>
        <div className="mb-2 font-medium">告警规则</div>
        <Table
          rowKey={(r: any) => r.id ?? Math.random()}
          columns={alertCols}
          data={alerts}
          loading={alertQ.isFetching}
          emptyText="暂无数据"
        />
      </TableCard>
    </div>
  );
}
