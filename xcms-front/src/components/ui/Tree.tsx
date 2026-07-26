import { useState } from 'react';
import clsx from 'clsx';
import type { LucideIcon } from 'lucide-react';
import { ChevronRight } from 'lucide-react';

export interface TreeNode {
  key: string;
  title: React.ReactNode;
  children?: TreeNode[];
  icon?: LucideIcon;
  disabled?: boolean;
}

interface TreeProps {
  data: TreeNode[];
  selectedKey?: string;
  onSelect?: (key: string, node: TreeNode) => void;
  defaultExpandAll?: boolean;
  /** 拖拽排序占位（后续阶段实现完整 onDrop） */
  draggable?: boolean;
}

/**
 * 树形组件（见设计系统 §9.5）：缩进 20px/级、展开图标、选中态、拖拽占位
 */
export function Tree({ data, selectedKey, onSelect, defaultExpandAll, draggable }: TreeProps) {
  const [expanded, setExpanded] = useState<Record<string, boolean>>(() => {
    if (!defaultExpandAll) return {};
    const m: Record<string, boolean> = {};
    const walk = (ns: TreeNode[]) =>
      ns.forEach((n) => {
        if (n.children?.length) {
          m[n.key] = true;
          walk(n.children);
        }
      });
    walk(data);
    return m;
  });

  const toggle = (k: string) => setExpanded((p) => ({ ...p, [k]: !p[k] }));

  const renderNode = (node: TreeNode, level: number): React.ReactNode => {
    const hasChildren = !!node.children?.length;
    const isExpanded = !!expanded[node.key];
    const Icon = node.icon;
    return (
      <div key={node.key} draggable={draggable}>
        <div
          className={clsx(
            'flex items-center gap-1 rounded-sm transition-colors',
            node.disabled ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer hover:bg-gray-100',
            selectedKey === node.key && !node.disabled && 'bg-primary-50 text-primary-500'
          )}
          style={{ height: 'var(--menu-h)', paddingLeft: level * 20 + 4 }}
          onClick={() => !node.disabled && onSelect?.(node.key, node)}
        >
          {hasChildren ? (
            <button
              onClick={(e) => {
                e.stopPropagation();
                toggle(node.key);
              }}
              className="text-gray-400 hover:text-gray-700 inline-flex"
            >
              <ChevronRight size={14} className={clsx('transition-transform', isExpanded && 'rotate-90')} />
            </button>
          ) : (
            <span className="w-[14px]" />
          )}
          {Icon && <Icon size={14} className="text-gray-400" />}
          <span className="text-[length:var(--fs-sm)] truncate">{node.title}</span>
        </div>
        {hasChildren && isExpanded && <div>{node.children!.map((c) => renderNode(c, level + 1))}</div>}
      </div>
    );
  };

  return <div className="select-none">{data.map((n) => renderNode(n, 0))}</div>;
}
