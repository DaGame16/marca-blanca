import { AfterViewInit, Directive, ElementRef, Input, OnDestroy } from '@angular/core';
import gsap from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';

gsap.registerPlugin(ScrollTrigger);

/**
 * Anima la aparición de un elemento cuando entra al viewport al hacer
 * scroll (fade + slide-up con GSAP ScrollTrigger). Uso:
 *
 *   <div appScrollReveal>...</div>
 *   <div appScrollReveal [appScrollRevealDelay]="0.08">...</div>  <!-- para escalonar varias tarjetas -->
 *
 * Respeta prefers-reduced-motion: si el usuario lo tiene activado, el
 * elemento aparece directo sin animación (mismo criterio que el resto del
 * proyecto, ver liwa-shared/omnicanal-liwa-panel.component.ts).
 */
@Directive({
  selector: '[appScrollReveal]',
  standalone: true,
})
export class ScrollRevealDirective implements AfterViewInit, OnDestroy {
  @Input() appScrollRevealDelay = 0;

  private tween?: gsap.core.Tween;

  constructor(private readonly el: ElementRef<HTMLElement>) {}

  ngAfterViewInit(): void {
    const nodo = this.el.nativeElement;
    const prefiereMenosMovimiento = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (prefiereMenosMovimiento) {
      return;
    }

    this.tween = gsap.fromTo(
      nodo,
      { opacity: 0, y: 28 },
      {
        opacity: 1,
        y: 0,
        duration: 0.55,
        delay: this.appScrollRevealDelay,
        ease: 'power2.out',
        scrollTrigger: {
          trigger: nodo,
          start: 'top 88%',
          toggleActions: 'play none none reverse',
        },
      },
    );
  }

  ngOnDestroy(): void {
    this.tween?.scrollTrigger?.kill();
    this.tween?.kill();
  }
}
