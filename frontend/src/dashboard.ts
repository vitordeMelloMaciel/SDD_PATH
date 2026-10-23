type EquipmentStatus = 'disponivel' | 'emprestado' | 'manutencao';

type Equipment = {
  name: string;
  code: string;
  category: string;
  status: EquipmentStatus;
  detail: string;
  image: string;
};

const equipment: Equipment[] = [
  {
    name: 'MacBook Air M2',
    code: 'NOTE-014',
    category: 'Notebook',
    status: 'emprestado',
    detail: 'Com Marina Lopes ate 30 set.',
    image: 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=240&q=80',
  },
  {
    name: 'Dell UltraSharp 27',
    code: 'MON-022',
    category: 'Monitor',
    status: 'disponivel',
    detail: 'Pronto para retirada',
    image: 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=240&q=80',
  },
  {
    name: 'Canon EOS R10',
    code: 'CAM-006',
    category: 'Audiovisual',
    status: 'emprestado',
    detail: 'Com Lucas Rocha ate hoje',
    image: 'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?auto=format&fit=crop&w=240&q=80',
  },
  {
    name: 'Logitech MX Keys',
    code: 'PER-031',
    category: 'Periferico',
    status: 'disponivel',
    detail: 'Armario B, prateleira 3',
    image: 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=240&q=80',
  },
  {
    name: 'Kit HDMI e USB-C',
    code: 'CAB-019',
    category: 'Acessorio',
    status: 'manutencao',
    detail: 'Inspecao de conectores',
    image: 'https://images.unsplash.com/photo-1601737487795-dab272f52420?auto=format&fit=crop&w=240&q=80',
  },
];

const statusLabels: Record<EquipmentStatus, string> = {
  disponivel: 'Disponivel',
  emprestado: 'Emprestado',
  manutencao: 'Manutencao',
};

const icon = (name: string) => {
  const paths: Record<string, string> = {
    grid: '<rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/>',
    box: '<path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16Z"/><path d="m3.3 7 8.7 5 8.7-5M12 22V12"/>',
    swap: '<path d="m16 3 4 4-4 4M20 7H4M8 21l-4-4 4-4M4 17h16"/>',
    users: '<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/>',
    tool: '<path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94Z"/>',
    search: '<circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/>',
    bell: '<path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M13.73 21a2 2 0 0 1-3.46 0"/>',
    plus: '<path d="M12 5v14M5 12h14"/>',
    arrow: '<path d="M5 12h14M13 6l6 6-6 6"/>',
    close: '<path d="M18 6 6 18M6 6l12 12"/>',
  };

  return `<svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">${paths[name]}</svg>`;
};

export function renderDashboard() {
  const app = document.querySelector<HTMLDivElement>('#app');
  if (!app) return;

  app.innerHTML = `
    <div class="app-shell">
      <aside class="sidebar" id="sidebar">
        <a class="brand" href="#" aria-label="Equipar - inicio">
          <span class="brand-mark">E</span>
          <span>equipar</span>
        </a>

        <nav class="main-nav" aria-label="Navegacao principal">
          <button class="nav-item active" type="button">${icon('grid')}<span>Visao geral</span></button>
          <button class="nav-item" type="button">${icon('box')}<span>Equipamentos</span></button>
          <button class="nav-item" type="button">${icon('swap')}<span>Emprestimos</span><span class="nav-count">34</span></button>
          <button class="nav-item" type="button">${icon('users')}<span>Pessoas</span></button>
          <button class="nav-item" type="button">${icon('tool')}<span>Manutencao</span></button>
        </nav>

        <div class="sidebar-foot">
          <div class="api-state" id="api-state">
            <span class="state-dot"></span>
            <div><strong>Verificando sistema</strong><small>Conectando com a API</small></div>
          </div>
          <div class="profile">
            <span class="avatar">VM</span>
            <div><strong>Vitor Maciel</strong><small>Operacoes</small></div>
            <button class="more-button" type="button" aria-label="Opcoes do perfil">...</button>
          </div>
        </div>
      </aside>
      <button class="sidebar-scrim" id="sidebar-scrim" type="button" aria-label="Fechar menu"></button>

      <main class="workspace">
        <header class="topbar">
          <button class="mobile-brand" id="mobile-menu-button" type="button" aria-label="Abrir menu"><span class="brand-mark">E</span></button>
          <div class="greeting"><p>SEGUNDA, 28 DE SETEMBRO</p><h1>Bom dia, Vitor</h1></div>
          <div class="top-actions">
            <div class="notification-wrap">
              <button class="icon-button" id="notification-button" type="button" aria-label="Notificacoes">${icon('bell')}<span class="notification-dot"></span></button>
              <div class="notification-popover" id="notification-popover" hidden>
                <strong>Notificacoes</strong>
                <p>3 emprestimos vencem hoje.</p>
                <p>2 itens aguardam inspecao.</p>
              </div>
            </div>
            <button class="primary-button" data-open-loan type="button" aria-label="Novo emprestimo">${icon('plus')}<span>Novo emprestimo</span></button>
          </div>
        </header>

        <div class="content">
          <section class="summary-grid" aria-label="Resumo do inventario">
            <article class="metric"><div><span class="metric-label">Equipamentos</span><strong>126</strong></div><span class="metric-note neutral">Total cadastrado</span></article>
            <article class="metric"><div><span class="metric-label">Disponiveis</span><strong>84</strong></div><span class="metric-note positive">67% do acervo</span></article>
            <article class="metric"><div><span class="metric-label">Emprestados</span><strong>34</strong></div><span class="metric-note neutral">8 vencem esta semana</span></article>
            <article class="metric attention"><div><span class="metric-label">Precisam de atencao</span><strong>8</strong></div><span class="metric-note warning">5 atrasados</span></article>
          </section>

          <div class="dashboard-grid">
            <section class="inventory-panel">
              <div class="section-heading">
                <div><p class="eyebrow">INVENTARIO</p><h2>Equipamentos recentes</h2></div>
                <button class="text-button" type="button">Ver inventario ${icon('arrow')}</button>
              </div>
              <div class="inventory-tools">
                <label class="search-field">${icon('search')}<input id="equipment-search" type="search" placeholder="Buscar nome ou patrimonio" /></label>
                <div class="segmented" aria-label="Filtrar por status">
                  <button class="active" data-filter="todos" type="button">Todos</button>
                  <button data-filter="disponivel" type="button">Disponiveis</button>
                  <button data-filter="emprestado" type="button">Emprestados</button>
                  <button data-filter="manutencao" type="button">Manutencao</button>
                </div>
              </div>
              <div class="equipment-list" id="equipment-list"></div>
            </section>

            <aside class="right-column">
              <section class="attention-panel">
                <div class="section-heading compact"><div><p class="eyebrow">PRIORIDADES</p><h2>Atencao hoje</h2></div><span class="count-badge">5</span></div>
                <div class="attention-list">
                  <button class="attention-row" type="button"><span class="priority-bar critical"></span><span><strong>Notebook Lenovo T14</strong><small>Atrasado ha 4 dias · Ana Souza</small></span><span class="row-arrow">›</span></button>
                  <button class="attention-row" type="button"><span class="priority-bar critical"></span><span><strong>Projetor Epson X49</strong><small>Atrasado ha 2 dias · Sala 204</small></span><span class="row-arrow">›</span></button>
                  <button class="attention-row" type="button"><span class="priority-bar due"></span><span><strong>Canon EOS R10</strong><small>Devolucao prevista para hoje</small></span><span class="row-arrow">›</span></button>
                  <button class="attention-row" type="button"><span class="priority-bar repair"></span><span><strong>2 itens em triagem</strong><small>Aguardando diagnostico tecnico</small></span><span class="row-arrow">›</span></button>
                </div>
              </section>

              <section class="activity-panel">
                <div class="section-heading compact"><div><p class="eyebrow">ULTIMOS 7 DIAS</p><h2>Movimentacao</h2></div><span class="trend">+12%</span></div>
                <div class="chart" aria-label="Grafico de movimentacao semanal">
                  <div class="bar-item"><span style="height:34%"></span><small>S</small></div>
                  <div class="bar-item"><span style="height:62%"></span><small>T</small></div>
                  <div class="bar-item"><span style="height:48%"></span><small>Q</small></div>
                  <div class="bar-item"><span style="height:82%"></span><small>Q</small></div>
                  <div class="bar-item"><span style="height:70%"></span><small>S</small></div>
                  <div class="bar-item"><span style="height:42%"></span><small>S</small></div>
                  <div class="bar-item current"><span style="height:92%"></span><small>D</small></div>
                </div>
                <div class="activity-legend"><span><i class="legend-loan"></i>18 retiradas</span><span><i class="legend-return"></i>14 devolucoes</span></div>
              </section>
            </aside>
          </div>
        </div>
      </main>
    </div>

    <dialog class="loan-dialog" id="loan-dialog">
      <form method="dialog" id="loan-form">
        <div class="dialog-header"><div><p class="eyebrow">NOVO REGISTRO</p><h2>Novo emprestimo</h2></div><button class="icon-button" value="cancel" aria-label="Fechar">${icon('close')}</button></div>
        <p class="dialog-intro">Registre quem vai retirar o item e combine a data de devolucao.</p>
        <label>Colaborador<input name="person" required placeholder="Nome completo" /></label>
        <label>Equipamento<select name="equipment" required><option value="">Selecione um item disponivel</option><option>Dell UltraSharp 27 · MON-022</option><option>Logitech MX Keys · PER-031</option></select></label>
        <label>Data de devolucao<input name="returnDate" type="date" required /></label>
        <div class="rule-note"><strong>Regra ativa</strong><span>Limite de 3 itens por pessoa e prazo maximo de 14 dias.</span></div>
        <div class="dialog-actions"><button class="secondary-button" value="cancel">Cancelar</button><button class="primary-button" value="default" type="submit">Confirmar emprestimo</button></div>
      </form>
    </dialog>
    <div class="toast" id="toast" role="status" aria-live="polite"></div>
  `;

  bindInteractions();
  renderEquipmentRows('todos', '');
  void loadHealth();
}

function renderEquipmentRows(filter: string, query: string) {
  const list = document.querySelector<HTMLDivElement>('#equipment-list');
  if (!list) return;

  const normalizedQuery = query.trim().toLowerCase();
  const visible = equipment.filter((item) => {
    const matchesStatus = filter === 'todos' || item.status === filter;
    const matchesQuery = `${item.name} ${item.code} ${item.category}`.toLowerCase().includes(normalizedQuery);
    return matchesStatus && matchesQuery;
  });

  if (!visible.length) {
    list.innerHTML = '<div class="empty-state"><strong>Nenhum item encontrado</strong><span>Tente ajustar a busca ou o filtro.</span></div>';
    return;
  }

  list.innerHTML = visible.map((item) => `
    <button class="equipment-row" type="button">
      <img src="${item.image}" alt="" />
      <span class="equipment-name"><strong>${item.name}</strong><small>${item.code} · ${item.category}</small></span>
      <span class="equipment-detail">${item.detail}</span>
      <span class="status status-${item.status}"><i></i>${statusLabels[item.status]}</span>
      <span class="row-arrow">›</span>
    </button>
  `).join('');
}

function bindInteractions() {
  let activeFilter = 'todos';
  const search = document.querySelector<HTMLInputElement>('#equipment-search');
  const filterButtons = document.querySelectorAll<HTMLButtonElement>('[data-filter]');

  search?.addEventListener('input', () => renderEquipmentRows(activeFilter, search.value));
  filterButtons.forEach((button) => {
    button.addEventListener('click', () => {
      activeFilter = button.dataset.filter ?? 'todos';
      filterButtons.forEach((item) => item.classList.toggle('active', item === button));
      renderEquipmentRows(activeFilter, search?.value ?? '');
    });
  });

  document.querySelectorAll<HTMLButtonElement>('.nav-item').forEach((button) => {
    button.addEventListener('click', () => {
      document.querySelectorAll('.nav-item').forEach((item) => item.classList.remove('active'));
      button.classList.add('active');
      closeMobileMenu();
    });
  });

  const sidebar = document.querySelector<HTMLElement>('#sidebar');
  const scrim = document.querySelector<HTMLButtonElement>('#sidebar-scrim');
  const menuButton = document.querySelector<HTMLButtonElement>('#mobile-menu-button');
  const closeMobileMenu = () => {
    sidebar?.classList.remove('open');
    scrim?.classList.remove('open');
    menuButton?.setAttribute('aria-expanded', 'false');
  };
  menuButton?.setAttribute('aria-expanded', 'false');
  menuButton?.addEventListener('click', () => {
    const open = !sidebar?.classList.contains('open');
    sidebar?.classList.toggle('open', open);
    scrim?.classList.toggle('open', open);
    menuButton.setAttribute('aria-expanded', String(open));
  });
  scrim?.addEventListener('click', closeMobileMenu);

  const notificationButton = document.querySelector<HTMLButtonElement>('#notification-button');
  const popover = document.querySelector<HTMLDivElement>('#notification-popover');
  notificationButton?.addEventListener('click', () => {
    if (popover) popover.hidden = !popover.hidden;
  });

  const dialog = document.querySelector<HTMLDialogElement>('#loan-dialog');
  document.querySelectorAll<HTMLButtonElement>('[data-open-loan]').forEach((button) => {
    button.addEventListener('click', () => dialog?.showModal());
  });
  dialog?.addEventListener('click', (event) => {
    if (event.target === dialog) dialog.close();
  });

  const form = document.querySelector<HTMLFormElement>('#loan-form');
  form?.addEventListener('submit', (event) => {
    event.preventDefault();
    dialog?.close();
    form.reset();
    showToast('Emprestimo registrado com sucesso.');
  });
}

async function loadHealth() {
  const state = document.querySelector<HTMLDivElement>('#api-state');
  if (!state) return;

  const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL as string | undefined)?.replace(/\/$/, '') || 'http://localhost:8080';
  try {
    const response = await fetch(`${apiBaseUrl}/health`, { signal: AbortSignal.timeout(3500) });
    if (!response.ok) throw new Error('API indisponivel');
    state.classList.add('online');
    state.innerHTML = '<span class="state-dot"></span><div><strong>Sistema operacional</strong><small>API e banco conectados</small></div>';
  } catch {
    state.classList.add('demo');
    state.innerHTML = '<span class="state-dot"></span><div><strong>Modo demonstracao</strong><small>API local indisponivel</small></div>';
  }
}

function showToast(message: string) {
  const toast = document.querySelector<HTMLDivElement>('#toast');
  if (!toast) return;
  toast.textContent = message;
  toast.classList.add('visible');
  window.setTimeout(() => toast.classList.remove('visible'), 3200);
}
