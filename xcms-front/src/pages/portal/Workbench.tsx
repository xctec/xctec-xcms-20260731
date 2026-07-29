import { useQuery } from '@tanstack/react-query';
import { PageHeader, TableCard } from '@/components/ui';
import { portalApi } from '@/api/portal';
import type { Schemas } from '@/types/api-helpers';

type PortalWorkbenchDTO = Schemas['PortalWorkbenchDTO'];

export default function Workbench() {
  const { data, isFetching } = useQuery({
    queryKey: ['portal-workbench'],
    queryFn: () => portalApi.getWorkbench(),
  });
  const wb = data as PortalWorkbenchDTO | undefined;
  const profile = wb?.profile;
  const quickEntries = (wb?.quickEntries ?? []) as any[];
  const surfaces = (wb?.surfaces ?? []) as any[];

  return (
    <div className="space-y-4">
      <PageHeader title="工作台" description={`欢迎，${profile?.realName ?? profile?.username ?? ''}`} />
      <TableCard>
        <div className="mb-2 font-medium">快捷入口</div>
        <div className="grid grid-cols-2 gap-3 md:grid-cols-4">
          {quickEntries.map((q: any) => (
            <a
              key={q.id}
              href={q.url}
              target={q.target || '_self'}
              rel="noreferrer"
              className="rounded-lg border border-gray-200 p-4 hover:border-blue-400"
            >
              <div className="font-medium">{q.title}</div>
              <div className="mt-1 truncate text-xs text-gray-500">{q.url}</div>
            </a>
          ))}
          {!quickEntries.length && <div className="text-sm text-gray-400">暂无快捷入口</div>}
        </div>
      </TableCard>

      {surfaces.map((s: any) => (
        <TableCard key={s.surface}>
          <div className="mb-2 font-medium">{s.surface}</div>
          <div className="flex flex-wrap gap-2">
            {(s.menus ?? []).map((m: any) => (
              <span key={m.id ?? m.menuCode} className="rounded bg-gray-100 px-2 py-1 text-sm">
                {m.menuName}
              </span>
            ))}
          </div>
        </TableCard>
      ))}
      {isFetching && <div className="text-sm text-gray-400">加载中…</div>}
    </div>
  );
}
