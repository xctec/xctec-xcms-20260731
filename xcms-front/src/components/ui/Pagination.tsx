import { ChevronLeft, ChevronRight } from 'lucide-react';

interface PaginationProps {
  page: number;
  total: number;
  size: number;
  onChange: (page: number) => void;
}

export function Pagination({ page, total, size, onChange }: PaginationProps) {
  const totalPages = Math.ceil(total / size) || 1;
  return (
    <div className="flex items-center justify-between border-t border-gray-100 px-4 py-3">
      <span className="text-xs text-gray-400">
        共 <span className="font-semibold text-gray-700">{total}</span> 条
      </span>
      <div className="flex items-center gap-1">
        <button
          onClick={() => onChange(page - 1)}
          disabled={page <= 1}
          className="flex h-7 w-7 items-center justify-center rounded border border-gray-200 text-gray-400 disabled:opacity-40"
        >
          <ChevronLeft size={14} />
        </button>
        <button className="flex h-7 min-w-[28px] items-center justify-center rounded bg-primary-500 px-2 text-xs font-medium text-white">
          {page}
        </button>
        <span className="px-1 text-xs text-gray-400">/ {totalPages}</span>
        <button
          onClick={() => onChange(page + 1)}
          disabled={page >= totalPages}
          className="flex h-7 w-7 items-center justify-center rounded border border-gray-200 text-gray-500 disabled:opacity-40"
        >
          <ChevronRight size={14} />
        </button>
      </div>
    </div>
  );
}
