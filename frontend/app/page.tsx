import type { Metadata } from 'next';
import { LoginPage } from '@/components/login-page';

export const dynamic = 'force-dynamic';

export const metadata: Metadata = { title: 'EmpresTI | Entrar' };

export default function Home() {
  return <LoginPage />;
}
