/* =====================================================
   TPVFácil — JavaScript Global
   ===================================================== */

/* ── Navbar: sombra al hacer scroll ──────────────── */
(function () {
  const nav = document.querySelector('.nav');
  if (!nav) return;
  window.addEventListener('scroll', () => {
    nav.classList.toggle('con-sombra', window.scrollY > 10);
  }, { passive: true });
})();

/* ── Menú hamburguesa (móvil) ─────────────────────── */
(function () {
  const btn   = document.getElementById('btn-hamburguesa');
  const movil = document.getElementById('nav-movil');
  if (!btn || !movil) return;
  btn.addEventListener('click', () => {
    movil.classList.toggle('abierto');
    btn.setAttribute('aria-expanded', movil.classList.contains('abierto'));
  });
})();

/* ── FAQ acordeón ─────────────────────────────────── */
(function () {
  document.querySelectorAll('.faq-pregunta').forEach(btn => {
    btn.addEventListener('click', () => {
      const item = btn.closest('.faq-item');
      const yaAbierto = item.classList.contains('abierto');
      // Cerrar todos
      document.querySelectorAll('.faq-item.abierto').forEach(i => i.classList.remove('abierto'));
      if (!yaAbierto) item.classList.add('abierto');
    });
  });
})();

/* ── Banner de cookies ────────────────────────────── */
(function () {
  const banner = document.getElementById('banner-cookies');
  const btnAceptar = document.getElementById('aceptar-cookies');
  if (!banner) return;

  if (localStorage.getItem('cookies-aceptadas')) {
    banner.classList.add('oculto');
    return;
  }

  banner.classList.remove('oculto');
  if (btnAceptar) {
    btnAceptar.addEventListener('click', () => {
      localStorage.setItem('cookies-aceptadas', '1');
      banner.classList.add('oculto');
    });
  }
})();

/* ── Evento analytics al descargar ───────────────── */
function trackDescarga(so) {
  if (typeof gtag === 'function') {
    gtag('event', 'download', {
      event_category: 'instalador',
      event_label: so || 'desconocido'
    });
  }
}
