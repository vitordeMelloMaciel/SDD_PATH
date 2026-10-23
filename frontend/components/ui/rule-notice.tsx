import { InfoIcon } from '@phosphor-icons/react/dist/ssr';

export function RuleNotice({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <aside className="flex gap-3 rounded-[var(--radius-user)] border border-accent-700 bg-[image:var(--gradient-notice)] px-4 py-[13px]">
      <InfoIcon aria-hidden="true" className="mt-px size-[17px] shrink-0 text-accent-400" />
      <div className="grid gap-1">
        <strong className="text-[13px] font-semibold text-foreground">{title}</strong>
        <div className="text-[12.5px] leading-5 text-borrowed">{children}</div>
      </div>
    </aside>
  );
}
