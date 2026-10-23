import { CaretDownIcon } from '@phosphor-icons/react';
import * as React from 'react';
import { cn } from '@/lib/utils';

type FieldProps = { label: string; id: string; hint?: string };
const controlClass = 'h-[42px] w-full rounded-[var(--radius-control)] border border-strong-border bg-sunken px-3 text-sm text-foreground placeholder:text-faint disabled:cursor-not-allowed disabled:opacity-45';

export const Input = React.forwardRef<HTMLInputElement, React.InputHTMLAttributes<HTMLInputElement> & FieldProps>(
  ({ label, id, hint, className, ...props }, ref) => (
    <label htmlFor={id} className="grid gap-[7px] text-xs text-borrowed">
      <span>{label}</span>
      <input ref={ref} id={id} className={cn(controlClass, className)} {...props} />
      {hint ? <span className="text-[11px] text-faint">{hint}</span> : null}
    </label>
  ),
);
Input.displayName = 'Input';

export const Select = React.forwardRef<HTMLSelectElement, React.SelectHTMLAttributes<HTMLSelectElement> & FieldProps>(
  ({ label, id, hint, className, children, ...props }, ref) => (
    <label htmlFor={id} className="grid gap-[7px] text-xs text-borrowed">
      <span>{label}</span>
      <span className="relative">
        <select ref={ref} id={id} className={cn(controlClass, 'appearance-none pr-9', className)} {...props}>{children}</select>
        <CaretDownIcon aria-hidden="true" className="pointer-events-none absolute right-3 top-1/2 size-[13px] -translate-y-1/2 text-faint" />
      </span>
      {hint ? <span className="text-[11px] text-faint">{hint}</span> : null}
    </label>
  ),
);
Select.displayName = 'Select';

export const Textarea = React.forwardRef<HTMLTextAreaElement, React.TextareaHTMLAttributes<HTMLTextAreaElement> & FieldProps>(
  ({ label, id, hint, className, ...props }, ref) => (
    <label htmlFor={id} className="grid gap-[7px] text-xs text-borrowed">
      <span>{label}</span>
      <textarea ref={ref} id={id} className={cn(controlClass, 'h-[88px] resize-none py-3', className)} {...props} />
      {hint ? <span className="text-[11px] text-faint">{hint}</span> : null}
    </label>
  ),
);
Textarea.displayName = 'Textarea';
