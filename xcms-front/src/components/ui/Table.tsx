import { useState } from 'react';
import clsx from 'clsx';
import { ChevronDown, ChevronRight } from 'lucide-react';
import { Checkbox } from './FormControls';
import { TableSkeleton } from './Skeleton';
import { EmptyState } from './EmptyState';

export interface Column<T> {
  key: string;
  title: React.ReactNode;
  width?: number | string;
  render?: (record: T, index: number) => React.ReactNode;
  align?: 'left' | 'center' | 'right';
  /** 固定到左侧（目前支持首列） */
  fixed?: 'left';
}

interface TableProps<T> {
  columns: Column<T>[];
  data: T[];
  rowKey: (record: T) => string | number;
  loading?: boolean;
  selectable?: boolean;
  selectedKeys?: Array<string | number>;
  onSelectionChange?: (keys: Array<string | number>, rows: T[]) => void;
  expandable?: {
    expandedRowRender: (record: T) => React.ReactNode;
    rowExpandable?: (record: T) => boolean;
  };
  onRow?: (record: T, index: number) => React.HTMLAttributes<HTMLTableRowElement>;
  emptyText?: React.ReactNode;
}

const EXPAND_W = 36;
const CHECK_W = 44;

export function Table<T>({
  columns,
  data,
  rowKey,
  loading,
  selectable,
  selectedKeys = [],
  onSelectionChange,
  expandable,
  onRow,
  emptyText,
}: TableProps<T>) {
  const [expandedKeys, setExpandedKeys] = useState<Array<string | number>>([]);

  const checkLeft = expandable ? EXPAND_W : 0;
  const firstFixedLeft = checkLeft + (selectable ? CHECK_W : 0);

  const allKeys = data.map(rowKey);
  const allChecked = selectable && allKeys.length > 0 && allKeys.every((k) => selectedKeys.includes(k));
  const indeterminate = selectable && !allChecked && allKeys.some((k) => selectedKeys.includes(k));

  const toggleAll = () => {
    if (!onSelectionChange) return;
    if (allChecked) onSelectionChange([], []);
    else onSelectionChange(allKeys, data);
  };

  const toggleRow = (record: T) => {
    if (!onSelectionChange) return;
    const k = rowKey(record);
    const exists = selectedKeys.includes(k);
    const nextKeys = exists ? selectedKeys.filter((x) => x !== k) : [...selectedKeys, k];
    const nextRows = data.filter((r) => nextKeys.includes(rowKey(r)));
    onSelectionChange(nextKeys, nextRows);
  };

  const toggleExpand = (k: string | number) => {
    setExpandedKeys((prev) => (prev.includes(k) ? prev.filter((x) => x !== k) : [...prev, k]));
  };

  const alignCls = (a?: string) =>
    a === 'center' ? 'text-center' : a === 'right' ? 'text-right' : 'text-left';

  return (
    <div className="border border-gray-200 rounded-[var(--card-radius)] overflow-x-auto bg-white">
      <table className="w-full border-collapse text-[length:var(--fs)]">
        <thead>
          <tr className="border-b border-gray-200">
            {expandable && (
              <th
                className="sticky left-0 z-20 bg-[var(--c-header-bg)] border-r border-gray-200"
                style={{ width: EXPAND_W, height: 'var(--header-h)' }}
              />
            )}
            {selectable && (
              <th
                className="sticky z-20 bg-[var(--c-header-bg)] border-r border-gray-200 px-2"
                style={{ left: checkLeft, width: CHECK_W, height: 'var(--header-h)' }}
              >
                <Checkbox checked={!!allChecked} indeterminate={!!indeterminate} onChange={toggleAll} />
              </th>
            )}
            {columns.map((col) => (
              <th
                key={col.key}
                className={clsx(
                  'bg-[var(--c-header-bg)] text-[length:var(--fs-sm)] font-medium text-gray-500 px-[var(--gap-sm)] whitespace-nowrap',
                  col.fixed && 'sticky z-10 border-r border-gray-200',
                  alignCls(col.align)
                )}
                style={{
                  width: col.width,
                  height: 'var(--header-h)',
                  left: col.fixed ? firstFixedLeft : undefined,
                }}
              >
                {col.title}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr>
              <td colSpan={columns.length + (expandable ? 1 : 0) + (selectable ? 1 : 0)} className="p-0">
                <TableSkeleton rows={6} cols={columns.length} />
              </td>
            </tr>
          ) : data.length === 0 ? (
            <tr>
              <td colSpan={columns.length + (expandable ? 1 : 0) + (selectable ? 1 : 0)} className="p-0">
                {emptyText ?? <EmptyState title="暂无数据" />}
              </td>
            </tr>
          ) : (
            data.map((record, index) => {
              const k = rowKey(record);
              const checked = selectedKeys.includes(k);
              const expanded = expandedKeys.includes(k);
              const canExpand = expandable && (!expandable.rowExpandable || expandable.rowExpandable(record));
              const rowAttrs = onRow?.(record, index) ?? {};
              return (
                <FragmentTag key={k}>
                  <tr
                    {...rowAttrs}
                    className={clsx(
                      'border-b border-gray-200 transition-colors',
                      checked ? 'bg-primary-50' : 'hover:bg-[var(--c-hover)]'
                    )}
                  >
                    {expandable && (
                      <td
                        className="sticky left-0 z-10 bg-inherit border-r border-gray-200 text-center"
                        style={{ width: EXPAND_W, height: 'var(--row-h)' }}
                      >
                        {canExpand && (
                          <button
                            onClick={() => toggleExpand(k)}
                            className="text-gray-400 hover:text-gray-700 inline-flex"
                          >
                            {expanded ? <ChevronDown size={15} /> : <ChevronRight size={15} />}
                          </button>
                        )}
                      </td>
                    )}
                    {selectable && (
                      <td
                        className="sticky z-10 bg-inherit border-r border-gray-200 px-2"
                        style={{ left: checkLeft, height: 'var(--row-h)' }}
                      >
                        <Checkbox checked={checked} onChange={() => toggleRow(record)} />
                      </td>
                    )}
                    {columns.map((col) => (
                      <td
                        key={col.key}
                        className={clsx(
                          'px-[var(--gap-sm)] text-gray-700 whitespace-nowrap',
                          col.fixed && 'sticky z-10 bg-inherit border-r border-gray-200',
                          alignCls(col.align)
                        )}
                        style={{ height: 'var(--row-h)', left: col.fixed ? firstFixedLeft : undefined }}
                      >
                        {col.render ? col.render(record, index) : (record as Record<string, unknown>)[col.key] as React.ReactNode}
                      </td>
                    ))}
                  </tr>
                  {expandable && expanded && canExpand && (
                    <tr>
                      <td
                        colSpan={columns.length + (expandable ? 1 : 0) + (selectable ? 1 : 0)}
                        className="bg-[var(--c-bg)] px-[var(--card-p)]"
                      >
                        {expandable.expandedRowRender(record)}
                      </td>
                    </tr>
                  )}
                </FragmentTag>
              );
            })
          )}
        </tbody>
      </table>
    </div>
  );
}

// 局部 Fragment 别名，避免顶层 import 与 JSX 冲突
const FragmentTag = ({ children }: { children: React.ReactNode }) => <>{children}</>;
