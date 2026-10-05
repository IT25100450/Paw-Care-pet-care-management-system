document.addEventListener('DOMContentLoaded', () => {
  if (document.getElementById('dot-field')) return;

  const container = document.createElement('div');
  container.className = 'dot-field-container';
  container.id = 'dot-field';
  container.innerHTML = `
    <canvas class="dot-field-canvas" id="dot-canvas"></canvas>
    <svg class="dot-field-svg" id="dot-svg">
      <defs>
        <radialGradient id="dot-glow">
          <stop offset="0%" stop-color="rgba(34, 197, 94, 0.15)" />
          <stop offset="100%" stop-color="transparent" />
        </radialGradient>
      </defs>
      <circle id="glow-circle" cx="-9999" cy="-9999" r="160" fill="url(#dot-glow)" style="opacity: 0; will-change: opacity;" />
    </svg>
  `;
  document.body.appendChild(container);

  const canvas = document.getElementById('dot-canvas');
  const glowEl = document.getElementById('glow-circle');
  if (!canvas) return;

  const ctx = canvas.getContext('2d', { alpha: true });
  const dpr = Math.min(window.devicePixelRatio || 1, 2);
  
  // Configuration matched to PawCare Green Theme
  const p = {
    dotRadius: 1.5,
    dotSpacing: 14,
    cursorRadius: 500,
    cursorForce: 0.1,
    bulgeOnly: true,
    bulgeStrength: 67,
    sparkle: false,
    waveAmplitude: 0,
    gradientFrom: 'rgba(74, 222, 128, 0.45)', // green-300
    gradientTo: 'rgba(34, 197, 94, 0.25)'     // green-400
  };

  let dots = [];
  let size = { w: 0, h: 0 };
  const mouse = { x: -9999, y: -9999, prevX: -9999, prevY: -9999, speed: 0 };
  let glowOpacity = 0;
  let engagement = 0;
  let frameCount = 0;
  let rafId = null;

  function resize() {
    const rect = container.getBoundingClientRect();
    size.w = rect.width;
    size.h = rect.height;
    canvas.width = size.w * dpr;
    canvas.height = size.h * dpr;
    canvas.style.width = `${size.w}px`;
    canvas.style.height = `${size.h}px`;
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    buildDots();
  }

  function buildDots() {
    const step = p.dotRadius + p.dotSpacing;
    const cols = Math.floor(size.w / step);
    const rows = Math.floor(size.h / step);
    const padX = (size.w % step) / 2;
    const padY = (size.h % step) / 2;
    dots = new Array(rows * cols);
    let idx = 0;

    for (let row = 0; row < rows; row++) {
      for (let col = 0; col < cols; col++) {
        const ax = padX + col * step + step / 2;
        const ay = padY + row * step + step / 2;
        dots[idx++] = { ax, ay, sx: ax, sy: ay, vx: 0, vy: 0, x: ax, y: ay };
      }
    }
  }

  function onMouseMove(e) {
    mouse.x = e.clientX;
    mouse.y = e.clientY;
  }

  setInterval(() => {
    const dx = mouse.prevX - mouse.x;
    const dy = mouse.prevY - mouse.y;
    const dist = Math.sqrt(dx * dx + dy * dy);
    mouse.speed += (dist - mouse.speed) * 0.5;
    if (mouse.speed < 0.001) mouse.speed = 0;
    mouse.prevX = mouse.x;
    mouse.prevY = mouse.y;
  }, 20);

  function tick() {
    frameCount++;
    const len = dots.length;
    const t = frameCount * 0.02;

    const targetEngagement = Math.min(mouse.speed / 5, 1);
    engagement += (targetEngagement - engagement) * 0.06;
    if (engagement < 0.001) engagement = 0;

    glowOpacity += (engagement - glowOpacity) * 0.08;

    if (glowEl) {
      glowEl.setAttribute('cx', mouse.x);
      glowEl.setAttribute('cy', mouse.y);
      glowEl.style.opacity = glowOpacity;
    }

    ctx.clearRect(0, 0, size.w, size.h);

    const grad = ctx.createLinearGradient(0, 0, size.w, size.h);
    grad.addColorStop(0, p.gradientFrom);
    grad.addColorStop(1, p.gradientTo);
    ctx.fillStyle = grad;

    const crSq = p.cursorRadius * p.cursorRadius;
    const rad = p.dotRadius / 2;

    ctx.beginPath();

    for (let i = 0; i < len; i++) {
      const d = dots[i];
      const dx = mouse.x - d.ax;
      const dy = mouse.y - d.ay;
      const distSq = dx * dx + dy * dy;

      if (distSq < crSq && engagement > 0.01) {
        const dist = Math.sqrt(distSq);
        const t = 1 - dist / p.cursorRadius;
        const push = t * t * p.bulgeStrength * engagement;
        const angle = Math.atan2(dy, dx);
        d.sx += (d.ax - Math.cos(angle) * push - d.sx) * 0.15;
        d.sy += (d.ay - Math.sin(angle) * push - d.sy) * 0.15;
      } else {
        d.sx += (d.ax - d.sx) * 0.1;
        d.sy += (d.ay - d.sy) * 0.1;
      }

      let drawX = d.sx;
      let drawY = d.sy;

      ctx.moveTo(drawX + rad, drawY);
      ctx.arc(drawX, drawY, rad, 0, Math.PI * 2);
    }

    ctx.fill();
    rafId = requestAnimationFrame(tick);
  }

  window.addEventListener('resize', () => {
    clearTimeout(window.dotResizeTimer);
    window.dotResizeTimer = setTimeout(resize, 100);
  });
  
  window.addEventListener('mousemove', onMouseMove, { passive: true });
  
  resize();
  rafId = requestAnimationFrame(tick);
});
