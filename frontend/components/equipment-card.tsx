import { ArrowRightIcon, LaptopIcon, MonitorIcon, ProjectorScreenIcon, UsbIcon, VideoCameraIcon } from '@phosphor-icons/react/dist/ssr';
import type { Icon } from '@phosphor-icons/react';
import { StatusPill, type EquipmentStatus } from './ui/status';
import { cn } from '@/lib/utils';

const icons: Record<string, Icon> = {
  notebook: LaptopIcon,
  monitor: MonitorIcon,
  camera: VideoCameraIcon,
  accessory: UsbIcon,
  projector: ProjectorScreenIcon,
};

export type EquipmentCardProps = {
  name: string;
  assetCode: string;
  category: keyof typeof icons;
  status: EquipmentStatus;
  detail: string;
  onRequest?: (trigger: HTMLButtonElement) => void;
};

export function EquipmentCard({ name, assetCode, category, status, detail, onRequest }: EquipmentCardProps) {
  const EquipmentIcon = icons[category];
  const available = status === 'available';
  const content = (
    <>
      <div className="flex items-start justify-between gap-3">
        <span className="grid size-10 place-items-center rounded-[var(--radius-item)] bg-sunken text-accent-400"><EquipmentIcon aria-hidden="true" className="size-5" /></span>
        <StatusPill status={status} />
      </div>
      <div className="grid gap-1.5 text-left">
        <strong className="text-[14.5px] font-semibold text-foreground">{name}</strong>
        <span className="text-xs text-dim">{assetCode}</span>
      </div>
      <div className="mt-auto flex items-center justify-between gap-2 border-t border-row-border pt-3 text-xs">
        <span className={available ? 'font-semibold text-accent-300' : 'text-dim'}>{available ? 'Solicitar' : detail}</span>
        {available ? <ArrowRightIcon aria-hidden="true" className="size-4 text-accent-400" /> : null}
      </div>
    </>
  );

  const cardClass = cn('flex min-h-[190px] flex-col gap-3 rounded-[var(--radius-card)] border border-card-border bg-surface p-4', available && 'transition-colors duration-150 hover:border-accent-600');
  return available ? <button type="button" onClick={(event) => onRequest?.(event.currentTarget)} className={cardClass} aria-label={`Solicitar ${name}`}>{content}</button> : <article className={cardClass}>{content}</article>;
}
