import { OperationsDashboard, type SystemHealth } from '@/components/operations-dashboard';

export const dynamic = 'force-dynamic';

async function loadSystemHealth(): Promise<SystemHealth> {
  const baseUrl = process.env.INTERNAL_API_BASE_URL ?? process.env.NEXT_PUBLIC_API_BASE_URL ?? 'http://localhost:8080';

  try {
    const response = await fetch(`${baseUrl}/health`, {
      cache: 'no-store',
      signal: AbortSignal.timeout(3000),
    });
    const body = (await response.json()) as { status?: string; database?: string };
    const healthy = response.ok && body.status === 'ok' && body.database === 'up';

    return healthy
      ? { healthy: true, label: 'API e banco conectados' }
      : { healthy: false, label: 'Integração indisponível' };
  } catch {
    return { healthy: false, label: 'Integração indisponível' };
  }
}

export default async function Home() {
  return <OperationsDashboard systemHealth={await loadSystemHealth()} />;
}
