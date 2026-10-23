import { Slot } from '@radix-ui/react-slot';
import { cva, type VariantProps } from 'class-variance-authority';
import * as React from 'react';
import { cn } from '@/lib/utils';

const buttonVariants = cva(
  'inline-flex shrink-0 items-center justify-center gap-2 rounded-[var(--radius-control)] border text-[13px] font-semibold transition-[background-color,border-color,color,transform] duration-150 ease-out active:translate-y-px active:duration-75 disabled:pointer-events-none disabled:opacity-45',
  {
    variants: {
      variant: {
        primary: 'border-accent-500 bg-transparent text-accent-300 hover:bg-[var(--action-subtle)] hover:text-accent-100',
        confirm: 'border-accent-500 bg-[var(--action-subtle)] text-accent-200 hover:bg-[var(--action-hover)]',
        secondary: 'border-strong-border bg-transparent text-borrowed hover:border-ghost hover:text-foreground',
        row: 'border-strong-border bg-transparent text-accent-200 hover:border-accent-500 hover:bg-[var(--row-hover)]',
        'row-overdue': 'border-accent-500 bg-[var(--row-strong)] text-accent-200 hover:bg-[var(--row-strong-hover)]',
        icon: 'border-strong-border bg-transparent text-muted hover:border-ghost hover:bg-sunken hover:text-foreground',
      },
      size: {
        form: 'h-11 px-4',
        dialog: 'h-[38px] px-4',
        list: 'h-9 px-3',
        header: 'h-9 px-3.5',
        table: 'h-[30px] px-3 text-xs',
        icon: 'size-9 p-0',
      },
    },
    defaultVariants: { variant: 'primary', size: 'form' },
  },
);

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement>, VariantProps<typeof buttonVariants> {
  asChild?: boolean;
}

export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ asChild = false, className, variant, size, type = 'button', ...props }, ref) => {
    const Component = asChild ? Slot : 'button';
    return <Component ref={ref} type={asChild ? undefined : type} className={cn(buttonVariants({ variant, size }), className)} {...props} />;
  },
);
Button.displayName = 'Button';

export { buttonVariants };
