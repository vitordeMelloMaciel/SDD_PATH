'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useRef, useState, type ReactNode } from 'react';
import type { Equipment } from '@/lib/loan-api';
import { EquipmentPhotoCard } from './equipment-card';
import { Button } from './ui/button';

const cardStatus = { disponivel: 'available', emprestado: 'borrowed', manutencao: 'maintenance' } as const;

type Page = 'catalogo' | 'meus-emprestimos';

async function csrfToken(): Promise<string> {
  const response = await fetch('/api/auth/csrf', { cache: 'no-store' });
  if (!response.ok) throw new Error('CSRF indisponível');
  return (await response.json() as { token: string }).token;
}

/** Quadro das páginas da jornada (Spec 004): marca, navegação e sair. */
export function LoanPageFrame({ title, current, children }: { title: string; current: Page; children?: ReactNode }) {
  const router = useRouter();
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  async function logout() {
    setBusy(true);
    setError('');
    try {
      const response = await fetch('/api/auth/logout', { method: 'POST', headers: { 'X-XSRF-TOKEN': await csrfToken() } });
      if (!response.ok) throw new Error('Logout indisponível');
      router.replace('/');
    } catch {
      setError('Não foi possível sair. Tente novamente.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="mx-auto min-h-dvh max-w-6xl px-5 py-8 sm:px-8">
      <header className="mb-8 flex flex-wrap items-center justify-between gap-5 border-b border-frame pb-5">
        <strong className="text-[15px]">EmpresTI</strong>
        <nav aria-label="Navegação" className="flex flex-wrap items-center gap-5 text-accent-300">
          <Link href="/catalogo" aria-current={current === 'catalogo' ? 'page' : undefined}>Catálogo</Link>
          <Link href="/meus-emprestimos" aria-current={current === 'meus-emprestimos' ? 'page' : undefined}>Meus empréstimos</Link>
          <Button variant="secondary" size="list" onClick={logout} disabled={busy}>Sair</Button>
        </nav>
      </header>
      {error && <p role="alert" className="mb-4 text-overdue">{error}</p>}
      <h1 className="mb-5 text-[24px] font-semibold">{title}</h1>
      {children}
    </main>
  );
}

/**
 * Catálogo (Spec 004, CA-01, CA-03 e CA-07). A tela só mostra o estado que a
 * API devolveu: depois do 201 ou de um 409, recarrega os dados do servidor e
 * não guarda estado local de empréstimo nem data.
 */
export function Catalog({ equipment }: { equipment: Equipment[] }) {
  const router = useRouter();
  const busy = useRef(false);
  const [pending, setPending] = useState<string | null>(null);
  const [notice, setNotice] = useState<{ error: boolean; text: string } | null>(null);

  async function request(item: Equipment) {
    if (busy.current) return;
    busy.current = true;
    setPending(item.id);
    setNotice(null);
    try {
      const response = await fetch('/api/loans', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': await csrfToken() },
        body: JSON.stringify({ equipmentId: item.id }),
        cache: 'no-store',
      });
      if (response.status === 401) {
        router.replace('/');
        return;
      }
      if (response.status === 201) {
        setNotice({ error: false, text: 'Empréstimo registrado. Consulte a data de devolução em Meus empréstimos.' });
      } else {
        const failure = await response.json().catch(() => null) as { message?: string } | null;
        setNotice({ error: true, text: failure?.message ?? 'Não foi possível solicitar o equipamento.' });
      }
      router.refresh();
    } catch {
      setNotice({ error: true, text: 'Não foi possível solicitar o equipamento. Tente novamente.' });
    } finally {
      busy.current = false;
      setPending(null);
    }
  }

  return (
    <>
      {notice && (
        <p role={notice.error ? 'alert' : 'status'} className={`mb-5 ${notice.error ? 'text-overdue' : 'text-available'}`}>{notice.text}</p>
      )}
      {equipment.length === 0 ? <p className="text-borrowed">Nenhum equipamento no acervo.</p> : (
        <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {equipment.map((item) => (
            <li key={item.id} className="grid">
              <EquipmentPhotoCard
                name={item.name}
                assetCode={item.assetCode}
                category={item.category}
                status={cardStatus[item.status]}
                imageUrl={item.imageUrl}
                requesting={pending === item.id}
                disabled={pending !== null}
                onRequest={item.status === 'disponivel' ? () => request(item) : undefined}
              />
            </li>
          ))}
        </ul>
      )}
      <p className="mt-6 text-xs text-borrowed"><a href="https://pixabay.com/" className="underline">Fotos: Pixabay</a></p>
    </>
  );
}

/** Spec 004 (CA-05): `operacoes` vê só este aviso, sem lista e sem chamar a API. */
export function AccessDenied() {
  return (
    <main className="mx-auto grid min-h-dvh max-w-xl place-content-center gap-3 px-5 py-8 text-center">
      <h1 className="text-[24px] font-semibold">Acesso negado</h1>
      <p className="text-borrowed">Esta área é exclusiva de colaboradores.</p>
    </main>
  );
}

export function PageUnavailable() {
  return (
    <main className="mx-auto grid min-h-dvh max-w-xl place-content-center gap-3 px-5 py-8 text-center">
      <p role="alert" className="text-overdue">Não foi possível carregar a página. Tente novamente.</p>
    </main>
  );
}
