// Union Bank of India (VYOM Digital NetBanking) — Enterprise Client Engine

const API_BASE = window.location.origin.includes('http') && !window.location.protocol.startsWith('file')
  ? `${window.location.origin}/api`
  : 'http://localhost:8080/api';

// Global Caches & State
let globalCustomers = [];
let globalAccounts = [];
let globalPayees = [
  { id: 1, name: 'Suresh Kumar', accountNo: 'ACC100002', ifsc: 'UBIN0532145', bankName: 'Union Bank of India', limit: 100000 },
  { id: 2, name: 'Priya Sharma', accountNo: 'ACC100003', ifsc: 'SBIN0001234', bankName: 'State Bank of India', limit: 50000 }
];

let hiddenBalances = {}; // Track account balance visibility
let sessionTimeSeconds = 900; // 15:00 min session timer
let pendingTransferPayload = null; // Store payload during T-PIN confirmation

// DOM Loaded Initialization
document.addEventListener('DOMContentLoaded', () => {
  initNavigation();
  initForms();
  initModals();
  initSessionTimer();
  checkApiHealth();
  refreshAllData();

  // Search input listeners
  const searchCust = document.getElementById('search-customers');
  if (searchCust) searchCust.addEventListener('input', renderCustomersTable);

  const searchAcc = document.getElementById('search-accounts');
  if (searchAcc) searchAcc.addEventListener('input', renderAccountsTable);

  const searchLedger = document.getElementById('search-ledger');
  if (searchLedger) searchLedger.addEventListener('input', filterLedgerTable);
});

// Session Countdown Timer
function initSessionTimer() {
  const timerElem = document.getElementById('session-timer');
  if (!timerElem) return;

  setInterval(() => {
    if (sessionTimeSeconds <= 0) {
      timerElem.textContent = '00:00 EXPIRED';
      timerElem.className = 'text-rose-400 font-bold font-mono';
      showToast('NetBanking session expired for security. Please refresh.', 'warning');
      return;
    }
    sessionTimeSeconds--;
    const mins = String(Math.floor(sessionTimeSeconds / 60)).padStart(2, '0');
    const secs = String(sessionTimeSeconds % 60).padStart(2, '0');
    timerElem.textContent = `${mins}:${secs}`;
  }, 1000);
}

// Check Backend API Health
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

// Refresh Data from REST Endpoints
async function refreshAllData() {
  await Promise.all([loadCustomers(), loadAccounts()]);
  updateDashboardStats();
  populateDropdowns();
  renderAccountCards();
  renderPayeeTable();
}

// Navigation Handling
function initNavigation() {
  const navButtons = document.querySelectorAll('.nav-tab-btn');
  navButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      const targetTab = btn.getAttribute('data-tab');
      switchTab(targetTab);
    });
  });
}

function switchTab(tabId) {
  document.querySelectorAll('.nav-tab-btn').forEach(b => b.classList.remove('active'));
  const activeBtn = document.querySelector(`.nav-tab-btn[data-tab="${tabId}"]`);
  if (activeBtn) activeBtn.classList.add('active');

  document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
  const targetContent = document.getElementById(tabId);
  if (targetContent) targetContent.classList.add('active');
}

// Load Customers
async function loadCustomers() {
  try {
    const res = await fetch(`${API_BASE}/customers`);
    if (!res.ok) throw new Error('Failed to fetch customers');
    globalCustomers = await res.json();
    renderCustomersTable();
  } catch (err) {
    console.error('Customer fetch error:', err);
  }
}

// Render Customers Directory
function renderCustomersTable() {
  const tbody = document.getElementById('customers-table-body');
  if (!tbody) return;

  const search = document.getElementById('search-customers')?.value.toLowerCase().trim() || '';

  const filtered = globalCustomers.filter(c => 
    `${c.firstName} ${c.lastName}`.toLowerCase().includes(search) ||
    (c.email && c.email.toLowerCase().includes(search)) ||
    (c.phone && c.phone.includes(search)) ||
    (c.customerCode && c.customerCode.toLowerCase().includes(search))
  );

  if (filtered.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" class="text-center py-6 text-slate-500">No customer records found. Click "+ Add Customer" to register one.</td></tr>`;
    return;
  }

  tbody.innerHTML = filtered.map(c => `
    <tr class="hover:bg-slate-900/50 transition-colors border-b border-slate-800/40">
      <td class="py-3 px-4 font-mono text-xs text-slate-400">#${c.id}</td>
      <td class="py-3 px-4 font-mono text-xs text-amber-400 font-bold">
        <span class="cursor-pointer hover:underline" onclick="copyToClipboard('${c.customerCode}')" title="Copy Customer Code">${c.customerCode}</span>
      </td>
      <td class="py-3 px-4 font-bold text-white">${escapeHtml(c.firstName)} ${escapeHtml(c.lastName)}</td>
      <td class="py-3 px-4 text-slate-300 text-xs">${escapeHtml(c.email)}</td>
      <td class="py-3 px-4 text-slate-300 font-mono text-xs">${escapeHtml(c.phone)}</td>
      <td class="py-3 px-4 text-slate-400 text-xs">${escapeHtml(c.address || '—')}</td>
      <td class="py-3 px-4 text-right flex items-center justify-end gap-2">
        <button class="btn btn-secondary text-xs py-1 px-2.5" onclick="openAccountForCustomer(${c.id})">
          <i class="fa-solid fa-plus text-emerald-400"></i> Open Account
        </button>
        <button class="btn btn-secondary text-xs py-1 px-2" onclick="editCustomer(${c.id})">
          <i class="fa-solid fa-pen text-amber-400"></i>
        </button>
        <button class="btn btn-secondary text-xs py-1 px-2" onclick="confirmDeleteCustomer(${c.id}, '${escapeHtml(c.firstName)} ${escapeHtml(c.lastName)}')">
          <i class="fa-solid fa-trash text-rose-400"></i>
        </button>
      </td>
    </tr>
  `).join('');
}

// Load Accounts
async function loadAccounts() {
  try {
    const res = await fetch(`${API_BASE}/accounts`);
    if (!res.ok) throw new Error('Failed to fetch accounts');
    globalAccounts = await res.json();
    renderAccountsTable();
  } catch (err) {
    console.error('Accounts fetch error:', err);
  }
}

// Render Accounts Master Table
function renderAccountsTable() {
  const tbody = document.getElementById('accounts-table-body');
  if (!tbody) return;

  const search = document.getElementById('search-accounts')?.value.toLowerCase().trim() || '';

  const filtered = globalAccounts.filter(a => 
    (a.accountNumber && a.accountNumber.toLowerCase().includes(search)) ||
    (a.customerName && a.customerName.toLowerCase().includes(search))
  );

  if (filtered.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" class="text-center py-6 text-slate-500">No bank accounts found. Click "+ Open Account" to register.</td></tr>`;
    return;
  }

  tbody.innerHTML = filtered.map(a => `
    <tr class="hover:bg-slate-900/50 transition-colors border-b border-slate-800/40">
      <td class="py-3 px-4 font-mono text-sm text-cyan-400 font-bold">
        <span class="cursor-pointer hover:underline" onclick="copyToClipboard('${a.accountNumber}')" title="Copy Account Number">${a.accountNumber}</span>
      </td>
      <td class="py-3 px-4 font-bold text-white">${escapeHtml(a.customerName)}</td>
      <td class="py-3 px-4"><span class="badge ${a.accountType === 'SAVINGS' ? 'badge-active' : 'badge-closed'}">${a.accountType}</span></td>
      <td class="py-3 px-4 text-right font-mono font-bold text-emerald-400">₹${formatMoney(a.balance)}</td>
      <td class="py-3 px-4 text-center">${getStatusBadge(a.status)}</td>
      <td class="py-3 px-4 text-slate-400 text-xs">${formatDate(a.createdAt)}</td>
      <td class="py-3 px-4 text-right flex items-center justify-end gap-2">
        <button class="btn btn-secondary text-xs py-1 px-2.5" onclick="viewAccountStatement('${a.accountNumber}')">
          <i class="fa-solid fa-receipt text-cyan-400"></i> Statement
        </button>
        <select onchange="updateAccountStatus(${a.id}, this.value)" class="bg-slate-900 border border-slate-800 rounded px-2 py-1 text-xs text-slate-300 outline-none cursor-pointer">
          <option value="ACTIVE" ${a.status === 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
          <option value="BLOCKED" ${a.status === 'BLOCKED' ? 'selected' : ''}>BLOCKED</option>
          <option value="CLOSED" ${a.status === 'CLOSED' ? 'selected' : ''}>CLOSED</option>
        </select>
      </td>
    </tr>
  `).join('');
}

// Render Dashboard Account Summary Cards
function renderAccountCards() {
  const container = document.getElementById('account-cards-container');
  if (!container) return;

  if (globalAccounts.length === 0) {
    container.innerHTML = `
      <div class="col-span-full glass-card p-8 text-center text-slate-400">
        <i class="fa-solid fa-wallet text-4xl text-slate-600 mb-3 block"></i>
        No active Union Bank accounts registered yet. Click "+ Open Account" to create your first account.
      </div>
    `;
    return;
  }

  container.innerHTML = globalAccounts.map(a => {
    const isHidden = hiddenBalances[a.accountNumber] !== false; // Default hidden
    const displayBalance = isHidden ? '•••• ••••' : `₹${formatMoney(a.balance)}`;

    return `
      <div class="account-card ${a.accountType === 'CURRENT' ? 'current' : ''}">
        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center gap-2">
            <span class="w-2.5 h-2.5 rounded-full ${a.status === 'ACTIVE' ? 'bg-emerald-400' : 'bg-rose-400'}"></span>
            <span class="text-xs font-bold uppercase tracking-wider text-cyan-300">${a.accountType} ACCOUNT</span>
          </div>
          <span class="text-xs font-mono text-slate-400">IFSC: UBIN0532145</span>
        </div>

        <div class="mb-4">
          <span class="text-xs text-slate-400 uppercase tracking-wider font-semibold block mb-1">Available Balance</span>
          <div class="flex items-center gap-3">
            <span class="font-mono text-2xl font-black text-white" id="card-bal-${a.accountNumber}">${displayBalance}</span>
            <button class="text-slate-400 hover:text-amber-400 text-sm" onclick="toggleBalanceVisibility('${a.accountNumber}')" title="Toggle Show/Hide Balance">
              <i class="fa-solid ${isHidden ? 'fa-eye' : 'fa-eye-slash'}"></i>
            </button>
          </div>
        </div>

        <div class="flex items-center justify-between pt-3 border-t border-slate-700/50 text-xs">
          <div>
            <span class="text-slate-400 block">Account Number</span>
            <span class="font-mono font-bold text-white">${a.accountNumber}</span>
          </div>
          <div>
            <span class="text-slate-400 block text-right">Customer</span>
            <span class="font-semibold text-slate-200 text-right block">${escapeHtml(a.customerName)}</span>
          </div>
        </div>
      </div>
    `;
  }).join('');
}

// Toggle Balance Show/Hide Eye Button
function toggleBalanceVisibility(accNum) {
  hiddenBalances[accNum] = !hiddenBalances[accNum];
  renderAccountCards();
}

// Update Topbar Dashboard Stats
function updateDashboardStats() {
  const statCust = document.getElementById('stat-customers');
  if (statCust) statCust.textContent = globalCustomers.length;

  const statAcc = document.getElementById('stat-accounts');
  if (statAcc) statAcc.textContent = globalAccounts.filter(a => a.status === 'ACTIVE').length;

  const totalLiquidity = globalAccounts.reduce((sum, a) => sum + (parseFloat(a.balance) || 0), 0);
  const statLiq = document.getElementById('stat-liquidity');
  if (statLiq) statLiq.textContent = `₹${formatMoney(totalLiquidity)}`;
}

// Populate Dropdown Select Controls
function populateDropdowns() {
  const customerSelect = document.getElementById('acc-customer-id');
  if (customerSelect) {
    customerSelect.innerHTML = `<option value="">Select Customer...</option>` + globalCustomers.map(c => `
      <option value="${c.id}">${escapeHtml(c.firstName)} ${escapeHtml(c.lastName)} (${c.customerCode})</option>
    `).join('');
  }

  const accountOptions = globalAccounts.map(a => `
    <option value="${a.accountNumber}">${a.accountNumber} — ${escapeHtml(a.customerName)} (₹${formatMoney(a.balance)}) [${a.status}]</option>
  `).join('');

  const defaultOption = `<option value="">Select Account...</option>`;

  const depAcc = document.getElementById('deposit-account');
  if (depAcc) depAcc.innerHTML = defaultOption + accountOptions;

  const withAcc = document.getElementById('withdraw-account');
  if (withAcc) withAcc.innerHTML = defaultOption + accountOptions;

  const transFrom = document.getElementById('transfer-from');
  if (transFrom) transFrom.innerHTML = defaultOption + accountOptions;

  const ledgerAcc = document.getElementById('ledger-account-select');
  if (ledgerAcc) ledgerAcc.innerHTML = defaultOption + accountOptions;

  // Payee Dropdown in Transfer
  const payeeSelect = document.getElementById('transfer-to-payee');
  if (payeeSelect) {
    const payeeOptions = globalPayees.map(p => `<option value="${p.accountNo}">${p.name} (${p.accountNo} - ${p.bankName})</option>`).join('');
    const otherAccOptions = globalAccounts.map(a => `<option value="${a.accountNumber}">${escapeHtml(a.customerName)} (${a.accountNumber})</option>`).join('');
    payeeSelect.innerHTML = `<option value="">Select Payee or Receiver...</option>` + payeeOptions + otherAccOptions;
  }
}

// Form Handlers Setup
function initForms() {
  // Create Customer
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
        if (!res.ok) throw new Error(data.message || data.error || 'Failed to register customer');

        showToast(`Customer "${data.firstName} ${data.lastName}" registered successfully!`, 'success');
        closeModal('modal-customer');
        formCust.reset();
        refreshAllData();
      } catch (err) {
        showToast(err.message, 'error');
      }
    });
  }

  // Open Bank Account
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

        showToast(`Union Bank Account ${data.accountNumber} created for ${data.customerName}!`, 'success');
        closeModal('modal-account');
        formAcc.reset();
        refreshAllData();
      } catch (err) {
        showToast(err.message, 'error');
      }
    });
  }

  // Cash Deposit
  const formDep = document.getElementById('form-deposit');
  if (formDep) {
    formDep.addEventListener('submit', async (e) => {
      e.preventDefault();
      const accountNumber = document.getElementById('deposit-account').value;
      const amount = parseFloat(document.getElementById('deposit-amount').value);
      const description = document.getElementById('deposit-desc').value.trim() || 'Cash Deposit';

      if (!accountNumber) {
        showToast('Please select an account for deposit.', 'warning');
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

        showToast(`Deposit Successful! Ref: ${data.referenceNumber}. New Balance: ₹${formatMoney(data.newBalance)}`, 'success');
        formDep.reset();
        refreshAllData();
      } catch (err) {
        showToast(err.message, 'error');
      }
    });
  }

  // Cash Withdrawal
  const formWith = document.getElementById('form-withdraw');
  if (formWith) {
    formWith.addEventListener('submit', async (e) => {
      e.preventDefault();
      const accountNumber = document.getElementById('withdraw-account').value;
      const amount = parseFloat(document.getElementById('withdraw-amount').value);
      const description = document.getElementById('withdraw-desc').value.trim() || 'Cash Withdrawal';

      if (!accountNumber) {
        showToast('Please select an account for withdrawal.', 'warning');
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

        showToast(`Withdrawal Successful! Ref: ${data.referenceNumber}. New Balance: ₹${formatMoney(data.newBalance)}`, 'success');
        formWith.reset();
        refreshAllData();
      } catch (err) {
        showToast(err.message, 'error');
      }
    });
  }

  // Fund Transfer (Triggers Security T-PIN Modal)
  const formTransfer = document.getElementById('form-transfer');
  if (formTransfer) {
    formTransfer.addEventListener('submit', (e) => {
      e.preventDefault();
      const mode = document.querySelector('input[name="transfer-mode"]:checked')?.value || 'IMPS';
      const fromAccount = document.getElementById('transfer-from').value;
      const toAccount = document.getElementById('transfer-to-payee').value;
      const amount = parseFloat(document.getElementById('transfer-amount').value);
      const description = document.getElementById('transfer-desc').value.trim() || `${mode} Fund Transfer`;

      if (!fromAccount || !toAccount) {
        showToast('Please select both sender and receiver accounts.', 'warning');
        return;
      }
      if (fromAccount === toAccount) {
        showToast('Source and Destination accounts cannot be identical.', 'error');
        return;
      }

      pendingTransferPayload = { fromAccount, toAccount, amount, description, mode };

      document.getElementById('tpin-amount-preview').textContent = `₹${formatMoney(amount)}`;
      document.getElementById('tpin-from-preview').textContent = fromAccount;
      document.getElementById('tpin-to-preview').textContent = toAccount;
      document.getElementById('tpin-mode-preview').textContent = mode;

      openModal('modal-tpin');
    });
  }

  // T-PIN Confirmation Handler
  const formTpin = document.getElementById('form-tpin');
  if (formTpin) {
    formTpin.addEventListener('submit', async (e) => {
      e.preventDefault();
      const pin = document.getElementById('tpin-input').value;
      if (pin !== '1234' && pin.length < 4) {
        showToast('Invalid 4-Digit Security T-PIN. Enter 1234 for demo authorization.', 'error');
        return;
      }

      if (!pendingTransferPayload) return;

      try {
        const res = await fetch(`${API_BASE}/transactions/transfer`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            fromAccount: pendingTransferPayload.fromAccount,
            toAccount: pendingTransferPayload.toAccount,
            amount: pendingTransferPayload.amount,
            description: `${pendingTransferPayload.mode}: ${pendingTransferPayload.description}`
          })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || data.error || 'Transfer failed');

        showToast(`${pendingTransferPayload.mode} Transfer Complete! TXN Ref: ${data.referenceNumber}. Amount: ₹${formatMoney(data.amount)} transferred.`, 'success');
        closeModal('modal-tpin');
        document.getElementById('form-transfer').reset();
        document.getElementById('tpin-input').value = '';
        pendingTransferPayload = null;
        refreshAllData();
      } catch (err) {
        showToast(err.message, 'error');
      }
    });
  }

  // Add Beneficiary Form
  const formPayee = document.getElementById('form-add-payee');
  if (formPayee) {
    formPayee.addEventListener('submit', (e) => {
      e.preventDefault();
      const name = document.getElementById('payee-name').value.trim();
      const accountNo = document.getElementById('payee-account').value.trim();
      const ifsc = document.getElementById('payee-ifsc').value.trim().toUpperCase();
      const limit = parseFloat(document.getElementById('payee-limit').value) || 100000;

      globalPayees.push({
        id: Date.now(),
        name,
        accountNo,
        ifsc,
        bankName: ifsc.startsWith('UBIN') ? 'Union Bank of India' : 'Other Bank',
        limit
      });

      showToast(`Beneficiary "${name}" added to Payee Directory!`, 'success');
      closeModal('modal-add-payee');
      formPayee.reset();
      populateDropdowns();
      renderPayeeTable();
    });
  }

  // Ledger Filter Form
  const formLedger = document.getElementById('ledger-filter-form');
  if (formLedger) {
    formLedger.addEventListener('submit', async (e) => {
      e.preventDefault();
      const accNum = document.getElementById('ledger-account-select').value;
      const type = document.getElementById('ledger-type-select')?.value || '';

      if (!accNum) {
        showToast('Please select an account to view passbook statement.', 'warning');
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

// Render Payee Directory Table
function renderPayeeTable() {
  const tbody = document.getElementById('payee-table-body');
  if (!tbody) return;

  if (globalPayees.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" class="text-center py-6 text-slate-500">No beneficiaries registered yet. Click "+ Add Beneficiary" above.</td></tr>`;
    return;
  }

  tbody.innerHTML = globalPayees.map(p => `
    <tr class="hover:bg-slate-900/50 border-b border-slate-800/40">
      <td class="py-3 px-4 font-bold text-white">${escapeHtml(p.name)}</td>
      <td class="py-3 px-4 font-mono text-xs text-cyan-400 font-bold">${p.accountNo}</td>
      <td class="py-3 px-4 font-mono text-xs text-amber-400">${p.ifsc}</td>
      <td class="py-3 px-4 text-xs text-slate-300">${p.bankName}</td>
      <td class="py-3 px-4 text-right font-mono text-xs text-slate-300">₹${formatMoney(p.limit)}</td>
      <td class="py-3 px-4 text-right flex items-center justify-end gap-2">
        <button class="btn btn-ubi-navy text-xs py-1 px-2.5" onclick="quickTransferToPayee('${p.accountNo}')">
          <i class="fa-solid fa-paper-plane text-cyan-400"></i> Transfer
        </button>
        <button class="btn btn-secondary text-xs py-1 px-2" onclick="deletePayee(${p.id})">
          <i class="fa-solid fa-trash text-rose-400"></i>
        </button>
      </td>
    </tr>
  `).join('');
}

function quickTransferToPayee(accNo) {
  switchTab('tab-transfer');
  const payeeSel = document.getElementById('transfer-to-payee');
  if (payeeSel) payeeSel.value = accNo;
}

function deletePayee(id) {
  globalPayees = globalPayees.filter(p => p.id !== id);
  showToast('Beneficiary removed from Payee Directory', 'success');
  populateDropdowns();
  renderPayeeTable();
}

// Render Passbook Ledger Table
function renderLedgerTable(transactions) {
  const tbody = document.getElementById('ledger-table-body');
  if (!tbody) return;

  if (!transactions || transactions.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" class="text-center py-6 text-slate-500">No transaction records found for this account filter.</td></tr>`;
    return;
  }

  tbody.innerHTML = transactions.map(t => {
    const isPositive = t.transactionType === 'DEPOSIT' || t.transactionType === 'TRANSFER_IN';
    const amountColor = isPositive ? 'text-emerald-400' : 'text-rose-400';

    return `
      <tr class="hover:bg-slate-900/50 border-b border-slate-800/40">
        <td class="py-3 px-4 font-mono text-xs text-cyan-400 font-bold">
          <span class="cursor-pointer hover:underline" onclick="copyToClipboard('${t.referenceNumber}')">${t.referenceNumber}</span>
        </td>
        <td class="py-3 px-4 text-slate-300 text-xs">${formatDate(t.timestamp)}</td>
        <td class="py-3 px-4 font-mono text-xs text-slate-300">${t.accountNumber}</td>
        <td class="py-3 px-4"><span class="badge ${isPositive ? 'badge-active' : 'badge-blocked'}">${t.transactionType}</span></td>
        <td class="py-3 px-4 text-right font-mono font-bold ${amountColor}">${isPositive ? '+' : '-'}₹${formatMoney(t.amount)}</td>
        <td class="py-3 px-4 text-right font-mono text-slate-200">₹${formatMoney(t.balanceAfter)}</td>
        <td class="py-3 px-4 text-slate-400 text-xs">${escapeHtml(t.description || '—')}</td>
      </tr>
    `;
  }).join('');
}

// View E-Passbook Statement Modal
async function viewAccountStatement(accNum) {
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

    const tbody = document.getElementById('stmt-table-body');
    if (tbody) {
      if (txns.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-center py-4 text-slate-500">No transactions recorded for this account.</td></tr>`;
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

// Financial Calculators
function calculateFD() {
  const principal = parseFloat(document.getElementById('fd-amount').value) || 0;
  const tenureMonths = parseFloat(document.getElementById('fd-tenure').value) || 12;
  const rate = parseFloat(document.getElementById('fd-rate').value) || 7.25;

  const interest = (principal * (rate / 100) * (tenureMonths / 12));
  const maturity = principal + interest;

  document.getElementById('fd-result-interest').textContent = `₹${formatMoney(interest)}`;
  document.getElementById('fd-result-maturity').textContent = `₹${formatMoney(maturity)}`;
}

function calculateEMI() {
  const p = parseFloat(document.getElementById('emi-amount').value) || 0;
  const annualRate = parseFloat(document.getElementById('emi-rate').value) || 8.5;
  const tenureYears = parseFloat(document.getElementById('emi-years').value) || 5;

  const r = annualRate / 12 / 100;
  const n = tenureYears * 12;

  let emi = 0;
  if (r > 0 && n > 0) {
    emi = p * r * (Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
  }

  const totalPayment = emi * n;
  const totalInterest = totalPayment - p;

  document.getElementById('emi-result-monthly').textContent = `₹${formatMoney(emi)}`;
  document.getElementById('emi-result-total').textContent = `₹${formatMoney(totalPayment)}`;
}

// Account Status Update via REST API
async function updateAccountStatus(accountId, newStatus) {
  try {
    const res = await fetch(`${API_BASE}/accounts/${accountId}/status`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status: newStatus })
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to update status');

    showToast(`Account ${data.accountNumber} status updated to ${newStatus}`, 'success');
    refreshAllData();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// Delete Customer Action
async function executeDeleteCustomer() {
  const id = document.getElementById('delete-cust-id').value;
  try {
    const res = await fetch(`${API_BASE}/customers/${id}`, { method: 'DELETE' });
    if (!res.ok) throw new Error('Cannot delete customer with active accounts.');

    showToast('Customer record deleted successfully', 'success');
    closeModal('modal-delete-customer');
    refreshAllData();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// Open Account For Customer Shortcut
function openAccountForCustomer(customerId) {
  openModal('modal-account');
  const custSelect = document.getElementById('acc-customer-id');
  if (custSelect) custSelect.value = customerId;
}

// Modal Helpers
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

// Utilities
function copyToClipboard(text) {
  navigator.clipboard.writeText(text).then(() => {
    showToast(`Copied "${text}" to clipboard`, 'success');
  });
}

function getStatusBadge(status) {
  if (status === 'ACTIVE') return `<span class="badge badge-active"><i class="fa-solid fa-circle text-[8px]"></i> ACTIVE</span>`;
  if (status === 'BLOCKED') return `<span class="badge badge-blocked"><i class="fa-solid fa-lock text-[8px]"></i> BLOCKED</span>`;
  return `<span class="badge badge-closed"><i class="fa-solid fa-circle-xmark text-[8px]"></i> CLOSED</span>`;
}

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
