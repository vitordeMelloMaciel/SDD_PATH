import * as React from 'react';
import { cn } from '@/lib/utils';

export function DataTable({ children, label }: { children: React.ReactNode; label: string }) {
  return <div className="overflow-x-auto rounded-[var(--radius-card)] border border-soft-border"><table className="w-full min-w-[760px] border-collapse text-left text-[13px]" aria-label={label}>{children}</table></div>;
}

export function DataTableHeader({ children }: { children: React.ReactNode }) {
  return <thead className="bg-sunken text-[11px] uppercase tracking-[0.1em] text-faint">{children}</thead>;
}

export function DataTableBody({ children }: { children: React.ReactNode }) {
  return <tbody>{children}</tbody>;
}

export function DataTableRow({ children, overdue = false }: { children: React.ReactNode; overdue?: boolean }) {
  return <tr className={cn('border-t border-row-border first:border-t-0', overdue && 'bg-[var(--overdue-bg)]')}>{children}</tr>;
}

export function DataTableHead({ children, className }: { children: React.ReactNode; className?: string }) {
  return <th scope="col" className={cn('px-4 py-3 font-normal', className)}>{children}</th>;
}

export function DataTableCell({ children, className }: { children: React.ReactNode; className?: string }) {
  return <td className={cn('px-4 py-3.5 align-middle text-dim', className)}>{children}</td>;
}
