'use client';

import {
  BellIcon,
  HandArrowDownIcon,
  ListChecksIcon,
  PackageIcon,
  PlusIcon,
  PlusSquareIcon,
  SquaresFourIcon,
  XIcon,
} from '@phosphor-icons/react';
import type { Icon } from '@phosphor-icons/react';
import * as React from 'react';
import { cn } from '@/lib/utils';
import { Button } from './ui/button';
import { LimitSegments } from './ui/status';

type NavigationItemProps = {
  icon: Icon;
  label: string;
  active?: boolean;
  badge?: string;
  onClick?: () => void;
};

export function NavigationItem({ icon: NavigationIcon, label, active = false, badge, onClick }: NavigationItemProps) {
  return (
    <button type="button" aria-current={active ? 'page' : undefined} onClick={onClick} className={cn('flex w-full items-center gap-2.5 rounded-[var(--radius-control)] px-2.5 py-[9px] text-left text-[13px] text-borrowed transition-[background-color,color] duration-100 hover:bg-sunken hover:text-foreground', active && 'bg-surface text-foreground shadow-[inset_2px_0_0_var(--accent-500)]')}>
      <NavigationIcon aria-hidden="true" className={cn('size-4 shrink-0', active && 'text-accent-400')} weight={active ? 'fill' : 'regular'} />
      <span>{label}</span>
      {badge ? <span className="ml-auto rounded-[var(--radius-pill)] bg-accent-800 px-2 py-0.5 text-[11px] font-semibold text-accent-200">{badge}</span> : null}
    </button>
  );
}

const navigation = [
  { label: 'Catálogo', icon: SquaresFourIcon },
  { label: 'Meus empréstimos', icon: HandArrowDownIcon },
  { label: 'Operações', icon: ListChecksIcon, badge: '12' },
  { label: 'Cadastro', icon: PlusSquareIcon },
];

export function AppShell({ children, onNewLoan }: { children: React.ReactNode; onNewLoan: (trigger: HTMLButtonElement) => void }) {
  const [activeItem, setActiveItem] = React.useState('Operações');
  const [menuOpen, setMenuOpen] = React.useState(false);
  const [isDesktop, setIsDesktop] = React.useState(false);

  React.useEffect(() => {
    const media = window.matchMedia('(min-width: 1024px)');
    const update = () => setIsDesktop(media.matches);
    update();
    media.addEventListener('change', update);
    return () => media.removeEventListener('change', update);
  }, []);

  return (
    <div className="min-h-screen bg-app lg:grid lg:grid-cols-[236px_minmax(0,1fr)]">
      {menuOpen ? <button type="button" className="fixed inset-0 z-30 bg-[var(--backdrop)] lg:hidden" onClick={() => setMenuOpen(false)} aria-label="Fechar navegação" /> : null}
      <aside inert={!isDesktop && !menuOpen} aria-hidden={!isDesktop && !menuOpen} className={cn('fixed inset-y-0 left-0 z-40 flex w-[236px] -translate-x-full flex-col border-r border-soft-border bg-sidebar px-3.5 py-[22px] transition-transform duration-150 lg:sticky lg:top-0 lg:h-screen lg:translate-x-0', menuOpen && 'translate-x-0')}>
        <div className="mb-8 flex items-center gap-2.5 px-2">
          <span className="grid size-9 place-items-center rounded-[var(--radius-compact)] border border-accent-700 bg-accent-900 text-accent-300"><PackageIcon aria-hidden="true" className="size-5" weight="fill" /></span>
          <div className="grid">
            <strong className="text-[15px] font-semibold tracking-[0.02em] text-foreground">EmpresTI</strong>
            <span className="text-[10px] uppercase tracking-[0.1em] text-ghost">Patrimônio interno</span>
          </div>
          <button type="button" onClick={() => setMenuOpen(false)} className="ml-auto grid size-8 place-items-center rounded-[var(--radius-control)] text-muted hover:bg-sunken lg:hidden" aria-label="Fechar menu"><XIcon aria-hidden="true" className="size-4" /></button>
        </div>

        <p className="mb-2 px-2.5 text-[10px] uppercase tracking-[0.1em] text-ghost">Navegação</p>
        <nav className="grid gap-1" aria-label="Navegação principal">
          {navigation.map((item) => <NavigationItem key={item.label} {...item} active={activeItem === item.label} onClick={() => { setActiveItem(item.label); setMenuOpen(false); }} />)}
        </nav>

        <div className="mt-auto grid gap-5">
          <div className="grid gap-2 rounded-[var(--radius-user)] border border-soft-border bg-sunken p-3">
            <span className="text-[11px] font-semibold text-accent-300">Limite de empréstimos</span>
            <LimitSegments current={2} />
          </div>
          <div className="h-px bg-[image:var(--divider-fade)]" />
          <div className="flex items-center gap-2.5 px-2">
            <span className="grid size-9 place-items-center rounded-[var(--radius-pill)] bg-accent-800 text-[11px] font-semibold text-accent-200">VM</span>
            <div className="min-w-0">
              <strong className="block truncate text-xs font-semibold text-foreground">Vitor Maciel</strong>
              <span className="text-[11px] text-faint">Operações</span>
            </div>
          </div>
        </div>
      </aside>

      <div className="min-w-0">
        <header className="flex h-16 items-center gap-3 border-b border-soft-border bg-app px-4 sm:px-7">
          <button type="button" onClick={() => setMenuOpen(true)} className="grid size-9 place-items-center rounded-[var(--radius-control)] border border-strong-border text-accent-300 lg:hidden" aria-label="Abrir menu"><PackageIcon aria-hidden="true" className="size-5" /></button>
          <div className="min-w-0">
            <div className="flex items-center gap-2 text-[11px] text-faint"><span>EmpresTI</span><span aria-hidden="true">/</span><span className="truncate text-dim">Operações</span></div>
            <h1 className="truncate text-[17px] font-semibold text-foreground">Gestão de equipamentos</h1>
          </div>
          <div className="ml-auto flex items-center gap-2">
            <Button variant="icon" size="icon" aria-label="Notificações"><BellIcon aria-hidden="true" className="size-4" /></Button>
            <Button variant="primary" size="header" aria-label="Novo empréstimo" onClick={(event) => onNewLoan(event.currentTarget)}><PlusIcon aria-hidden="true" className="size-4" /><span className="hidden sm:inline">Novo empréstimo</span></Button>
          </div>
        </header>
        <main className="grid gap-5 px-4 py-[26px] sm:px-7 sm:py-8">{children}</main>
      </div>
    </div>
  );
}
