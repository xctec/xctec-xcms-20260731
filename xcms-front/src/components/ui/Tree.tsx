import { useEffect, useRef, useState } from 'react';
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

interface FlatNode {
  node: TreeNode;
  level: number;
  parentKey?: string;
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
  const [focusedKey, setFocusedKey] = useState<string | undefined>(selectedKey);
  const containerRef = useRef<HTMLDivElement>(null);

  const toggle = (k: string) => setExpanded((p) => ({ ...p, [k]: !p[k] }));

  // 当前可见节点（展开的才会入列）
  const visible: FlatNode[] = [];
  const walk = (ns: TreeNode[], level: number, parentKey?: string) => {
    ns.forEach((n) => {
      visible.push({ node: n, level, parentKey });
      if (n.children?.length && expanded[n.key]) walk(n.children, level + 1, n.key);
    });
  };
  walk(data, 0);

  useEffect(() => {
    if (focusedKey == null) return;
    const el = containerRef.current?.querySelector<HTMLElement>(`[data-tree-key="${focusedKey}"]`);
    el?.focus();
  }, [focusedKey]);

  const focusKey = (k?: string) => {
    if (k == null) return;
    setFocusedKey(k);
  };

  const onNodeKeyDown = (e: React.KeyboardEvent, node: TreeNode) => {
    if (node.disabled) return;
    const idx = visible.findIndex((v) => v.node.key === node.key);
    const hasChildren = !!node.children?.length;
    const isExpanded = !!expanded[node.key];

    switch (e.key) {
      case 'Enter':
      case ' ':
        e.preventDefault();
        onSelect?.(node.key, node);
        break;
      case 'ArrowDown':
        e.preventDefault();
        focusKey(visible[idx + 1]?.node.key);
        break;
      case 'ArrowUp':
        e.preventDefault();
        focusKey(visible[idx - 1]?.node.key);
        break;
      case 'ArrowRight':
        e.preventDefault();
        if (hasChildren && !isExpanded) toggle(node.key);
        else if (hasChildren && isExpanded) focusKey(visible[idx + 1]?.node.key);
        break;
      case 'ArrowLeft':
        e.preventDefault();
        if (hasChildren && isExpanded) toggle(node.key);
        else focusKey(visible[idx]?.parentKey);
        break;
    }
  };

  const renderNode = (flat: FlatNode): React.ReactNode => {
    const { node, level, parentKey } = flat;
    const hasChildren = !!node.children?.length;
    const isExpanded = !!expanded[node.key];
    const Icon = node.icon;
    return (
      <div key={node.key} draggable={draggable}>
        <div
          data-tree-key={node.key}
          role="treeitem"
          aria-selected={selectedKey === node.key}
          aria-expanded={hasChildren ? isExpanded : undefined}
          tabIndex={focusedKey === node.key || (focusedKey == null && selectedKey === node.key) ? 0 : -1}
          className={clsx(
            'flex items-center gap-1 rounded-sm transition-colors outline-none',
            'focus-visible:ring-2 focus-visible:ring-primary-500 focus-visible:ring-offset-1',
            node.disabled ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer hover:bg-gray-100',
            selectedKey === node.key && !node.disabled && 'bg-primary-50 text-primary-500'
          )}
          style={{ height: 'var(--menu-h)', paddingLeft: level * 20 + 4 }}
          onClick={() => {
            if (!node.disabled) {
              setFocusedKey(node.key);
              onSelect?.(node.key, node);
            }
          }}
          onKeyDown={(e) => onNodeKeyDown(e, node)}
        >
          {hasChildren ? (
            <button
              onClick={(e) => {
                e.stopPropagation();
                toggle(node.key);
              }}
              className="text-gray-400 hover:text-gray-700 inline-flex"
              tabIndex={-1}
              aria-label={isExpanded ? '收起' : '展开'}
            >
              <ChevronRight size={14} className={clsx('transition-transform', isExpanded && 'rotate-90')} />
            </button>
          ) : (
            <span className="w-[14px]" />
          )}
          {Icon && <Icon size={14} className="text-gray-400" />}
          <span className="text-[length:var(--fs-sm)] truncate">{node.title}</span>
        </div>
        {hasChildren && isExpanded && (
          <div>{node.children!.map((c) => renderNode({ node: c, level: level + 1, parentKey: node.key }))}</div>
        )}
      </div>
    );
  };

  return (
    <div className="select-none" role="tree" ref={containerRef} aria-label="树">
      {data.map((n) => renderNode({ node: n, level: 0 }))}
    </div>
  );
}
