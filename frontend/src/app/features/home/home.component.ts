import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, MatCardModule, MatButtonModule, MatIconModule],
  template: `
    <div class="landing-container">
      <!-- ================= HEADER ================= -->
      <header class="site-header">
        <div class="header-inner">
          <a routerLink="/" class="brand">
            <mat-icon>hub</mat-icon>
            <span>Marca Blanca</span>
          </a>

          <nav class="main-nav">
            <a href="#inicio">Inicio</a>
            <a href="#modulos">Módulos</a>
            <a href="#ventajas">Ventajas</a>
          </nav>

          <a mat-flat-button color="primary" routerLink="/login" class="header-cta">
            Iniciar Sesión
          </a>
        </div>
      </header>

      <!-- ================= BODY ================= -->
      <main class="site-body">
      <!-- ================= HERO ================= -->
      <section id="inicio" class="hero">
        <div class="hero-bg">
          <div class="mesh-blob blob-1"></div>
          <div class="mesh-blob blob-2"></div>
          <div class="mesh-blob blob-3"></div>
          <div class="grid-overlay"></div>
        </div>

        <div class="hero-content">
          <div class="hero-badge">
            <mat-icon>bolt</mat-icon>
            <span>Plataforma multi-empresa lista para producción</span>
          </div>

          <h1>
            Gestiona tu negocio
            <span class="gradient-text">sin fricción</span>
          </h1>

          <p class="subtitle">
            Usuarios, empresas, tareas y atención al cliente en una sola plataforma
            construida para escalar con arquitectura moderna y segura.
          </p>

          <div class="cta-buttons">
            <a mat-flat-button color="primary" routerLink="/login" class="btn-primary">
              <mat-icon>login</mat-icon>
              Iniciar Sesión
            </a>
            <a mat-stroked-button class="btn-secondary" href="#modulos">
              Ver módulos
              <mat-icon>arrow_downward</mat-icon>
            </a>
          </div>

          <div class="hero-stats">
            <div class="stat">
              <span class="stat-number">2</span>
              <span class="stat-label">Módulos integrados</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat">
              <span class="stat-number">100%</span>
              <span class="stat-label">Multi-tenant</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat">
              <span class="stat-number">JWT</span>
              <span class="stat-label">Autenticación segura</span>
            </div>
          </div>
        </div>
      </section>

      <!-- ================= MÓDULOS ================= -->
      <section id="modulos" class="modules-section">
        <div class="section-heading">
          <span class="eyebrow">Módulos</span>
          <h2>Todo lo que tu empresa necesita</h2>
          <p class="section-subtitle">Soluciones integrales para cada área de tu operación</p>
        </div>

        <div class="modules-grid modules-grid-2">
          <mat-card class="module-card module-card-clickable" style="--accent: #7c3aed" routerLink="/modulos/omnicanal">
            <div class="module-icon">
              <mat-icon>support_agent</mat-icon>
            </div>
            <h3>Omnicanal</h3>
            <p>Gestiona la comunicación con tus clientes desde todos los canales, en un solo lugar.</p>
            <ul>
              <li><mat-icon>check_circle</mat-icon>Atención unificada</li>
              <li><mat-icon>check_circle</mat-icon>Múltiples canales</li>
              <li><mat-icon>check_circle</mat-icon>Historial centralizado</li>
            </ul>
            <span class="module-link">
              Ver más
              <mat-icon>arrow_forward</mat-icon>
            </span>
          </mat-card>

          <mat-card class="module-card module-card-clickable" style="--accent: #2563eb" routerLink="/modulos/pbx-3cx">
            <div class="module-icon">
              <mat-icon>call</mat-icon>
            </div>
            <h3>PBX 3CX</h3>
            <p>Integración telefónica para gestionar llamadas de tu empresa directamente desde la plataforma.</p>
            <ul>
              <li><mat-icon>check_circle</mat-icon>Extensiones y llamadas</li>
              <li><mat-icon>check_circle</mat-icon>Integración con 3CX</li>
              <li><mat-icon>check_circle</mat-icon>Registro de llamadas</li>
            </ul>
            <span class="module-link">
              Ver más
              <mat-icon>arrow_forward</mat-icon>
            </span>
          </mat-card>
        </div>
      </section>

      <!-- ================= FEATURES ================= -->
      <section id="ventajas" class="features-section">
        <div class="features-bg-shape"></div>
        <div class="section-heading light">
          <span class="eyebrow">Ventajas</span>
          <h2>¿Por qué elegirnos?</h2>
        </div>

        <div class="features-grid">
          <div class="feature">
            <div class="feature-icon">
              <mat-icon>security</mat-icon>
            </div>
            <h3>Seguro</h3>
            <p>Autenticación robusta con JWT y refresh tokens</p>
          </div>
          <div class="feature">
            <div class="feature-icon">
              <mat-icon>speed</mat-icon>
            </div>
            <h3>Rápido</h3>
            <p>Arquitectura moderna con Angular y Spring Boot</p>
          </div>
          <div class="feature">
            <div class="feature-icon">
              <mat-icon>sync</mat-icon>
            </div>
            <h3>Escalable</h3>
            <p>Arquitectura hexagonal preparada para crecer</p>
          </div>
          <div class="feature">
            <div class="feature-icon">
              <mat-icon>cloud</mat-icon>
            </div>
            <h3>Cloud Ready</h3>
            <p>Dockerizado y listo para desplegar en la nube</p>
          </div>
        </div>
      </section>

      <!-- ================= CTA FINAL ================= -->
      <section class="cta-section">
        <div class="cta-glow"></div>
        <h2>¿Listo para comenzar?</h2>
        <p>Accede a la plataforma y descubre todo lo que podemos hacer por tu empresa</p>
        <a mat-flat-button color="primary" routerLink="/login" class="btn-large">
          Iniciar Sesión Ahora
          <mat-icon>arrow_forward</mat-icon>
        </a>
      </section>

      </main>

      <!-- ================= FOOTER ================= -->
      <footer class="footer">
        <div class="footer-inner">
          <div class="footer-brand">
            <mat-icon>hub</mat-icon>
            <span>Marca Blanca</span>
          </div>
          <p>&copy; 2026 Plataforma Marca Blanca. Todos los derechos reservados.</p>
        </div>
      </footer>
    </div>
  `,
  styles: [`
    :host {
      --brand-dark: #0f2744;
      --brand-mid: #1e3a5f;
      --brand-light: #2563eb;
      --brand-accent: #38bdf8;
      display: block;
    }

    .landing-container {
      min-height: 100vh;
      overflow-x: hidden;
    }

    /* ================= HEADER ================= */
    .site-header {
      position: sticky;
      top: 0;
      z-index: 50;
      background: rgba(8, 22, 39, 0.75);
      backdrop-filter: blur(10px);
      border-bottom: 1px solid rgba(255, 255, 255, 0.08);
    }

    .header-inner {
      max-width: 1200px;
      margin: 0 auto;
      padding: 14px 24px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 24px;
    }

    .brand {
      display: flex;
      align-items: center;
      gap: 8px;
      color: white;
      text-decoration: none;
      font-weight: 700;
      font-size: 1.05rem;
    }

    .brand mat-icon {
      color: var(--brand-accent);
    }

    .main-nav {
      display: flex;
      gap: 28px;
      flex: 1;
      justify-content: center;
    }

    .main-nav a {
      color: rgba(255, 255, 255, 0.75);
      text-decoration: none;
      font-size: 0.95rem;
      font-weight: 500;
      transition: color 0.2s ease;
    }

    .main-nav a:hover {
      color: white;
    }

    .header-cta {
      height: 38px;
      line-height: 38px;
      padding: 0 20px !important;
      font-size: 0.9rem;
    }

    @media (max-width: 768px) {
      .main-nav {
        display: none;
      }
    }


    /* ================= HERO ================= */
    .hero {
      position: relative;
      overflow: hidden;
      padding: 140px 24px 110px;
      text-align: center;
      background: radial-gradient(120% 120% at 50% 0%, var(--brand-mid) 0%, var(--brand-dark) 60%, #081627 100%);
      color: white;
    }

    .hero-bg {
      position: absolute;
      inset: 0;
      z-index: 0;
    }

    .grid-overlay {
      position: absolute;
      inset: 0;
      background-image:
        linear-gradient(rgba(255,255,255,0.05) 1px, transparent 1px),
        linear-gradient(90deg, rgba(255,255,255,0.05) 1px, transparent 1px);
      background-size: 48px 48px;
      mask-image: radial-gradient(ellipse at center, black 0%, transparent 75%);
    }

    .mesh-blob {
      position: absolute;
      border-radius: 50%;
      filter: blur(60px);
      opacity: 0.55;
    }

    .blob-1 {
      width: 480px;
      height: 480px;
      top: -160px;
      left: -120px;
      background: radial-gradient(circle, var(--brand-accent) 0%, transparent 70%);
      animation: float-slow 12s ease-in-out infinite;
    }

    .blob-2 {
      width: 420px;
      height: 420px;
      top: 40px;
      right: -140px;
      background: radial-gradient(circle, #6366f1 0%, transparent 70%);
      animation: float-slow 14s ease-in-out infinite reverse;
    }

    .blob-3 {
      width: 320px;
      height: 320px;
      bottom: -180px;
      left: 40%;
      background: radial-gradient(circle, #2563eb 0%, transparent 70%);
      animation: float-slow 16s ease-in-out infinite;
    }

    @keyframes float-slow {
      0%, 100% { transform: translate(0, 0) scale(1); }
      50% { transform: translate(30px, -20px) scale(1.08); }
    }

    .hero-content {
      position: relative;
      z-index: 1;
      max-width: 760px;
      margin: 0 auto;
    }

    .hero-badge {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      padding: 8px 18px;
      border-radius: 999px;
      background: rgba(255, 255, 255, 0.08);
      border: 1px solid rgba(255, 255, 255, 0.18);
      backdrop-filter: blur(6px);
      font-size: 0.85rem;
      font-weight: 500;
      margin-bottom: 32px;
    }

    .hero-badge mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
      color: var(--brand-accent);
    }

    .hero-content h1 {
      font-size: 3.4rem;
      font-weight: 800;
      line-height: 1.15;
      margin: 0 0 20px;
      letter-spacing: -0.02em;
    }

    .gradient-text {
      display: block;
      background: linear-gradient(90deg, var(--brand-accent), #a78bfa 60%, #f472b6);
      -webkit-background-clip: text;
      background-clip: text;
      color: transparent;
    }

    .subtitle {
      font-size: 1.2rem;
      line-height: 1.65;
      margin: 0 auto 40px;
      max-width: 560px;
      opacity: 0.85;
    }

    .cta-buttons {
      display: flex;
      gap: 16px;
      justify-content: center;
      flex-wrap: wrap;
      margin-bottom: 56px;
    }

    .btn-primary {
      font-size: 16px;
      padding: 0 32px;
      height: 50px;
      line-height: 50px;
      display: inline-flex;
      align-items: center;
      gap: 8px;
      box-shadow: 0 8px 24px rgba(37, 99, 235, 0.45);
    }

    .btn-secondary {
      font-size: 16px;
      padding: 0 32px;
      height: 50px;
      line-height: 48px;
      display: inline-flex;
      align-items: center;
      gap: 8px;
      color: white;
      border-color: rgba(255, 255, 255, 0.35);
    }

    .hero-stats {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 32px;
      flex-wrap: wrap;
    }

    .stat {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .stat-number {
      font-size: 1.6rem;
      font-weight: 800;
      color: var(--brand-accent);
    }

    .stat-label {
      font-size: 0.85rem;
      opacity: 0.75;
    }

    .stat-divider {
      width: 1px;
      height: 32px;
      background: rgba(255, 255, 255, 0.2);
    }

    /* ================= SECTION HEADINGS ================= */
    .section-heading {
      text-align: center;
      max-width: 620px;
      margin: 0 auto 56px;
    }

    .eyebrow {
      display: inline-block;
      font-size: 0.8rem;
      font-weight: 700;
      letter-spacing: 0.08em;
      text-transform: uppercase;
      color: var(--brand-light);
      background: #eef2ff;
      padding: 6px 14px;
      border-radius: 999px;
      margin-bottom: 16px;
    }

    .section-heading.light .eyebrow {
      color: var(--brand-accent);
      background: rgba(255, 255, 255, 0.1);
    }

    .section-heading h2 {
      font-size: 2.4rem;
      font-weight: 800;
      margin: 0 0 12px;
      color: #0f172a;
      letter-spacing: -0.01em;
    }

    .section-heading.light h2 {
      color: white;
    }

    .section-subtitle {
      font-size: 1.1rem;
      color: #64748b;
      margin: 0;
    }

    /* ================= MODULES ================= */
    .modules-section {
      padding: 100px 24px;
      max-width: 1200px;
      margin: 0 auto;
    }

    .modules-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
      gap: 24px;
    }

    .modules-grid-2 {
      max-width: 800px;
      margin: 0 auto;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    }

    .module-card {
      position: relative;
      padding: 32px 28px !important;
      border-radius: 20px !important;
      border: 1px solid #e2e8f0;
      box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
      transition: transform 0.3s ease, box-shadow 0.3s ease, border-color 0.3s ease;
      overflow: hidden;
    }

    .module-card::before {
      content: '';
      position: absolute;
      inset: 0 0 auto 0;
      height: 4px;
      background: var(--accent);
      transform: scaleX(0);
      transform-origin: left;
      transition: transform 0.3s ease;
    }

    .module-card:hover {
      transform: translateY(-6px);
      box-shadow: 0 20px 40px rgba(15, 23, 42, 0.12);
      border-color: transparent;
    }

    .module-card:hover::before {
      transform: scaleX(1);
    }

    .module-icon {
      width: 56px;
      height: 56px;
      border-radius: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: color-mix(in srgb, var(--accent) 12%, white);
      margin-bottom: 20px;
    }

    .module-icon mat-icon {
      font-size: 28px;
      width: 28px;
      height: 28px;
      color: var(--accent);
    }

    .module-card h3 {
      font-size: 1.25rem;
      font-weight: 700;
      margin: 0 0 10px;
      color: #0f172a;
    }

    .module-card > p {
      color: #64748b;
      margin: 0 0 18px;
      line-height: 1.55;
    }

    .module-card ul {
      list-style: none;
      margin: 0;
      padding: 0;
      display: flex;
      flex-direction: column;
      gap: 10px;
    }

    .module-card li {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 0.9rem;
      color: #334155;
    }

    .module-card li mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
      color: var(--accent);
      flex-shrink: 0;
    }

    .module-card-clickable {
      cursor: pointer;
    }

    .module-link {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      margin-top: 20px;
      font-size: 0.9rem;
      font-weight: 700;
      color: var(--accent);
    }

    .module-link mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
      transition: transform 0.2s ease;
    }

    .module-card-clickable:hover .module-link mat-icon {
      transform: translateX(4px);
    }

    /* ================= FEATURES ================= */
    .features-section {
      position: relative;
      overflow: hidden;
      padding: 100px 24px;
      background: linear-gradient(180deg, var(--brand-dark) 0%, #050c17 100%);
    }

    .features-bg-shape {
      position: absolute;
      top: -50%;
      left: 50%;
      transform: translateX(-50%);
      width: 900px;
      height: 900px;
      border-radius: 50%;
      background: radial-gradient(circle, rgba(37, 99, 235, 0.18) 0%, transparent 70%);
    }

    .features-grid {
      position: relative;
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 32px;
      max-width: 1100px;
      margin: 0 auto;
    }

    .feature {
      text-align: center;
      color: white;
    }

    .feature-icon {
      width: 72px;
      height: 72px;
      margin: 0 auto 20px;
      border-radius: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(255, 255, 255, 0.06);
      border: 1px solid rgba(255, 255, 255, 0.12);
    }

    .feature-icon mat-icon {
      font-size: 32px;
      width: 32px;
      height: 32px;
      color: var(--brand-accent);
    }

    .feature h3 {
      font-size: 1.3rem;
      font-weight: 700;
      margin: 0 0 8px;
    }

    .feature p {
      color: rgba(255, 255, 255, 0.65);
      margin: 0;
      font-size: 0.95rem;
    }

    /* ================= CTA ================= */
    .cta-section {
      position: relative;
      overflow: hidden;
      padding: 100px 24px;
      text-align: center;
      background: linear-gradient(135deg, var(--brand-mid) 0%, var(--brand-light) 100%);
      color: white;
    }

    .cta-glow {
      position: absolute;
      top: -40%;
      left: 50%;
      transform: translateX(-50%);
      width: 700px;
      height: 700px;
      border-radius: 50%;
      background: radial-gradient(circle, rgba(255,255,255,0.12) 0%, transparent 70%);
    }

    .cta-section h2 {
      position: relative;
      font-size: 2.4rem;
      font-weight: 800;
      margin: 0 0 16px;
    }

    .cta-section p {
      position: relative;
      font-size: 1.1rem;
      margin: 0 0 36px;
      opacity: 0.9;
    }

    .btn-large {
      position: relative;
      font-size: 17px;
      padding: 0 40px;
      height: 54px;
      line-height: 54px;
      display: inline-flex;
      align-items: center;
      gap: 8px;
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.25);
    }

    /* ================= FOOTER ================= */
    .footer {
      background: #050c17;
      color: white;
      padding: 48px 24px 32px;
      text-align: center;
      border-top: 1px solid rgba(255, 255, 255, 0.06);
    }

    .footer-inner {
      max-width: 1200px;
      margin: 0 auto;
    }

    .footer-brand {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      font-weight: 700;
      margin-bottom: 16px;
    }

    .footer-brand mat-icon {
      color: var(--brand-accent);
    }

    .footer p {
      margin: 0 0 16px;
      opacity: 0.6;
      font-size: 0.85rem;
    }



    /* ================= RESPONSIVE ================= */
    @media (max-width: 768px) {
      .hero {
        padding: 110px 20px 80px;
      }

      .hero-content h1 {
        font-size: 2.2rem;
      }

      .subtitle {
        font-size: 1.05rem;
      }

      .section-heading h2 {
        font-size: 1.9rem;
      }

      .hero-stats {
        gap: 20px;
      }
    }
  `],
})
export class HomeComponent {}
