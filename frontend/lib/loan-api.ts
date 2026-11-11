// Módulo de servidor: usa next/headers e o endereço interno do backend.
import { cookies } from 'next/headers';
import { redirect } from 'next/navigation';

export type SessionUser = {
  id: string;
  fullName: string;
  email: string;
  role: 'colaborador' | 'operacoes';
};

export type PageGate =
  | { kind: 'collaborator'; user: SessionUser; sessionCookie: string }
  | { kind: 'denied' }
  | { kind: 'unavailable' };

function backendUrl(): string | undefined {
  return process.env.INTERNAL_API_BASE_URL
    ?? (process.env.NODE_ENV === 'development' ? 'http://localhost:8080' : undefined);
}

/** Chama o backend no servidor, repassando só o cookie de sessão. */
export async function backendFetch(path: string, sessionCookie: string): Promise<Response | null> {
  const backend = backendUrl();
  if (!backend) return null;
  return fetch(new URL(path, backend), {
    headers: { cookie: `SESSION=${sessionCookie}` },
    cache: 'no-store',
    signal: AbortSignal.timeout(8000),
  }).catch(() => null);
}

/**
 * Spec 004 (CA-05): consulta `GET /api/auth/session` no servidor antes de
 * renderizar. Sem sessão ou com 401, inclusive de conta desativada ou
 * excluída, redireciona para `/`. Para `operacoes`, nega o acesso sem
 * chamar as rotas da jornada.
 */
export async function requireCollaboratorPage(): Promise<PageGate> {
  const sessionCookie = (await cookies()).get('SESSION')?.value;
  if (!sessionCookie) redirect('/');

  const response = await backendFetch('/api/auth/session', sessionCookie);
  if (response?.status === 401) redirect('/');
  if (!response?.ok) return { kind: 'unavailable' };

  const body = await response.json() as { user?: SessionUser };
  if (!body.user) redirect('/');
  if (body.user.role !== 'colaborador') return { kind: 'denied' };
  return { kind: 'collaborator', user: body.user, sessionCookie };
}
