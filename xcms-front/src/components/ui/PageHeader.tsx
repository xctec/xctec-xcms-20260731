import { Search, Filter, Columns3 } from 'lucide-react';

interface PageHeaderProps {
  title: string;
  description?: string;
  actions?: React.ReactNode;
}

export function PageHeader({ title, description, actions }: PageHeaderProps) {
  return (
    <div className="mb-6 flex items-end justify-between">
      <div>
        <h1 className="text-xl font-bold text-gray-900">{title}</h1>
        {description && <p className="mt-1 text-sm text-gray-500">{description}</p>}
      </div>
      {actions && <div className="flex gap-2">{actions}</div>}
    </div>
  );
}

interface FilterBarProps {
  children?: React.ReactNode;
  searchValue?: string;
  searchPlaceholder?: string;
  onSearch?: (val: string) => void;
  filters?: React.ReactNode;
}

export function FilterBar({ children, searchValue, searchPlaceholder = '搜索...', onSearch, filters }: FilterBarProps) {
  return (
    <div className="flex items-center justify-between border-b border-gray-100 px-4 py-3">
      <div className="flex gap-2">
        {filters}
        {children}
      </div>
      <div className="flex gap-2">
        {onSearch && (
          <div className="relative w-60">
            <Search size={14} className="absolute left-2.5 top-1/2 -translate-y-1/2 text-gray-400" />
            <input
              value={searchValue}
              onChange={(e) => onSearch(e.target.value)}
              placeholder={searchPlaceholder}
              className="h-8 w-full rounded-md border border-gray-300 pl-8 pr-3 text-xs outline-none focus:border-primary-500"
            />
          </div>
        )}
        <button className="flex h-8 w-8 items-center justify-center rounded-md border border-gray-300 text-gray-400 hover:text-gray-600">
          <Filter size={14} />
        </button>
        <button className="flex h-8 w-8 items-center justify-center rounded-md border border-gray-300 text-gray-400 hover:text-gray-600">
          <Columns3 size={14} />
        </button>
      </div>
    </div>
  );
}

export function TableCard({ children }: { children: React.ReactNode }) {
  return (
    <div className="overflow-hidden rounded-lg border border-gray-200 bg-white shadow-card">{children}</div>
  );
}
