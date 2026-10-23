'use client';

import * as React from 'react';
import { cn } from '@/lib/utils';

export type EquipmentStatus = 'available' | 'borrowed' | 'maintenance';
const statusLabels: Record<EquipmentStatus, string> = { available: 'Disponível', borrowed: 'Emprestado', maintenance: 'Manutenção' };
const statusStyles: Record<EquipmentStatus, string> = {
  available: 'border-[var(--available-border)] bg-[var(--available-bg)] text-available',
  borrowed: 'border-[var(--borrowed-border)] bg-[var(--borrowed-bg)] text-borrowed',
  maintenance: 'border-[var(--maintenance-border)] bg-[var(--maintenance-bg)] text-maintenance',
};

export function StatusPill({ status }: { status: EquipmentStatus }) {
  return <span className={cn('inline-flex rounded-[var(--radius-pill)] border px-[9px] py-[3px] text-[11px] font-semibold', statusStyles[status])}>{statusLabels[status]}</span>;
}

export function FilterChip({ active, children, ...props }: React.ButtonHTMLAttributes<HTMLButtonElement> & { active?: boolean }) {
  return <button type="button" aria-pressed={active} className={cn('rounded-[var(--radius-pill)] border px-3 py-1.5 text-xs transition-[background-color,border-color,color] duration-150', active ? 'border-accent-700 bg-accent-800 text-accent-200' : 'border-frame bg-transparent text-borrowed hover:border-strong-border hover:text-foreground')} {...props}>{children}</button>;
}

type SegmentOption<T extends string> = { value: T; label: string };

export function SegmentedControl<T extends string>({ value, options, onChange, label }: { value: T; options: SegmentOption<T>[]; onChange: (value: T) => void; label: string }) {
  const onKeyDown = (event: React.KeyboardEvent<HTMLDivElement>) => {
    if (!['ArrowLeft', 'ArrowRight', 'Home', 'End'].includes(event.key)) return;
    event.preventDefault();
    const index = options.findIndex((option) => option.value === value);
    const nextIndex = event.key === 'Home' ? 0 : event.key === 'End' ? options.length - 1 : event.key === 'ArrowRight' ? (index + 1) % options.length : (index - 1 + options.length) % options.length;
    onChange(options[nextIndex].value);
    document.getElementById(`segment-${options[nextIndex].value}`)?.focus();
  };

  return (
    <div role="radiogroup" aria-label={label} onKeyDown={onKeyDown} className="inline-flex overflow-hidden rounded-[var(--radius-control)] border border-strong-border">
      {options.map((option, index) => {
        const selected = option.value === value;
        return <button key={option.value} id={`segment-${option.value}`} type="button" role="radio" aria-checked={selected} tabIndex={selected ? 0 : -1} onClick={() => onChange(option.value)} className={cn('px-[18px] py-[9px] text-[13px] text-borrowed transition-colors duration-150', index > 0 && 'border-l border-strong-border', selected && 'bg-accent-800 font-semibold text-accent-200')}>{option.label}</button>;
      })}
    </div>
  );
}

export function LimitSegments({ current, limit = 3 }: { current: number; limit?: number }) {
  const safeCurrent = Math.max(0, Math.min(current, 3));
  return (
    <div className="grid gap-1.5" role="img" aria-label={`${current} de ${limit} empréstimos em uso`}>
      <div className="flex gap-[5px]" aria-hidden="true">{[0, 1, 2].map((segment) => <span key={segment} className={cn('h-[5px] flex-1 rounded-[var(--radius-pill)] transition-colors duration-200', segment < safeCurrent ? 'bg-accent-500' : 'bg-frame')} />)}</div>
      <span className="text-[11px] text-faint">{current} de {limit} itens em uso</span>
    </div>
  );
}

const progressWidths = ['w-0', 'w-[7%]', 'w-[14%]', 'w-[21%]', 'w-[29%]', 'w-[36%]', 'w-[43%]', 'w-1/2', 'w-[57%]', 'w-[64%]', 'w-[71%]', 'w-[79%]', 'w-[86%]', 'w-[93%]', 'w-full'];

export function DeadlineProgress({ elapsedDays, dueText }: { elapsedDays: number; dueText: string }) {
  const safeDays = Math.max(0, Math.min(Math.round(elapsedDays), 14));
  return (
    <div className="grid min-w-32 gap-1.5">
      <span className="text-xs text-dim">{dueText}</span>
      <span className="h-1 overflow-hidden rounded-[var(--radius-pill)] bg-frame" role="progressbar" aria-label={`${safeDays} de 14 dias decorridos`} aria-valuemin={0} aria-valuemax={14} aria-valuenow={safeDays}><span className={cn('block h-full rounded-[var(--radius-pill)] bg-accent-500 transition-[width] duration-200', progressWidths[safeDays])} /></span>
    </div>
  );
}
