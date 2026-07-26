import { useState } from 'react';
import clsx from 'clsx';

export interface FormRule {
  required?: boolean | string;
  pattern?: { value: RegExp; message: string };
  min?: number;
  max?: number;
  message?: string;
}

export interface FormApi<T> {
  values: T;
  errors: Partial<Record<keyof T, string>>;
  setField: (key: keyof T, value: unknown) => void;
  setValues: React.Dispatch<React.SetStateAction<T>>;
  setErrors: React.Dispatch<React.SetStateAction<Partial<Record<keyof T, string>>>>;
  validate: () => boolean;
  reset: () => void;
}

/**
 * 受控表单 hook：值管理 + 规则校验 + 错误提示
 */
export function useForm<T extends Record<string, unknown>>(initial: T, rules?: Partial<Record<keyof T, FormRule[]>>): FormApi<T> {
  const [values, setValues] = useState<T>(initial);
  const [errors, setErrors] = useState<Partial<Record<keyof T, string>>>({});

  const setField = (key: keyof T, value: unknown) => setValues((p) => ({ ...p, [key]: value }));

  const validate = (): boolean => {
    const next: Partial<Record<keyof T, string>> = {};
    let ok = true;
    if (rules) {
      (Object.keys(rules) as Array<keyof T>).forEach((k) => {
        const rs = rules[k];
        if (!rs) return;
        const v = values[k];
        for (const r of rs) {
          if (r.required && (v === '' || v === null || v === undefined)) {
            next[k] = typeof r.required === 'string' ? r.required : r.message || '该项必填';
            ok = false;
            break;
          }
          if (r.pattern && v != null && !r.pattern.value.test(String(v))) {
            next[k] = r.pattern.message;
            ok = false;
            break;
          }
          if (r.min != null && typeof v === 'string' && v.length < r.min) {
            next[k] = r.message || `不少于 ${r.min} 个字符`;
            ok = false;
            break;
          }
          if (r.max != null && typeof v === 'string' && v.length > r.max) {
            next[k] = r.message || `不超过 ${r.max} 个字符`;
            ok = false;
            break;
          }
        }
      });
    }
    setErrors(next);
    return ok;
  };

  const reset = () => {
    setValues(initial);
    setErrors({});
  };

  return { values, errors, setField, setValues, setErrors, validate, reset };
}

interface FormProps {
  onSubmit?: () => void;
  children: React.ReactNode;
  className?: string;
}

export function Form({ onSubmit, children, className }: FormProps) {
  return (
    <form
      className={clsx('space-y-[var(--gap-sm)]', className)}
      onSubmit={(e) => {
        e.preventDefault();
        onSubmit?.();
      }}
    >
      {children}
    </form>
  );
}

interface FormItemProps {
  label?: React.ReactNode;
  required?: boolean;
  error?: string;
  htmlFor?: string;
  children: React.ReactNode;
  className?: string;
}

export function FormItem({ label, required, error, htmlFor, children, className }: FormItemProps) {
  return (
    <div className={className}>
      {label && (
        <label htmlFor={htmlFor} className="block text-[length:var(--fs-sm)] font-medium text-gray-600 mb-1">
          {label}
          {required && <span className="text-danger-500 ml-0.5">*</span>}
        </label>
      )}
      {children}
      {error && <p className="text-[length:var(--fs-xs)] text-danger-500 mt-1">{error}</p>}
    </div>
  );
}
