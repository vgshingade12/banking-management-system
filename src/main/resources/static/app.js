// ApexBank Core Banking System — Enterprise Frontend Engine

const API_BASE = window.location.origin.includes('http') && !window.location.protocol.startsWith('file')
  ? `${window.location.origin}/api`
  : 'http://localhost:8080/api';

// Global Data Caches
let globalCustomers = [];
let globalAccounts = [];
let globalCurrentAccountTransactions = [];

// DOM Loaded Initialization
document.addEventListener('DOMContentLoaded', () => {
  initNavigation();
  initSubTabs();
  initForms();
  initModals();
  checkApiHealth();
  refreshAllData();

  // Attach Topbar Buttons safely
  const btnRefresh = document.getElementById('btn-refresh');
  if (btnRefresh) btnRefresh.addEventListener('click', () => {
    refreshAllData();
    showToast('Data refreshed successfully', 'success');
  });

  const btnQuickCust = document.getElementById('btn-quick-customer');
  if (btnQuickCust) btnQuickCust.addEventListener('click', () => openModal('modal-customer'));

  const btnQuickAcc = document.getElementById('btn-quick-account');
  if (btnQuickAcc) btnQuickAcc.addEventListener('click', () => openModal('modal-account'));

  // Search Listeners
  const searchCust = document.getElementById('search-customers');
  if (searchCust) searchCust.addEventListener('input', renderCustomersTable);

  const searchAcc = document.getElementById('search-accounts');
  if (searchAcc) searchAcc.addEventListener('input', renderAccountsTable);
});

// Check API Health & Latency
async function checkApiHealth() {
  const statusTag = document.getElementById('api-status-tag');
  if (!statusTag) return;

  const startTime = Date.now();
  try {
    const res = await fetch(`${API_BASE}/customers`);
    const latency = Date.now() - startTime;
    if (res.ok) {
      statusTag.textContent = `ONLINE (${latency}ms)`;
      statusTag.className = 'text-emerald-400 font-semibold';
    } else {
      statusTag.textContent = 'DEGRADED';
      statusTag.className = 'text-amber-400 font-semibold';
    }
  } catch (err) {
    statusTag.textContent = 'OFFLINE';
    statusTag.className = 'text-rose-400 font-semibold';
  }
}

// Global Refresh Data
async function refreshAllData() {
  await Promise.all([loadCustomers(), loadAccounts()]);
  updateDashboardStats();
  populateDropdowns();
}

// Navigation Handling
function initNavigation() {
  const navButtons = document.querySelectorAll('.nav-item');
  navButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      const targetTab = btn.getAttribute('data-tab');
      switchTab(targetTab);
    });
  });
}

function switchTab(tabId, subtab = null) {
  document.querySelectorAll('.nav-item').forEach(b => b.classList.remove('active'));
  const activeBtn = document.querySelector(`.nav-item[data-tab="${tabId}"]`);
  if (activeBtn) activeBtn.classList.add('active');

  document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
  const targetContent = document.getElementById(tabId);
  if (targetContent) targetContent.classList.add('active');

  // Page title update
  const titleMap = {
    'tab-overview': { title: 'Dashboard Overview', sub: 'Real-time telemetry and management controls for ApexBank Core.' },
    'tab-customers': { title: 'Customer Management', sub: 'Directory of registered individual & enterprise bank clients.' },
    'tab-accounts': { title: 'Bank Accounts Ledger', sub: 'Savings and Current account status and balance controls.' },
    'tab-operations': { title: 'Banking Service Counter', sub: 'Execute cash deposits, withdrawals, and inter-account transfers.' },
    'tab-ledger': { title: 'Transaction Audit Log', sub: 'Detailed transaction history and filtering per account.' }
  };

  if (titleMap[tabId]) {
    const pageTitle = document.getElementById('page-title');
    const pageSub = document.getElementById('page-subtitle');
    if (pageTitle) pageTitle.textContent = titleMap[tabId].title;
    if (pageSub) pageSub.textContent = titleMap[tabId].sub;
  }

  if (subtab) {
    const subBtn = document.querySelector(`.ops-nav-btn[data-subtab="subtab-${subtab}"]`);
    if (subBtn) subBtn.click();
  }
}

// Sub-Tab Ops Handling
function initSubTabs() {
  const opsNavBtns = document.querySelectorAll('.ops-nav-btn');
  opsNavBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      opsNavBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');

      const targetSubtab = btn.getAttribute('data-subtab');
      document.querySelectorAll('.ops-subtab').forEach(s => s.classList.add('hidden'));
      const subTarget = document.getElementById(targetSubtab);
      if (subTarget) subTarget.classList.remove('hidden');
    });
  });
}

// Preset Amount Buttons Handler
function setPresetAmount(fieldId, val) {
  const field = document.getElementById(fieldId);
  if (field) field.value = val;
}

// Copy to Clipboard Utility
function copyToClipboard(text) {
  navigator.clipboard.writeText(text).then(() => {
    showToast(`Copied "${text}" to clipboard`, 'success');
  }).catch(() => {
    showToast('Failed to copy to clipboard', 'error');
  });
}

// Load Customers from API
async function loadCustomers() {
  try {
    const res = await fetch(`${API_BASE}/customers`);
    if (!res.ok) throw new Error('Failed to fetch customers');
    globalCustomers = await res.json();
    renderCustomersTable();
  } catch (err) {
    console.error('Error fetching customers:', err);
  }
}

// Render Customers Table
function renderCustomersTable() {
  const tbody = document.getElementById('customers-table-body');
  if (!tbody) return;

  const searchElem = document.getElementById('search-customers');
  const search = searchElem ? searchElem.value.toLowerCase().trim() : '';

  const filtered = globalCustomers.filter(c => 
    `${c.firstName} ${c.lastName}`.toLowerCase().includes(search) ||
    (c.email && c.email.toLowerCase().includes(search)) ||
    (c.phone && c.phone.includes(search)) ||
    (c.customerCode && c.customerCode.toLowerCase().includes(search))
  );

  if (filtered.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" class="text-center py-6 text-slate-500">No customers found. Click "+ Add Customer" to create one.</td></tr>`;
    return;
  }

  tbody.innerHTML = filtered.map(c => `
    <tr class="hover:bg-slate-900/40 transition-colors">
      <td class="py-3 px-4 font-mono text-xs text-slate-400">#${c.id}</td>
      <td class="py-3 px-4 font-mono text-xs text-cyan-400 font-bold">
        <span class="cursor-pointer hover:underline" onclick="copyToClipboard('${c.customerCode}')" title="Click to copy code">${c.customerCode}</span>
      </td>
      <td class="py-3 px-4 font-bold text-white">${escapeHtml(c.firstName)} ${escapeHtml(c.lastName)}</td>
      <td class="py-3 px-4 text-slate-300 text-xs">${escapeHtml(c.email)}</td>
      <td class="py-3 px-4 text-slate-300 font-mono text-xs">${escapeHtml(c.phone)}</td>
      <td class="py-3 px-4 text-slate-400 text-xs">${escapeHtml(c.address || '—')}</td>
      <td class="py-3 px-4 text-right flex items-center justify-end gap-2">
        <button class="btn btn-secondary text-xs py-1 px-2.5" onclick="openAccountForCustomer(${c.id})">
          <i class="fa-solid fa-plus text-emerald-400"></i> Open Account
        </button>
        <button class="btn btn-secondary text-xs py-1 px-2" onclick="editCustomer(${c.id})" title="Edit Customer">
          <i class="fa-solid fa-pen text-amber-400"></i>
        </button>
        <button class="btn btn-secondary text-xs py-1 px-2" onclick="confirmDeleteCustomer(${c.id}, '${escapeHtml(c.firstName)} ${escapeHtml(c.lastName)}')" title="Delete Customer">
          <i class="fa-solid fa-trash text-rose-400"></i>
        </button>
      </td>
    </tr>
  `).join('');
}

// Load Accounts from API
async function loadAccounts() {
  try {
    const res = await fetch(`${API_BASE}/accounts`);
    if (!res.ok) throw new Error('Failed to fetch accounts');
    globalAccounts = await res.json();
    renderAccountsTable();
    renderOverviewAccountsTable();
  } catch (err) {
    console.error('Error fetching accounts:', err);
  }
}

// Render Accounts Table
function renderAccountsTable() {
  const tbody = document.getElementById('accounts-table-body');
  if (!tbody) return;

  const searchElem = document.getElementById('search-accounts');
  const search = searchElem ? searchElem.value.toLowerCase().trim() : '';

  const filtered = globalAccounts.filter(a => 
    (a.accountNumber && a.accountNumber.toLowerCase().includes(search)) ||
    (a.customerName && a.customerName.toLowerCase().includes(search))
  );

  if (filtered.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" class="text-center py-6 text-slate-500">No bank accounts found. Click "+ Open New Account" to open one.</td></tr>`;
    return;
  }

  tbody.innerHTML = filtered.map(a => `
    <tr class="hover:bg-slate-900/40 transition-colors">
      <td class="py-3 px-4 font-mono text-sm text-cyan-400 font-bold">
        <span class="cursor-pointer hover:underline" onclick="copyToClipboard('${a.accountNumber}')" title="Click to copy account number">${a.accountNumber}</span>
      </td>
      <td class="py-3 px-4 font-bold text-white">${escapeHtml(a.customerName)}</td>
      <td class="py-3 px-4"><span class="badge ${a.accountType === 'SAVINGS' ? 'badge-savings' : 'badge-current'}">${a.accountType}</span></td>
      <td class="py-3 px-4 text-right font-mono font-bold text-emerald-400">₹${formatMoney(a.balance)}</td>
      <td class="py-3 px-4 text-center">${getStatusBadge(a.status)}</td>
      <td class="py-3 px-4 text-slate-400 text-xs">${formatDate(a.createdAt)}</td>
      <td class="py-3 px-4 text-right flex items-center justify-end gap-2">
        <button class="btn btn-secondary text-xs py-1 px-2.5" onclick="viewAccountDetails('${a.accountNumber}')" title="View Statement">
          <i class="fa-solid fa-receipt text-cyan-400"></i> Statement
        </button>
        <select onchange="updateAccountStatus(${a.id}, this.value)" class="bg-slate-900 border border-slate-800 rounded px-2 py-1 text-xs text-slate-300 outline-none cursor-pointer">
          <option value="ACTIVE" ${a.status === 'ACTIVE' ? 'selected' : ''}>Active</option>
          <option value="BLOCKED" ${a.status === 'BLOCKED' ? 'selected' : ''}>Blocked</option>
          <option value="CLOSED" ${a.status === 'CLOSED' ? 'selected' : ''}>Closed</option>
        </select>
      </td>
    </tr>
  `).join('');
}

// Render Overview Accounts Table
function renderOverviewAccountsTable() {
  const tbody = document.getElementById('overview-accounts-body');
  if (!tbody) return;

  if (globalAccounts.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" class="text-center py-6 text-slate-500">No accounts active yet.</td></tr>`;
    return;
  }

  tbody.innerHTML = globalAccounts.slice(0, 5).map(a => `
    <tr class="hover:bg-slate-900/40 transition-colors">
      <td class="py-3 px-4 font-mono text-sm text-cyan-400 font-bold">${a.accountNumber}</td>
      <td class="py-3 px-4 font-bold text-white">${escapeHtml(a.customerName)}</td>
      <td class="py-3 px-4"><span class="badge ${a.accountType === 'SAVINGS' ? 'badge-savings' : 'badge-current'}">${a.accountType}</span></td>
      <td class="py-3 px-4 text-right font-mono font-bold text-emerald-400">₹${formatMoney(a.balance)}</td>
      <td class="py-3 px-4 text-center">${getStatusBadge(a.status)}</td>
      <td class="py-3 px-4 text-right">
        <button class="btn btn-secondary text-xs py-1 px-2.5" onclick="quickSelectAccountForDeposit('${a.accountNumber}')">
          <i class="fa-solid fa-arrow-down-left text-emerald-400"></i> Deposit
        </button>
      </td>
    </tr>
  `).join('');
}

// Quick Select Deposit Shortcut
function quickSelectAccountForDeposit(accNum) {
  switchTab('tab-operations', 'deposit');
  const depAcc = document.getElementById('deposit-account');
  if (depAcc) depAcc.value = accNum;
}

// Account Status Badge Helper
function getStatusBadge(status) {
  if (status === 'ACTIVE') return `<span class="badge badge-active"><i class="fa-solid fa-circle text-[8px]"></i> ACTIVE</span>`;
  if (status === 'BLOCKED') return `<span class="badge badge-blocked"><i class="fa-solid fa-lock text-[8px]"></i> BLOCKED</span>`;
  return `<span class="badge badge-closed"><i class="fa-solid fa-circle-xmark text-[8px]"></i> CLOSED</span>`;
}

// Update Dashboard Stats
function updateDashboardStats() {
  const statCust = document.getElementById('stat-customers');
  if (statCust) statCust.textContent = globalCustomers.length;

  const statAcc = document.getElementById('stat-accounts');
  if (statAcc) statAcc.textContent = globalAccounts.filter(a => a.status === 'ACTIVE').length;

  const totalLiquidity = globalAccounts.reduce((sum, a) => sum + (parseFloat(a.balance) || 0), 0);
  const statLiq = document.getElementById('stat-liquidity');
  if (statLiq) statLiq.textContent = `₹${formatMoney(totalLiquidity)}`;
}

// Populate Select Dropdowns
function populateDropdowns() {
  const customerSelect = document.getElementById('acc-customer-id');
  if (customerSelect) {
    customerSelect.innerHTML = `<option value="">Select a customer...</option>` + globalCustomers.map(c => `
      <option value="${c.id}">${escapeHtml(c.firstName)} ${escapeHtml(c.lastName)} (${c.customerCode})</option>
    `).join('');
  }

  const accountOptions = globalAccounts.map(a => `
    <option value="${a.accountNumber}">${a.accountNumber} — ${escapeHtml(a.customerName)} (₹${formatMoney(a.balance)}) [${a.status}]</option>
  `).join('');

  const defaultOption = `<option value="">Select an account...</option>`;

  const depAcc = document.getElementById('deposit-account');
  if (depAcc) depAcc.innerHTML = defaultOption + accountOptions;

  const withAcc = document.getElementById('withdraw-account');
  if (withAcc) withAcc.innerHTML = defaultOption + accountOptions;

  const transFrom = document.getElementById('transfer-from');
  if (transFrom) transFrom.innerHTML = defaultOption + accountOptions;

  const transTo = document.getElementById('transfer-to');
  if (transTo) transTo.innerHTML = defaultOption + accountOptions;

  const ledgerAcc = document.getElementById('ledger-account-select');
  if (ledgerAcc) ledgerAcc.innerHTML = defaultOption + accountOptions;
}

// Form Handlers Initialization
function initForms() {
  // Create Customer Form
  const formCust = document.getElementById('form-customer');
  if (formCust) {
    formCust.addEventListener('submit', async (e) => {
      e.preventDefault();
      const payload = {
        firstName: document.getElementById('cust-first-name').value.trim(),
        lastName: document.getElementById('cust-last-name').value.trim(),
        email: document.getElementById('cust-email').value.trim(),
        phone: document.getElementById('cust-phone').value.trim(),
        address: document.getElementById('cust-address').value.trim()
      };

      try {
        const res = await fetch(`${API_BASE}/customers`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || data.error || 'Failed to create customer');

        showToast(`Customer "${data.firstName} ${data.lastName}" created successfully!`, 'success');
        closeModal('modal-customer');
        formCust.reset();
        refreshAllData();
      } catch (err) {
        showToast(err.message, 'error');
      }
    });
  }

  // Edit Customer Form
  const formEditCust = document.getElementById('form-edit-customer');
  if (formEditCust) {
    formEditCust.addEventListener('submit', async (e) => {
      e.preventDefault();
      const custId = document.getElementById('edit-cust-id').value;
      const payload = {
        firstName: document.getElementById('edit-cust-first-name').value.trim(),
        lastName: document.getElementById('edit-cust-last-name').value.trim(),
        email: document.getElementById('edit-cust-email').value.trim(),
        phone: document.getElementById('edit-cust-phone').value.trim(),
        address: document.getElementById('edit-cust-address').value.trim()
      };

      try {
        const res = await fetch(`${API_BASE}/customers/${custId}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || data.error || 'Failed to update customer');

        showToast(`Customer profile updated successfully!`, 'success');
        closeModal('modal-edit-customer');
        refreshAllData();
      } catch (err) {
        showToast(err.message, 'error');
      }
    });
  }

  // Open Account Form
  const formAcc = document.getElementById('form-account');
  if (formAcc) {
    formAcc.addEventListener('submit', async (e) => {
      e.preventDefault();
      const payload = {
        customerId: parseInt(document.getElementById('acc-customer-id').value),
        accountType: document.getElementById('acc-type').value
      };

      try {
        const res = await fetch(`${API_BASE}/accounts`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || data.error || 'Failed to open account');

        showToast(`Account ${data.accountNumber} created for ${data.customerName}!`, 'success');
        closeModal('modal-account');
        formAcc.reset();
        refreshAllData();
      } catch (err) {
        showToast(err.message, 'error');
      }
    });
  }

  // Deposit Form
  const subDeposit = document.getElementById('subtab-deposit');
  if (subDeposit) {
    subDeposit.addEventListener('submit', async (e) => {
      e.preventDefault();
      const accountNumber = document.getElementById('deposit-account').value;
      const amount = parseFloat(document.getElementById('deposit-amount').value);
      const description = document.getElementById('deposit-desc').value.trim();

      if (!accountNumber) {
        showToast('Please select a target account for deposit.', 'warning');
        return;
      }
      if (isNaN(amount) || amount <= 0) {
        showToast('Please enter a valid deposit amount greater than ₹0.', 'warning');
        return;
      }

      try {
        const res = await fetch(`${API_BASE}/accounts/${accountNumber}/deposit`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ amount, description })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || data.error || 'Deposit failed');

        showToast(`Deposit Successful! TXN Ref: ${data.referenceNumber}. New Balance: ₹${formatMoney(data.newBalance)}`, 'success');
        subDeposit.reset();
        refreshAllData();
      } catch (err) {
        showToast(err.message, 'error');
      }
    });
  }

  // Withdrawal Form
  const subWithdraw = document.getElementById('subtab-withdraw');
  if (subWithdraw) {
    subWithdraw.addEventListener('submit', async (e) => {
      e.preventDefault();
      const accountNumber = document.getElementById('withdraw-account').value;
      const amount = parseFloat(document.getElementById('withdraw-amount').value);
      const description = document.getElementById('withdraw-desc').value.trim();

      if (!accountNumber) {
        showToast('Please select an account for withdrawal.', 'warning');
        return;
      }
      if (isNaN(amount) || amount <= 0) {
        showToast('Please enter a valid withdrawal amount.', 'warning');
        return;
      }

      try {
        const res = await fetch(`${API_BASE}/accounts/${accountNumber}/withdraw`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ amount, description })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || data.error || 'Withdrawal failed');

        showToast(`Withdrawal Successful! TXN Ref: ${data.referenceNumber}. New Balance: ₹${formatMoney(data.newBalance)}`, 'success');
        subWithdraw.reset();
        refreshAllData();
      } catch (err) {
        showToast(err.message, 'error');
      }
    });
  }

  // Transfer Form
  const subTransfer = document.getElementById('subtab-transfer');
  if (subTransfer) {
    subTransfer.addEventListener('submit', async (e) => {
      e.preventDefault();
      const payload = {
        fromAccount: document.getElementById('transfer-from').value,
        toAccount: document.getElementById('transfer-to').value,
        amount: parseFloat(document.getElementById('transfer-amount').value),
        description: document.getElementById('transfer-desc').value.trim()
      };

      if (!payload.fromAccount || !payload.toAccount) {
        showToast('Please select both sender and receiver accounts.', 'warning');
        return;
      }

      if (payload.fromAccount === payload.toAccount) {
        showToast('Source and Destination accounts cannot be the same.', 'error');
        return;
      }

      if (isNaN(payload.amount) || payload.amount <= 0) {
        showToast('Please enter a valid transfer amount.', 'warning');
        return;
      }

      try {
        const res = await fetch(`${API_BASE}/transactions/transfer`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || data.error || 'Transfer failed');

        showToast(`Transfer Complete! TXN Ref: ${data.referenceNumber}. Amount: ₹${formatMoney(data.amount)} transferred.`, 'success');
        subTransfer.reset();
        refreshAllData();
      } catch (err) {
        showToast(err.message, 'error');
      }
    });
  }

  // Ledger Filter Form
  const ledgerForm = document.getElementById('ledger-filter-form');
  if (ledgerForm) {
    ledgerForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const accNum = document.getElementById('ledger-account-select').value;
      const typeSelect = document.getElementById('ledger-type-select');
      const type = typeSelect ? typeSelect.value : '';

      if (!accNum) {
        showToast('Please select an account to view transaction history.', 'warning');
        return;
      }

      try {
        let url = `${API_BASE}/accounts/${accNum}/transactions`;
        if (type) url += `?transactionType=${type}`;

        const res = await fetch(url);
        const data = await res.json();
        if (!res.ok) throw new Error('Failed to fetch transactions');

        renderLedgerTable(data);
      } catch (err) {
        showToast(err.message, 'error');
      }
    });
  }
}

// Render Transaction Ledger
function renderLedgerTable(transactions) {
  const tbody = document.getElementById('ledger-table-body');
  if (!tbody) return;

  if (!transactions || transactions.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" class="text-center py-6 text-slate-500">No transactions recorded for this account filter.</td></tr>`;
    return;
  }

  tbody.innerHTML = transactions.map(t => {
    const isPositive = t.transactionType === 'DEPOSIT' || t.transactionType === 'TRANSFER_IN';
    const amountColor = isPositive ? 'text-emerald-400' : 'text-rose-400';
    const amountPrefix = isPositive ? '+₹' : '-₹';

    return `
      <tr class="hover:bg-slate-900/40 transition-colors">
        <td class="py-3 px-4 font-mono text-xs text-cyan-400 font-bold">
          <span class="cursor-pointer hover:underline" onclick="copyToClipboard('${t.referenceNumber}')">${t.referenceNumber}</span>
        </td>
        <td class="py-3 px-4 text-slate-300 text-xs">${formatDate(t.timestamp)}</td>
        <td class="py-3 px-4 font-mono text-xs text-slate-300">${t.accountNumber}</td>
        <td class="py-3 px-4"><span class="badge ${isPositive ? 'badge-active' : 'badge-blocked'}">${t.transactionType}</span></td>
        <td class="py-3 px-4 text-right font-mono font-bold ${amountColor}">${amountPrefix}${formatMoney(t.amount)}</td>
        <td class="py-3 px-4 text-right font-mono text-slate-200">₹${formatMoney(t.balanceAfter)}</td>
        <td class="py-3 px-4 text-slate-400 text-xs">${escapeHtml(t.description || '—')}</td>
      </tr>
    `;
  }).join('');
}

// View Account Details / Statement Modal
async function viewAccountDetails(accNum) {
  try {
    const res = await fetch(`${API_BASE}/accounts/number/${accNum}`);
    if (!res.ok) throw new Error('Account not found');
    const acc = await res.json();

    const txnRes = await fetch(`${API_BASE}/accounts/${accNum}/transactions`);
    const txns = txnRes.ok ? await txnRes.json() : [];

    document.getElementById('stmt-acc-num').textContent = acc.accountNumber;
    document.getElementById('stmt-cust-name').textContent = acc.customerName;
    document.getElementById('stmt-type').textContent = acc.accountType;
    document.getElementById('stmt-balance').textContent = `₹${formatMoney(acc.balance)}`;
    document.getElementById('stmt-status').innerHTML = getStatusBadge(acc.status);

    const tbody = document.getElementById('stmt-table-body');
    if (tbody) {
      if (txns.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-center py-4 text-slate-500">No transactions recorded yet.</td></tr>`;
      } else {
        tbody.innerHTML = txns.map(t => {
          const isPositive = t.transactionType === 'DEPOSIT' || t.transactionType === 'TRANSFER_IN';
          return `
            <tr class="border-b border-slate-800/50">
              <td class="py-2 px-3 font-mono text-xs text-cyan-400">${t.referenceNumber}</td>
              <td class="py-2 px-3 text-xs text-slate-300">${formatDate(t.timestamp)}</td>
              <td class="py-2 px-3 text-xs">${t.transactionType}</td>
              <td class="py-2 px-3 text-right font-mono text-xs ${isPositive ? 'text-emerald-400' : 'text-rose-400'}">${isPositive ? '+' : '-'}₹${formatMoney(t.amount)}</td>
              <td class="py-2 px-3 text-right font-mono text-xs text-slate-300">₹${formatMoney(t.balanceAfter)}</td>
            </tr>
          `;
        }).join('');
      }
    }

    openModal('modal-statement');
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// Edit Customer Modal Trigger
function editCustomer(id) {
  const cust = globalCustomers.find(c => c.id === id);
  if (!cust) return;

  document.getElementById('edit-cust-id').value = cust.id;
  document.getElementById('edit-cust-first-name').value = cust.firstName;
  document.getElementById('edit-cust-last-name').value = cust.lastName;
  document.getElementById('edit-cust-email').value = cust.email;
  document.getElementById('edit-cust-phone').value = cust.phone;
  document.getElementById('edit-cust-address').value = cust.address || '';

  openModal('modal-edit-customer');
}

// Confirm Delete Customer Modal Trigger
function confirmDeleteCustomer(id, name) {
  document.getElementById('delete-cust-id').value = id;
  document.getElementById('delete-cust-name').textContent = name;
  openModal('modal-delete-customer');
}

// Delete Customer Action
async function executeDeleteCustomer() {
  const id = document.getElementById('delete-cust-id').value;
  try {
    const res = await fetch(`${API_BASE}/customers/${id}`, { method: 'DELETE' });
    if (!res.ok) throw new Error('Failed to delete customer. Ensure customer has no open accounts.');

    showToast('Customer deleted successfully', 'success');
    closeModal('modal-delete-customer');
    refreshAllData();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// Account Status Update
async function updateAccountStatus(accountId, newStatus) {
  try {
    const res = await fetch(`${API_BASE}/accounts/${accountId}/status`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status: newStatus })
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to update status');

    showToast(`Account ${data.accountNumber} status changed to ${newStatus}`, 'success');
    refreshAllData();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// Helper: Open Account For Customer Shortcut
function openAccountForCustomer(customerId) {
  openModal('modal-account');
  const custSelect = document.getElementById('acc-customer-id');
  if (custSelect) custSelect.value = customerId;
}

// Modal Toggle Helpers
function initModals() {
  document.querySelectorAll('.modal-backdrop').forEach(modal => {
    modal.addEventListener('click', (e) => {
      if (e.target === modal) modal.classList.add('hidden');
    });
  });
}

function openModal(id) {
  const modal = document.getElementById(id);
  if (modal) modal.classList.remove('hidden');
}

function closeModal(id) {
  const modal = document.getElementById(id);
  if (modal) modal.classList.add('hidden');
}

// Toast System
function showToast(message, type = 'success') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast ${type === 'success' ? 'toast-success' : type === 'warning' ? 'toast-warning' : 'toast-error'}`;

  const icon = type === 'success' ? 'fa-circle-check' : type === 'warning' ? 'fa-triangle-exclamation' : 'fa-circle-exclamation';
  toast.innerHTML = `
    <i class="fa-solid ${icon} text-lg"></i>
    <div class="flex-1">${escapeHtml(message)}</div>
  `;

  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(20px)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 4500);
}

// Formatting Utilities
function formatMoney(amount) {
  const val = parseFloat(amount) || 0;
  return val.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatDate(isoStr) {
  if (!isoStr) return '—';
  const d = new Date(isoStr);
  return d.toLocaleString('en-IN', { dateStyle: 'short', timeStyle: 'short' });
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/[&<>"']/g, m => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  })[m]);
}
