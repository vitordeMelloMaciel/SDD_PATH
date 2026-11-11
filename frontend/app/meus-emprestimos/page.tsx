import type { Metadata } from 'next';
import { AccessDenied, LoanPageFrame, PageUnavailable } from '@/components/loan-journey';
import { requireCollaboratorPage } from '@/lib/loan-api';

export const dynamic = 'force-dynamic';

export const metadata: Metadata = { title: 'EmpresTI | Meus empréstimos' };

export default async function MyLoansPage() {
  const gate = await requireCollaboratorPage();
  if (gate.kind === 'denied') return <AccessDenied />;
  if (gate.kind === 'unavailable') return <PageUnavailable />;
  return <LoanPageFrame title="Meus empréstimos" current="meus-emprestimos" />;
}
