'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useState, type ReactNode } from 'react';
import { Button } from './ui/button';

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
