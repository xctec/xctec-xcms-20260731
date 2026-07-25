import clsx from 'clsx';
import type { LucideIcon } from 'lucide-react';

type Variant = 'primary' | 'secondary' | 'danger' | 'ghost';
type Size = 'sm' | 'md';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  size?: Size;
  icon?: LucideIcon;
}

const variantCls: Record<Variant, string> = {
  primary: 'bg-primary-500 text-white hover:bg-primary-600 active:bg-primary-700 shadow-sm shadow-primary-500/20',
  secondary: 'bg-white border border-gray-300 text-gray-600 hover:border-primary-500 hover:text-primary-500',
  danger: 'bg-danger-500 text-white hover:bg-red-600',
  ghost: 'bg-transparent text-primary-500 hover:bg-primary-50',
};

const sizeCls: Record<Size, string> = {
  sm: 'h-7 px-2.5 text-xs gap-1',
  md: 'h-8 px-4 text-[13px] gap-1.5',
};

export function Button({ variant = 'primary', size = 'md', icon: Icon, children, className, ...rest }: ButtonProps) {
  return (
    <button
      className={clsx(
        'inline-flex items-center justify-center rounded-md font-medium transition-colors duration-150 disabled:opacity-50 disabled:cursor-not-allowed',
        variantCls[variant],
        sizeCls[size],
        className
      )}
      {...rest}
    >
      {Icon && <Icon size={size === 'sm' ? 13 : 15} />}
      {children}
    </button>
  );
}
