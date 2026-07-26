import { useEffect, useRef } from 'react';
import clsx from 'clsx';
import { ChevronDown } from 'lucide-react';

const fieldBase =
  'w-full bg-white border border-gray-300 rounded-md text-[length:var(--fs)] text-gray-900 px-3 placeholder:text-gray-400 focus:outline-none focus:border-primary-500 focus:ring-1 focus:ring-primary-500 transition-colors disabled:opacity-50 disabled:bg-gray-100';

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  invalid?: boolean;
}

export function Input({ className, invalid, ...rest }: InputProps) {
  return (
    <input
      className={clsx(fieldBase, 'h-[var(--input-h)]', invalid && 'border-danger-500 focus:border-danger-500 focus:ring-danger-500', className)}
      {...rest}
    />
  );
}

export interface TextareaProps extends React.TextareaHTMLAttributes<HTMLTextAreaElement> {
  invalid?: boolean;
}

export function Textarea({ className, invalid, ...rest }: TextareaProps) {
  return (
    <textarea
      className={clsx(fieldBase, 'py-2 min-h-[80px]', invalid && 'border-danger-500', className)}
      {...rest}
    />
  );
}

export interface SelectProps extends React.SelectHTMLAttributes<HTMLSelectElement> {
  invalid?: boolean;
  options?: Array<{ label: React.ReactNode; value: string | number }>;
}

export function Select({ className, invalid, options, children, ...rest }: SelectProps) {
  return (
    <div className="relative">
      <select
        className={clsx(fieldBase, 'h-[var(--input-h)] appearance-none pr-8', invalid && 'border-danger-500', className)}
        {...rest}
      >
        {options ? options.map((o) => <option key={o.value} value={o.value}>{o.label}</option>) : children}
      </select>
      <ChevronDown size={14} className="absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none" />
    </div>
  );
}

export interface CheckboxProps extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'type' | 'onChange'> {
  onChange?: (checked: boolean) => void;
  indeterminate?: boolean;
}

export function Checkbox({ checked, onChange, indeterminate, disabled, ...rest }: CheckboxProps) {
  const ref = useRef<HTMLInputElement>(null);
  useEffect(() => {
    if (ref.current) ref.current.indeterminate = !!indeterminate;
  }, [indeterminate]);
  return (
    <input
      ref={ref}
      type="checkbox"
      className="cursor-pointer rounded border-gray-300 text-primary-500 focus:ring-primary-500 disabled:cursor-not-allowed"
      checked={checked}
      disabled={disabled}
      onChange={(e) => onChange?.(e.target.checked)}
      {...rest}
    />
  );
}

export interface RadioProps extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'type'> {
  label?: React.ReactNode;
}

export function Radio({ label, className, ...rest }: RadioProps) {
  return (
    <label className={clsx('inline-flex items-center gap-1.5 cursor-pointer text-[length:var(--fs)] text-gray-700', className)}>
      <input type="radio" className="cursor-pointer border-gray-300 text-primary-500 focus:ring-primary-500" {...rest} />
      {label && <span>{label}</span>}
    </label>
  );
}

export interface RadioGroupProps {
  options: Array<{ label: React.ReactNode; value: string | number }>;
  value?: string | number;
  onChange?: (value: string | number) => void;
  name?: string;
  className?: string;
}

export function RadioGroup({ options, value, onChange, name, className }: RadioGroupProps) {
  return (
    <div className={clsx('flex items-center gap-[var(--gap)]', className)}>
      {options.map((o) => (
        <Radio
          key={o.value}
          name={name}
          label={o.label}
          value={o.value}
          checked={value === o.value}
          onChange={() => onChange?.(o.value)}
        />
      ))}
    </div>
  );
}

/** 日期选择：原生 date input 包装，消费 --input-h */
export function DatePicker({ className, invalid, ...rest }: InputProps) {
  return <Input type="date" className={clsx(invalid && 'border-danger-500', className)} invalid={invalid} {...rest} />;
}
