'use client';

import { FunnelIcon, MagnifyingGlassIcon, PackageIcon, PlusIcon } from '@phosphor-icons/react';
import * as React from 'react';
import { AppShell } from './app-shell';
import { EquipmentCard, type EquipmentCardProps } from './equipment-card';
import { Button } from './ui/button';
import { DataTable, DataTableBody, DataTableCell, DataTableHead, DataTableHeader, DataTableRow } from './ui/data-table';
import { Dialog, DialogClose, DialogContent, DialogDescription, DialogTitle } from './ui/dialog';
import { Input, Select, Textarea } from './ui/form-controls';
import { RuleNotice } from './ui/rule-notice';
import { DeadlineProgress, FilterChip, SegmentedControl, StatusPill, type EquipmentStatus } from './ui/status';

type Filter = 'all' | EquipmentStatus;

const equipment: EquipmentCardProps[] = [
  { name: 'Dell Latitude 5440', assetCode: 'NOTE-014 · Notebook', category: 'notebook', status: 'available', detail: 'Pronto para retirada' },
  { name: 'LG UltraFine 27', assetCode: 'MON-022 · Monitor', category: 'monitor', status: 'borrowed', detail: 'Com Marina · devolução 30 out.' },
  { name: 'Canon EOS R10', assetCode: 'CAM-006 · Audiovisual', category: 'camera', status: 'borrowed', detail: 'Com Lucas · devolução hoje' },
  { name: 'Dock USB-C Dell', assetCode: 'ACC-031 · Acessório', category: 'accessory', status: 'available', detail: 'Pronto para retirada' },
  { name: 'Projetor Epson X49', assetCode: 'PROJ-008 · Projetor', category: 'projector', status: 'maintenance', detail: 'Em diagnóstico técnico' },
  { name: 'Lenovo ThinkPad T14', assetCode: 'NOTE-011 · Notebook', category: 'notebook', status: 'borrowed', detail: 'Com Ana · atraso de 4 dias' },
];

const loans = [
  { item: 'Lenovo ThinkPad T14', code: 'NOTE-011', person: 'Ana Souza', status: 'borrowed' as const, days: 14, due: 'Atrasado há 4 dias', overdue: true },
  { item: 'Canon EOS R10', code: 'CAM-006', person: 'Lucas Rocha', status: 'borrowed' as const, days: 13, due: 'Devolução hoje', overdue: false },
  { item: 'LG UltraFine 27', code: 'MON-022', person: 'Marina Lopes', status: 'borrowed' as const, days: 8, due: 'Devolve em 6 dias', overdue: false },
];

const filterOptions = [
  { value: 'all' as const, label: 'Todos' },
  { value: 'available' as const, label: 'Disponíveis' },
  { value: 'borrowed' as const, label: 'Emprestados' },
  { value: 'maintenance' as const, label: 'Manutenção' },
];

function SectionHeading({ eyebrow, title, action }: { eyebrow: string; title: string; action?: React.ReactNode }) {
  return (
    <div className="flex items-end justify-between gap-4">
      <div className="grid gap-1">
        <span className="text-[10px] uppercase tracking-[0.1em] text-ghost">{eyebrow}</span>
        <h2 className="text-[17px] font-semibold text-foreground">{title}</h2>
      </div>
      {action}
    </div>
  );
}

export function OperationsDashboard() {
  const [filter, setFilter] = React.useState<Filter>('all');
  const [category, setCategory] = React.useState('Todos');
  const [query, setQuery] = React.useState('');
  const [dialogOpen, setDialogOpen] = React.useState(false);
  const [toast, setToast] = React.useState('');
  const returnFocusRef = React.useRef<HTMLButtonElement | null>(null);

  const openLoanDialog = (trigger: HTMLButtonElement) => {
    returnFocusRef.current = trigger;
    setDialogOpen(true);
  };

  const visibleEquipment = equipment.filter((item) => {
    const matchesStatus = filter === 'all' || item.status === filter;
    const matchesCategory = category === 'Todos' || item.assetCode.toLowerCase().includes(category.toLowerCase());
    const matchesQuery = `${item.name} ${item.assetCode}`.toLowerCase().includes(query.trim().toLowerCase());
    return matchesStatus && matchesCategory && matchesQuery;
  });

  const submitLoan = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setDialogOpen(false);
    setToast('Empréstimo registrado. O item já aparece na fila de operações.');
    window.setTimeout(() => setToast(''), 3200);
  };

  return (
    <AppShell onNewLoan={openLoanDialog}>
      <section className="grid gap-1" aria-labelledby="overview-title">
        <span className="text-[10px] uppercase tracking-[0.1em] text-ghost">Sexta-feira, 23 de outubro</span>
        <h2 id="overview-title" className="text-2xl font-semibold text-foreground">Visão operacional</h2>
        <p className="max-w-2xl text-[13.5px] leading-5 text-muted">Acompanhe disponibilidade, prazos e itens que precisam de ação da equipe.</p>
      </section>

      <RuleNotice title="Regras em vigor">
        Cada pessoa pode manter até 3 itens por 14 dias. Quem possui atraso fica impedido de solicitar um novo equipamento.
      </RuleNotice>

      <section className="grid grid-cols-2 gap-3 xl:grid-cols-4" aria-label="Resumo do acervo">
        {[
          ['126', 'Equipamentos', 'Acervo cadastrado'],
          ['84', 'Disponíveis', '67% do inventário'],
          ['34', 'Emprestados', '8 vencem nesta semana'],
          ['5', 'Em atraso', 'Exigem contato hoje'],
        ].map(([value, label, detail], index) => (
          <article key={label} className="grid min-h-[116px] content-between rounded-[var(--radius-stat)] border border-card-border bg-surface p-4">
            <div className="flex items-start justify-between gap-3">
              <span className="text-xs text-muted">{label}</span>
              <PackageIcon aria-hidden="true" className={index === 3 ? 'size-4 text-overdue' : 'size-4 text-accent-400'} />
            </div>
            <div><strong className="block text-[26px] font-semibold text-foreground">{value}</strong><span className={index === 3 ? 'text-xs text-overdue' : 'text-xs text-faint'}>{detail}</span></div>
          </article>
        ))}
      </section>

      <section className="grid gap-4" aria-labelledby="catalog-title">
        <SectionHeading eyebrow="Catálogo" title="Equipamentos" action={<Button variant="secondary" size="list"><PlusIcon aria-hidden="true" className="size-4" />Cadastrar item</Button>} />
        <div className="grid gap-3 border-y border-soft-border py-4 xl:grid-cols-[minmax(260px,1fr)_auto] xl:items-end">
          <div className="relative">
            <MagnifyingGlassIcon aria-hidden="true" className="pointer-events-none absolute bottom-[13px] left-3 size-4 text-faint" />
            <Input id="equipment-search" label="Buscar no acervo" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Nome, patrimônio ou categoria" className="pl-9" />
          </div>
          <div className="overflow-x-auto pb-0.5 [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"><SegmentedControl value={filter} options={filterOptions} onChange={setFilter} label="Filtrar equipamentos por situação" /></div>
        </div>
        <div className="flex flex-wrap items-center gap-2" aria-label="Filtrar por categoria">
          <FunnelIcon aria-hidden="true" className="mr-1 size-4 text-faint" />
          {['Todos', 'Notebook', 'Monitor', 'Audiovisual', 'Acessório'].map((item) => <FilterChip key={item} active={category === item} onClick={() => setCategory(item)}>{item}</FilterChip>)}
        </div>
        {visibleEquipment.length ? (
          <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4">
            {visibleEquipment.map((item) => <EquipmentCard key={item.assetCode} {...item} onRequest={openLoanDialog} />)}
          </div>
        ) : (
          <div className="grid min-h-40 place-items-center rounded-[var(--radius-card)] border border-soft-border bg-sunken p-6 text-center"><div><strong className="block text-[15px] font-semibold text-foreground">Nenhum item encontrado</strong><span className="mt-1 block text-[13px] text-muted">Ajuste a busca ou os filtros.</span></div></div>
        )}
      </section>

      <section className="grid gap-4" aria-labelledby="loans-title">
        <SectionHeading eyebrow="Operações" title="Empréstimos em aberto" action={<Button variant="row" size="table">Ver todos</Button>} />
        <DataTable label="Empréstimos em aberto">
          <DataTableHeader><tr><DataTableHead>Equipamento</DataTableHead><DataTableHead>Responsável</DataTableHead><DataTableHead>Situação</DataTableHead><DataTableHead>Prazo</DataTableHead><DataTableHead className="text-right">Ação</DataTableHead></tr></DataTableHeader>
          <DataTableBody>
            {loans.map((loan) => (
              <DataTableRow key={loan.code} overdue={loan.overdue}>
                <DataTableCell><strong className="block font-semibold text-foreground">{loan.item}</strong><span className="text-xs text-faint">{loan.code}</span></DataTableCell>
                <DataTableCell>{loan.person}</DataTableCell>
                <DataTableCell><StatusPill status={loan.status} />{loan.overdue ? <span className="ml-2 text-[11px] font-semibold text-overdue">Em atraso</span> : null}</DataTableCell>
                <DataTableCell><DeadlineProgress elapsedDays={loan.days} dueText={loan.due} /></DataTableCell>
                <DataTableCell className="text-right"><Button variant={loan.overdue ? 'row-overdue' : 'row'} size="table">Registrar devolução</Button></DataTableCell>
              </DataTableRow>
            ))}
          </DataTableBody>
        </DataTable>
      </section>

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent onCloseAutoFocus={(event) => { event.preventDefault(); returnFocusRef.current?.focus(); }}>
          <DialogTitle>Novo empréstimo</DialogTitle>
          <DialogDescription>Registre a retirada e combine a devolução com o colaborador.</DialogDescription>
          <form className="mt-5 grid gap-4" onSubmit={submitLoan}>
            <Input id="borrower" name="borrower" label="Colaborador" placeholder="Nome completo" required />
            <Select id="equipment" name="equipment" label="Equipamento" defaultValue="" required>
              <option value="" disabled>Selecione um item disponível</option>
              <option value="NOTE-014">Dell Latitude 5440 · NOTE-014</option>
              <option value="ACC-031">Dock USB-C Dell · ACC-031</option>
            </Select>
            <Input id="return-date" name="returnDate" label="Data de devolução" type="date" required />
            <Textarea id="notes" name="notes" label="Observação" placeholder="Condição do item na retirada" />
            <div className="rounded-[var(--radius-item)] border border-soft-border bg-sunken px-3.5 py-3 text-xs leading-5 text-muted"><strong className="block text-[13px] font-semibold text-foreground">Consequência do registro</strong>O equipamento passa para “Emprestado” e ocupa um dos três limites do colaborador.</div>
            <div className="flex justify-end gap-2.5 pt-1">
              <DialogClose asChild><Button variant="secondary" size="dialog">Cancelar</Button></DialogClose>
              <Button variant="confirm" size="dialog" type="submit">Confirmar empréstimo</Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>

      <div role="status" aria-live="polite" className={toast ? 'fixed bottom-5 right-5 z-[60] max-w-sm rounded-[var(--radius-control)] border border-accent-700 bg-surface px-4 py-3 text-[13px] text-accent-200 shadow-md' : 'sr-only'}>{toast}</div>
    </AppShell>
  );
}
