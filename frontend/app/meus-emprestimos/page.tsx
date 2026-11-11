import type { Metadata } from 'next';
import { AccessDenied, LoanPageFrame, MyLoans, PageUnavailable } from '@/components/loan-journey';
import { loadJourneyData, requireCollaboratorPage, type Loan } from '@/lib/loan-api';

export const dynamic = 'force-dynamic';

export const metadata: Metadata = { title: 'EmpresTI | Meus empréstimos' };

export default async function MyLoansPage() {
  const gate = await requireCollaboratorPage();
  if (gate.kind === 'denied') return <AccessDenied />;
  if (gate.kind === 'unavailable') return <PageUnavailable />;

  const loans = await loadJourneyData<Loan[]>('/api/loans/me', gate.sessionCookie);
  if (!loans) return <PageUnavailable />;
  return (
    <LoanPageFrame title="Meus empréstimos" current="meus-emprestimos">
      <MyLoans loans={loans} />
    </LoanPageFrame>
  );
}
