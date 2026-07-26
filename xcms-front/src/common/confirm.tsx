import { useState, useCallback } from 'react';
import type { ReactNode } from 'react';
import { Modal, Button } from '@/components/ui';

export interface ConfirmOptions {
  title: string;
  description?: ReactNode;
  okText?: string;
  cancelText?: string;
  danger?: boolean;
  onOk: () => void | Promise<void>;
}

/**
 * 基于组件库 Modal 实现的确认弹窗 Hook。
 * 返回 confirm 触发函数与需要渲染到页面中的 Confirm 节点。
 */
export function useConfirm() {
  const [opts, setOpts] = useState<ConfirmOptions | null>(null);

  const confirm = useCallback((o: ConfirmOptions) => setOpts(o), []);
  const close = useCallback(() => setOpts(null), []);

  const handleOk = async () => {
    const current = opts;
    if (!current) return;
    await current.onOk();
    setOpts(null);
  };

  const Confirm = opts ? (
    <Modal
      open
      title={opts.title}
      onClose={close}
      footer={
        <>
          <Button variant="ghost" onClick={close}>
            {opts.cancelText ?? '取消'}
          </Button>
          <Button variant={opts.danger ? 'danger' : 'primary'} onClick={handleOk}>
            {opts.okText ?? '确定'}
          </Button>
        </>
      }
    >
      <div className="text-sm leading-6 text-gray-600">{opts.description}</div>
    </Modal>
  ) : null;

  return { confirm, Confirm };
}
