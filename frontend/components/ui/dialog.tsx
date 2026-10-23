'use client';

import * as DialogPrimitive from '@radix-ui/react-dialog';
import { XIcon } from '@phosphor-icons/react';
import * as React from 'react';
import { cn } from '@/lib/utils';
import { Button } from './button';

export const Dialog = DialogPrimitive.Root;
export const DialogTrigger = DialogPrimitive.Trigger;
export const DialogClose = DialogPrimitive.Close;

export const DialogContent = React.forwardRef<React.ElementRef<typeof DialogPrimitive.Content>, React.ComponentPropsWithoutRef<typeof DialogPrimitive.Content>>(
  ({ className, children, onEscapeKeyDown, onPointerDownOutside, ...props }, ref) => (
    <DialogPrimitive.Portal>
      <DialogPrimitive.Overlay className="fixed inset-0 z-40 bg-[var(--backdrop)] data-[state=open]:animate-[backdrop-in_180ms_ease-out]" />
      <DialogPrimitive.Content
        ref={ref}
        onEscapeKeyDown={(event) => { event.preventDefault(); onEscapeKeyDown?.(event); }}
        onPointerDownOutside={(event) => { event.preventDefault(); onPointerDownOutside?.(event); }}
        className={cn('fixed left-1/2 top-1/2 z-50 max-h-[calc(100vh-24px)] w-[min(432px,calc(100vw-32px))] -translate-x-1/2 -translate-y-1/2 overflow-y-auto rounded-[var(--radius-frame)] bg-surface p-6 shadow-lg data-[state=open]:animate-[dialog-in_180ms_cubic-bezier(.2,.8,.2,1)]', className)}
        {...props}
      >
        {children}
        <DialogPrimitive.Close asChild>
          <Button variant="icon" size="icon" className="absolute right-4 top-4" aria-label="Fechar diálogo"><XIcon aria-hidden="true" className="size-4" /></Button>
        </DialogPrimitive.Close>
      </DialogPrimitive.Content>
    </DialogPrimitive.Portal>
  ),
);
DialogContent.displayName = 'DialogContent';

export const DialogTitle = React.forwardRef<React.ElementRef<typeof DialogPrimitive.Title>, React.ComponentPropsWithoutRef<typeof DialogPrimitive.Title>>(
  ({ className, ...props }, ref) => <DialogPrimitive.Title ref={ref} className={cn('pr-10 text-[19px] font-semibold text-foreground', className)} {...props} />,
);
DialogTitle.displayName = 'DialogTitle';

export const DialogDescription = React.forwardRef<React.ElementRef<typeof DialogPrimitive.Description>, React.ComponentPropsWithoutRef<typeof DialogPrimitive.Description>>(
  ({ className, ...props }, ref) => <DialogPrimitive.Description ref={ref} className={cn('mt-2 text-[13.5px] leading-5 text-muted', className)} {...props} />,
);
DialogDescription.displayName = 'DialogDescription';
