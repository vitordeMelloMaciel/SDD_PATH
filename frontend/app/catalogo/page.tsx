import type { Metadata } from 'next';
import { AccessDenied, Catalog, LoanPageFrame, PageUnavailable } from '@/components/loan-journey';
import { loadJourneyData, requireCollaboratorPage, type Equipment } from '@/lib/loan-api';

export const dynamic = 'force-dynamic';

export const metadata: Metadata = { title: 'EmpresTI | Catálogo' };

export default async function CatalogPage() {
  const gate = await requireCollaboratorPage();
  if (gate.kind === 'denied') return <AccessDenied />;
  if (gate.kind === 'unavailable') return <PageUnavailable />;

  const equipment = await loadJourneyData<Equipment[]>('/api/equipment', gate.sessionCookie);
  if (!equipment) return <PageUnavailable />;
  return (
    <LoanPageFrame title="Catálogo" current="catalogo">
      <Catalog equipment={equipment} />
    </LoanPageFrame>
  );
}
