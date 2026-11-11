'use client';

import { PackageIcon } from '@phosphor-icons/react';
import { useRef, useState, type FormEvent } from 'react';
import { Button } from './ui/button';
import { Input } from './ui/form-controls';

const messages = {
  400: 'Informe e-mail e senha.',
  401: 'E-mail ou senha invalidos.',
  403: 'Atualize a pagina e tente novamente.',
} as const;

async function loadCsrfToken(): Promise<string> {
  const response = await fetch('/api/auth/csrf', { cache: 'no-store' });
  if (!response.ok) throw new Error('CSRF indisponível');
  const body: { token?: string } = await response.json();
  if (!body.token) throw new Error('CSRF ausente');
  return body.token;
}

export function LoginPage() {
  const pending = useRef(false);
  const csrf = useRef<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [invalidCredentials, setInvalidCredentials] = useState(false);
  const [error, setError] = useState('');
  const [userName, setUserName] = useState('');

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (pending.current) return;

    const form = event.currentTarget;
    const fields = new FormData(form);
    const email = String(fields.get('email') ?? '').trim();
    const password = String(fields.get('password') ?? '');
    setInvalidCredentials(false);
    setError('');
    if (!email || !password.trim()) {
      setError(messages[400]);
      return;
    }

    pending.current = true;
    setSubmitting(true);
    try {
      const token = csrf.current ?? await loadCsrfToken();
      csrf.current = null;
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': token },
        body: JSON.stringify({ email, password }),
        cache: 'no-store',
      });

      if (response.status === 400 || response.status === 401 || response.status === 403) {
        setError(messages[response.status]);
        setInvalidCredentials(response.status === 401);
        if (response.status === 403) {
          csrf.current = await loadCsrfToken().catch(() => null);
        }
        return;
      }
      if (!response.ok) throw new Error('Login indisponível');
      form.reset();

      const session = await fetch('/api/auth/session', { cache: 'no-store' });
      if (!session.ok) throw new Error('Sessão indisponível');
      const body: { user?: { fullName?: string } } = await session.json();
      if (!body.user?.fullName) throw new Error('Identidade ausente');
      setUserName(body.user.fullName);
    } catch {
      setError('Nao foi possivel entrar. Tente novamente.');
    } finally {
      pending.current = false;
      setSubmitting(false);
    }
  }

  return (
    <main className="grid min-h-dvh bg-app lg:grid-cols-[minmax(0,1.15fr)_minmax(0,1fr)]">
      <section className="flex flex-col justify-between gap-12 bg-[image:var(--gradient-login)] px-8 py-10 sm:px-14 lg:border-r lg:border-frame lg:px-20 lg:py-14">
        <div className="flex items-center gap-3 text-[15px] font-semibold tracking-[0.02em]">
          <span className="grid size-9 place-items-center rounded-[var(--radius-compact)] border border-accent-700 bg-accent-900 text-accent-300">
            <PackageIcon aria-hidden="true" className="size-5" weight="fill" />
          </span>
          EmpresTI
        </div>
        <div className="max-w-[410px] lg:mb-[20vh]">
          <div aria-hidden="true" className="mb-6 h-px w-32 bg-[image:var(--brand-rule)]" />
          <h1 className="max-w-[340px] text-[34px] leading-[1.15] font-semibold">Equipamentos certos, nas maos certas.</h1>
          <p className="mt-4 max-w-[350px] leading-6 text-borrowed">
            Consulte o acervo, acompanhe seus emprestimos e solicite itens disponiveis.
          </p>
        </div>
      </section>

      <section aria-labelledby="login-heading" className="grid place-items-center px-8 py-12">
        <div className="w-full max-w-[360px]">
          <h2 id="login-heading" className="text-[24px] font-semibold">{userName ? 'Sessão ativa' : 'Entrar'}</h2>
          {userName ? (
            <p role="status" className="mt-2 text-borrowed">Você entrou como {userName}.</p>
          ) : (
            <>
              <p className="mt-2 text-borrowed">Use seu e-mail institucional.</p>
              <form method="post" noValidate onSubmit={submit} className="mt-7 grid gap-4">
                <Input id="login-email" name="email" type="email" autoComplete="username" label="E-mail"
                  required aria-invalid={invalidCredentials || undefined}
                  aria-describedby={invalidCredentials ? 'login-error' : undefined} />
                <Input id="login-password" name="password" type="password" autoComplete="current-password" label="Senha"
                  required aria-invalid={invalidCredentials || undefined}
                  aria-describedby={invalidCredentials ? 'login-error' : undefined} />
                {error && (
                  <p id="login-error" role="alert" className="rounded-[var(--radius-control)] border border-[var(--overdue-border)] bg-[var(--overdue-bg)] px-3 py-2 text-overdue">
                    {error}
                  </p>
                )}
                <Button type="submit" disabled={submitting} className="mt-1 w-full">
                  {submitting ? 'Entrando...' : 'Entrar'}
                </Button>
              </form>
            </>
          )}
        </div>
      </section>
    </main>
  );
}
