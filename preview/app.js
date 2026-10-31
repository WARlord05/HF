/**
 * Pocket Ledger — Interactive Application Logic
 * Adheres strictly to DESIGN.md specification
 */

// Category Colors Palette
const CATEGORY_COLORS = {
  Food: '#C98B5A',
  Shopping: '#8A7BA8',
  Travel: '#5E8FA8',
  Utility: '#B39B4D',
  Subscriptions: '#2F5D50',
  Other: '#8C867B',
  Health: '#4E8775',
  Entertainment: '#7A6B8A'
};

// Initial Sample Data (India / INR focused ledger)
let transactions = [
  {
    id: 1,
    merchantName: 'Swiggy',
    amount: 349.00,
    currency: 'INR',
    date: '2026-10-31',
    time: '1:15 PM',
    category: 'Food',
    accountSource: 'HDFC Card ••4091',
    transactionType: 'DEBIT',
    isAutopay: false,
    rawNotificationText: 'HDFC Bank: Rs 349.00 spent on your card xx4091 at SWIGGY on 31-OCT-26 13:15:22. Avl Lmt: Rs 42,910.00'
  },
  {
    id: 2,
    merchantName: 'Uber',
    amount: 220.00,
    currency: 'INR',
    date: '2026-10-31',
    time: '11:30 AM',
    category: 'Travel',
    accountSource: 'UPI ••oksbi',
    transactionType: 'DEBIT',
    isAutopay: false,
    rawNotificationText: 'UPI-Debit: Rs 220.00 debited from A/c xx7812 to Uber Technologies on 31-10-26 11:30:10 Ref 63019281'
  },
  {
    id: 3,
    merchantName: 'Blinkit',
    amount: 682.00,
    currency: 'INR',
    date: '2026-10-30',
    time: '7:45 PM',
    category: 'Food',
    accountSource: 'HDFC Card ••4091',
    transactionType: 'DEBIT',
    isAutopay: false,
    rawNotificationText: 'HDFC Bank: Rs 682.00 spent on your card xx4091 at BLINKIT on 30-OCT-26 19:45:00. Avl Lmt: Rs 43,130.00'
  },
  {
    id: 4,
    merchantName: 'Netflix',
    amount: 649.00,
    currency: 'INR',
    date: '2026-10-30',
    time: '4:00 AM',
    category: 'Subscriptions',
    accountSource: 'ICICI Bank ••3309',
    transactionType: 'DEBIT',
    isAutopay: true,
    autopayId: 101,
    rawNotificationText: 'ICICI Bank: Auto-debit of INR 649.00 processed for Netflix Mandate SI-92811 on 30-Oct-26.'
  },
  {
    id: 5,
    merchantName: 'Salary Credit',
    amount: 60000.00,
    currency: 'INR',
    date: '2026-10-28',
    time: '10:00 AM',
    category: 'Other',
    accountSource: 'HDFC Salary A/c ••9011',
    transactionType: 'CREDIT',
    isAutopay: false,
    rawNotificationText: 'HDFC Bank: Your Salary of Rs 60,000.00 has been credited to A/c xx9011 on 28-OCT-26. Clear Bal: Rs 84,210.00'
  },
  {
    id: 6,
    merchantName: 'Zara',
    amount: 3490.00,
    currency: 'INR',
    date: '2026-10-28',
    time: '5:20 PM',
    category: 'Shopping',
    accountSource: 'HDFC Card ••4091',
    transactionType: 'DEBIT',
    isAutopay: false,
    rawNotificationText: 'HDFC Bank: Rs 3,490.00 spent on card xx4091 at ZARA INDIA on 28-OCT-26 17:20:12.'
  },
  {
    id: 7,
    merchantName: 'BESCOM Electricity',
    amount: 1420.00,
    currency: 'INR',
    date: '2026-10-25',
    time: '2:10 PM',
    category: 'Utility',
    accountSource: 'UPI ••okhdfc',
    transactionType: 'DEBIT',
    isAutopay: false,
    rawNotificationText: 'Paid Rs 1,420.00 to BESCOM via UPI Ref 629810481 on 25-10-26.'
  }
];

let autopays = [
  {
    id: 101,
    serviceName: 'Netflix',
    category: 'Subscriptions',
    estimatedAmount: 649.00,
    billingCycle: 'Monthly',
    lastCharged: '30 Oct 2026',
    nextExpected: '30 Nov 2026',
    status: 'ACTIVE',
    history: [
      { date: '30 Oct 2026', amount: 649.00 },
      { date: '30 Sep 2026', amount: 649.00 },
      { date: '30 Aug 2026', amount: 649.00 }
    ]
  },
  {
    id: 102,
    serviceName: 'Spotify Premium',
    category: 'Subscriptions',
    estimatedAmount: 119.00,
    billingCycle: 'Monthly',
    lastCharged: '3 Oct 2026',
    nextExpected: '3 Nov 2026', // Next 3 days -> UPCOMING
    status: 'UPCOMING',
    history: [
      { date: '3 Oct 2026', amount: 119.00 },
      { date: '3 Sep 2026', amount: 119.00 }
    ]
  },
  {
    id: 103,
    serviceName: 'ACT Broadband',
    category: 'Utility',
    estimatedAmount: 1050.00,
    billingCycle: 'Monthly',
    lastCharged: '6 Oct 2026',
    nextExpected: '6 Nov 2026', // Next 6 days -> UPCOMING
    status: 'UPCOMING',
    history: [
      { date: '6 Oct 2026', amount: 1050.00 },
      { date: '6 Sep 2026', amount: 1050.00 }
    ]
  },
  {
    id: 104,
    serviceName: 'Google One 100GB',
    category: 'Subscriptions',
    estimatedAmount: 130.00,
    billingCycle: 'Monthly',
    lastCharged: '18 Oct 2026',
    nextExpected: '18 Nov 2026',
    status: 'ACTIVE',
    history: [
      { date: '18 Oct 2026', amount: 130.00 },
      { date: '18 Sep 2026', amount: 130.00 }
    ]
  },
  {
    id: 105,
    serviceName: 'Cultpass Gym',
    category: 'Health',
    estimatedAmount: 1500.00,
    billingCycle: 'Monthly',
    lastCharged: '22 Oct 2026',
    nextExpected: '22 Nov 2026',
    status: 'ACTIVE',
    history: [
      { date: '22 Oct 2026', amount: 1500.00 }
    ]
  },
  {
    id: 106,
    serviceName: 'Disney+ Hotstar',
    category: 'Subscriptions',
    estimatedAmount: 1499.00,
    billingCycle: 'Yearly',
    lastCharged: '12 Dec 2025',
    nextExpected: '12 Dec 2026',
    status: 'CANCELLED',
    history: [
      { date: '12 Dec 2025', amount: 1499.00 }
    ]
  }
];

// App State
let activeTab = 'transactions';
let selectedCategory = 'ALL';
let selectedRange = 'this_month';
let searchQuery = '';
let currentViewingTransaction = null;
let editingTransactionId = null;

// Currency Formatter
function formatMoney(amount) {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 2,
    minimumFractionDigits: amount % 1 === 0 ? 0 : 2
  }).format(amount);
}

// Monogram Initials extractor
function getMonogramText(name, category) {
  if (name && name.trim().length > 0) {
    const parts = name.trim().split(/\s+/);
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    const clean = name.replace(/[^a-zA-Z0-9]/g, '');
    if (clean.length >= 2) return clean.slice(0, 2).toUpperCase();
    if (clean.length === 1) return clean.toUpperCase();
  }
  return category.slice(0, 2).toUpperCase();
}

function getCategoryColor(category) {
  return CATEGORY_COLORS[category] || CATEGORY_COLORS.Other;
}

// Group transactions by date
function groupTransactionsByDate(list) {
  const groups = {};
  list.forEach(tx => {
    if (!groups[tx.date]) {
      groups[tx.date] = [];
    }
    groups[tx.date].push(tx);
  });
  return Object.keys(groups).sort((a, b) => b.localeCompare(a)).map(dateKey => ({
    date: dateKey,
    items: groups[dateKey]
  }));
}

function formatDateHeader(dateStr) {
  const today = '2026-10-31';
  const yesterday = '2026-10-30';
  if (dateStr === today) return 'TODAY';
  if (dateStr === yesterday) return 'YESTERDAY';
  const d = new Date(dateStr + 'T00:00:00');
  const day = d.getDate();
  const month = d.toLocaleString('en-US', { month: 'short' }).toUpperCase();
  const weekday = d.toLocaleString('en-US', { weekday: 'short' }).toUpperCase();
  return `${day} ${month}, ${weekday}`;
}

// Render Transactions Timeline
function renderTransactions() {
  const container = document.getElementById('transactionsTimeline');
  if (!container) return;

  // Filter list
  let filtered = transactions.filter(tx => {
    const matchesCat = selectedCategory === 'ALL' || tx.category.toLowerCase() === selectedCategory.toLowerCase();
    const query = searchQuery.trim().toLowerCase();
    const matchesQuery = !query ||
      tx.merchantName.toLowerCase().includes(query) ||
      tx.category.toLowerCase().includes(query) ||
      tx.accountSource.toLowerCase().includes(query);
    return matchesCat && matchesQuery;
  });

  // Calculate totals
  const totalSpent = filtered
    .filter(t => t.transactionType === 'DEBIT')
    .reduce((sum, t) => sum + t.amount, 0);
  const totalReceived = filtered
    .filter(t => t.transactionType === 'CREDIT')
    .reduce((sum, t) => sum + t.amount, 0);

  document.getElementById('monthStatsLabel').textContent =
    `Spent ${formatMoney(totalSpent)} · Received ${formatMoney(totalReceived)}`;

  if (filtered.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <div class="empty-title">Nothing here yet</div>
        <div class="empty-caption">New payments appear as they arrive from bank notifications.</div>
        <button class="empty-action-btn" id="emptyAddBtn">+ Add cash entry</button>
      </div>
    `;
    const emptyAdd = document.getElementById('emptyAddBtn');
    if (emptyAdd) emptyAdd.onclick = openAddEntrySheet;
    return;
  }

  const grouped = groupTransactionsByDate(filtered);
  let html = '';

  grouped.forEach(group => {
    const dayDebits = group.items
      .filter(t => t.transactionType === 'DEBIT')
      .reduce((sum, t) => sum + t.amount, 0);

    html += `
      <div class="day-group">
        <div class="day-sticky-header">
          <span class="section-label">${formatDateHeader(group.date)}</span>
          ${dayDebits > 0 ? `<span class="day-total tabular-num">${formatMoney(dayDebits)}</span>` : ''}
        </div>
    `;

    group.items.forEach((tx, idx) => {
      const isCredit = tx.transactionType === 'CREDIT';
      const sign = isCredit ? '+' : '−'; // True Unicode minus
      const amountClass = isCredit ? 'credit' : 'debit';
      const catColor = getCategoryColor(tx.category);
      const initials = getMonogramText(tx.merchantName, tx.category);

      html += `
        <div class="transaction-row" data-id="${tx.id}">
          <div class="monogram-circle" style="background-color: ${catColor}24; color: ${catColor};">
            ${initials}
          </div>
          <div class="row-middle">
            <div class="row-merchant">${tx.merchantName}</div>
            <div class="row-caption">
              <span>${tx.category} · ${tx.accountSource}</span>
              ${tx.isAutopay ? '<span class="repeat-glyph" title="Autopay Mandate">↻</span>' : ''}
            </div>
          </div>
          <div class="row-amount ${amountClass} tabular-num">
            ${sign}${formatMoney(tx.amount)}
          </div>
        </div>
        ${idx < group.items.length - 1 ? '<div class="hairline row-hairline-indent"></div>' : ''}
      `;
    });

    html += `</div>`;
  });

  container.innerHTML = html;

  // Bind click on transaction rows
  container.querySelectorAll('.transaction-row').forEach(row => {
    row.addEventListener('click', () => {
      const id = parseInt(row.dataset.id, 10);
      const tx = transactions.find(t => t.id === id);
      if (tx) openDetailSheet(tx);
    });
  });
}

// Render Autopays View
function renderAutopays() {
  const monthlyTotal = autopays
    .filter(a => a.status === 'ACTIVE' || a.status === 'UPCOMING')
    .reduce((sum, a) => sum + (a.billingCycle === 'Yearly' ? a.estimatedAmount / 12 : a.estimatedAmount), 0);

  const upcomingList = autopays.filter(a => a.status === 'UPCOMING');
  const activeList = autopays.filter(a => a.status === 'ACTIVE');
  const cancelledList = autopays.filter(a => a.status === 'CANCELLED' || a.status === 'STALE');

  const upcomingTotal = upcomingList.reduce((sum, a) => sum + a.estimatedAmount, 0);

  document.getElementById('autopayMonthlyTotal').textContent = formatMoney(monthlyTotal);
  document.getElementById('autopayUpcomingStat').textContent = `Upcoming ${upcomingList.length} debits · ${formatMoney(upcomingTotal)}`;
  document.getElementById('autopayActiveStat').textContent = `Active ${activeList.length + upcomingList.length}`;

  function buildListHtml(items) {
    if (items.length === 0) {
      return '<div style="padding: 12px 20px; font-size: 13px; color: var(--on-surface-muted);">None in this category.</div>';
    }
    return items.map((item, idx) => {
      const catColor = getCategoryColor(item.category);
      const initials = getMonogramText(item.serviceName, item.category);
      const badgeClass = item.status.toLowerCase();

      return `
        <div class="autopay-item" data-id="${item.id}">
          <div class="autopay-row-top">
            <div class="monogram-circle" style="background-color: ${catColor}24; color: ${catColor};">
              ${initials}
            </div>
            <div class="row-middle">
              <div class="row-merchant">${item.serviceName}</div>
              <div class="row-caption">${item.billingCycle} · last ${formatMoney(item.estimatedAmount)} on ${item.lastCharged}</div>
            </div>
            <div class="autopay-meta-col">
              <span class="status-badge ${badgeClass}">${item.status.charAt(0) + item.status.slice(1).toLowerCase()}</span>
              <span class="autopay-next-date">Next ${item.nextExpected}</span>
            </div>
          </div>

          <!-- History disclosure -->
          <div class="autopay-history-panel" id="history-${item.id}">
            <div class="hairline" style="margin: 8px 0 6px;"></div>
            <div class="section-label" style="margin-bottom: 4px;">DEBIT HISTORY</div>
            ${item.history.map(h => `
              <div class="history-item">
                <span>${h.date}</span>
                <span class="tabular-num">−${formatMoney(h.amount)}</span>
              </div>
            `).join('')}
            <div class="history-actions">
              ${item.status === 'CANCELLED'
                ? `<button class="action-btn-link reactivate-btn" data-id="${item.id}">Reactivate mandate</button>`
                : `<button class="action-btn-link danger cancel-btn" data-id="${item.id}">Mark cancelled</button>`
              }
            </div>
          </div>
        </div>
        ${idx < items.length - 1 ? '<div class="hairline" style="margin: 0 20px;"></div>' : ''}
      `;
    }).join('');
  }

  document.getElementById('upcomingAutopaysList').innerHTML = buildListHtml(upcomingList);
  document.getElementById('activeAutopaysList').innerHTML = buildListHtml(activeList);
  document.getElementById('cancelledAutopaysList').innerHTML = buildListHtml(cancelledList);

  // Bind expandable rows & actions
  document.querySelectorAll('.autopay-item').forEach(itemEl => {
    itemEl.addEventListener('click', (e) => {
      if (e.target.closest('.action-btn-link')) return;
      const historyPanel = itemEl.querySelector('.autopay-history-panel');
      if (historyPanel) {
        historyPanel.classList.toggle('expanded');
      }
    });
  });

  // Cancel action
  document.querySelectorAll('.cancel-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const id = parseInt(btn.dataset.id, 10);
      const target = autopays.find(a => a.id === id);
      if (target) {
        target.status = 'CANCELLED';
        renderAutopays();
        showToast(`Cancelled mandate · ${target.serviceName}`);
      }
    });
  });

  // Reactivate action
  document.querySelectorAll('.reactivate-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const id = parseInt(btn.dataset.id, 10);
      const target = autopays.find(a => a.id === id);
      if (target) {
        target.status = 'ACTIVE';
        renderAutopays();
        showToast(`Reactivated mandate · ${target.serviceName}`);
      }
    });
  });
}

// Toast Notification
function showToast(message) {
  const toast = document.getElementById('toastNotification');
  if (!toast) return;
  toast.textContent = message;
  toast.style.display = 'block';
  setTimeout(() => {
    toast.style.display = 'none';
  }, 2800);
}

// Open Detail Sheet
function openDetailSheet(tx) {
  currentViewingTransaction = tx;
  const modal = document.getElementById('detailSheetModal');
  const catColor = getCategoryColor(tx.category);

  const monogram = document.getElementById('detailMonogram');
  monogram.textContent = getMonogramText(tx.merchantName, tx.category);
  monogram.style.backgroundColor = `${catColor}24`;
  monogram.style.color = catColor;

  document.getElementById('detailMerchant').textContent = tx.merchantName;
  document.getElementById('detailTimestamp').textContent = `${tx.date} · ${tx.time || '12:00 PM'}`;

  const isCredit = tx.transactionType === 'CREDIT';
  const sign = isCredit ? '+' : '−';
  const amountEl = document.getElementById('detailAmount');
  amountEl.textContent = `${sign}${formatMoney(tx.amount)}`;
  amountEl.style.color = isCredit ? 'var(--credit)' : 'var(--on-bg)';
  document.getElementById('detailTypeLabel').textContent = isCredit ? 'AMOUNT RECEIVED' : 'AMOUNT DEBITED';

  document.getElementById('detailCategory').textContent = tx.category;
  document.getElementById('detailAccount').textContent = tx.accountSource;
  document.getElementById('detailType').textContent = isCredit ? 'Credit' : 'Debit';

  const autopayRow = document.getElementById('detailAutopayRow');
  if (tx.isAutopay) {
    autopayRow.classList.remove('hidden');
  } else {
    autopayRow.classList.add('hidden');
  }

  // Notification source
  const sourceBlock = document.getElementById('detailSourceBlock');
  const rawText = document.getElementById('rawSourceText');
  const rawBox = document.getElementById('rawSourceContent');
  const toggleBtn = document.getElementById('btnDisclosureToggle');

  if (tx.rawNotificationText) {
    sourceBlock.classList.remove('hidden');
    rawText.textContent = tx.rawNotificationText;
    rawBox.classList.add('hidden');
    toggleBtn.textContent = 'Show';
  } else {
    sourceBlock.classList.add('hidden');
  }

  modal.classList.add('open');
}

// Open Add / Edit Entry Sheet
function openAddEntrySheet(editTx = null) {
  const modal = document.getElementById('entrySheetModal');
  const title = document.getElementById('sheetTitle');
  const deleteBtn = document.getElementById('btnDeleteEntry');
  const amountInput = document.getElementById('inputAmount');
  const merchantInput = document.getElementById('inputMerchant');
  const accountInput = document.getElementById('inputAccount');

  if (editTx && editTx.id) {
    editingTransactionId = editTx.id;
    title.textContent = 'Edit entry';
    amountInput.value = editTx.amount;
    merchantInput.value = editTx.merchantName;
    accountInput.value = editTx.accountSource;
    deleteBtn.classList.remove('hidden');
    setEntryType(editTx.transactionType || 'DEBIT');
    setCategoryChip(editTx.category || 'Food');
  } else {
    editingTransactionId = null;
    title.textContent = 'New entry';
    amountInput.value = '';
    merchantInput.value = '';
    accountInput.value = 'Cash';
    deleteBtn.classList.add('hidden');
    setEntryType('DEBIT');
    setCategoryChip('Food');
  }

  modal.classList.add('open');
  setTimeout(() => amountInput.focus(), 100);
}

function setEntryType(type) {
  const btnDebit = document.getElementById('btnTypeDebit');
  const btnCredit = document.getElementById('btnTypeCredit');
  if (type === 'CREDIT') {
    btnCredit.classList.add('active');
    btnDebit.classList.remove('active');
  } else {
    btnDebit.classList.add('active');
    btnCredit.classList.remove('active');
  }
}

function setCategoryChip(category) {
  document.querySelectorAll('#sheetCategoryPicker .cat-chip').forEach(btn => {
    if (btn.dataset.category.toLowerCase() === category.toLowerCase()) {
      btn.classList.add('active');
    } else {
      btn.classList.remove('active');
    }
  });
}

// Simulate Incoming Bank SMS Notification (Section 7 in DESIGN.md)
function simulateBankNotification() {
  const randomMocks = [
    {
      merchant: 'Zomato',
      amount: 412.00,
      category: 'Food',
      source: 'HDFC Card ••4091',
      raw: 'HDFC Bank: Rs 412.00 spent on card xx4091 at ZOMATO on 31-OCT-26 13:25:10.'
    },
    {
      merchant: 'Starbucks Coffee',
      amount: 380.00,
      category: 'Food',
      source: 'UPI ••oksbi',
      raw: 'UPI-Debit: Rs 380.00 debited from A/c xx7812 to Starbucks Coffee Ref 98124018.'
    },
    {
      merchant: 'Metro Smart Card',
      amount: 200.00,
      category: 'Travel',
      source: 'UPI ••okhdfc',
      raw: 'Paid Rs 200.00 to BMRCL Metro Recharge via UPI Ref 41092817 on 31-10-26.'
    }
  ];

  const pick = randomMocks[Math.floor(Math.random() * randomMocks.length)];
  const newTx = {
    id: Date.now(),
    merchantName: pick.merchant,
    amount: pick.amount,
    currency: 'INR',
    date: '2026-10-31',
    time: 'Just now',
    category: pick.category,
    accountSource: pick.source,
    transactionType: 'DEBIT',
    isAutopay: false,
    rawNotificationText: pick.raw
  };

  transactions.unshift(newTx);
  renderTransactions();

  // Find newly inserted row and apply slide / highlight
  const firstRow = document.querySelector('.transaction-row');
  if (firstRow) {
    firstRow.classList.add('newly-arrived');
  }

  showToast(`Added ${formatMoney(pick.amount)} · ${pick.merchant}`);
}

// Event Listeners Initialization
document.addEventListener('DOMContentLoaded', () => {
  // Navigation Tabs: Transactions | Autopays
  const tabTransactions = document.getElementById('tabTransactions');
  const tabAutopays = document.getElementById('tabAutopays');
  const viewTransactions = document.getElementById('viewTransactions');
  const viewAutopays = document.getElementById('viewAutopays');

  tabTransactions.addEventListener('click', () => {
    activeTab = 'transactions';
    tabTransactions.classList.add('active');
    tabAutopays.classList.remove('active');
    viewTransactions.classList.add('active');
    viewAutopays.classList.remove('active');
    renderTransactions();
  });

  tabAutopays.addEventListener('click', () => {
    activeTab = 'autopays';
    tabAutopays.classList.add('active');
    tabTransactions.classList.remove('active');
    viewAutopays.classList.add('active');
    viewTransactions.classList.remove('active');
    renderAutopays();
  });

  // Theme Toggle: Paper (Light) vs Ink (Dark)
  const themeToggleBtn = document.getElementById('themeToggleBtn');
  const themeLabel = document.getElementById('themeLabel');
  themeToggleBtn.addEventListener('click', () => {
    const current = document.body.getAttribute('data-theme');
    const next = current === 'ink' ? 'paper' : 'ink';
    document.body.setAttribute('data-theme', next);
    themeLabel.textContent = next === 'ink' ? 'Paper Theme' : 'Ink Theme';
  });

  // Simulate SMS button
  document.getElementById('simulateSmsBtn').addEventListener('click', simulateBankNotification);

  // Search Input
  const searchInput = document.getElementById('searchInput');
  searchInput.addEventListener('input', (e) => {
    searchQuery = e.target.value;
    renderTransactions();
  });

  // Date Range Filter Chips
  document.querySelectorAll('#dateFilterRow .chip').forEach(chip => {
    chip.addEventListener('click', () => {
      document.querySelectorAll('#dateFilterRow .chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      selectedRange = chip.dataset.range;
      renderTransactions();
    });
  });

  // Category Filter Chips
  document.querySelectorAll('#categoryFilterRow .chip').forEach(chip => {
    chip.addEventListener('click', () => {
      document.querySelectorAll('#categoryFilterRow .chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      selectedCategory = chip.dataset.category;
      renderTransactions();
    });
  });

  // Add Entry Button in Header
  document.getElementById('openAddEntryBtn').addEventListener('click', () => {
    openAddEntrySheet(null);
  });

  // Modals Close buttons & Backdrops
  document.getElementById('closeEntrySheetBtn').addEventListener('click', () => {
    document.getElementById('entrySheetModal').classList.remove('open');
  });

  document.getElementById('closeDetailSheetBtn').addEventListener('click', () => {
    document.getElementById('detailSheetModal').classList.remove('open');
  });

  document.getElementById('entrySheetModal').addEventListener('click', (e) => {
    if (e.target === e.currentTarget) {
      e.currentTarget.classList.remove('open');
    }
  });

  document.getElementById('detailSheetModal').addEventListener('click', (e) => {
    if (e.target === e.currentTarget) {
      e.currentTarget.classList.remove('open');
    }
  });

  // Type Toggle in Entry Sheet
  document.getElementById('btnTypeDebit').addEventListener('click', () => setEntryType('DEBIT'));
  document.getElementById('btnTypeCredit').addEventListener('click', () => setEntryType('CREDIT'));

  // Category selection in Entry Sheet
  document.querySelectorAll('#sheetCategoryPicker .cat-chip').forEach(btn => {
    btn.addEventListener('click', () => {
      setCategoryChip(btn.dataset.category);
    });
  });

  // Save Entry Form
  document.getElementById('entryForm').addEventListener('submit', (e) => {
    e.preventDefault();
    const amountVal = parseFloat(document.getElementById('inputAmount').value);
    const merchantVal = document.getElementById('inputMerchant').value.trim();
    const accountVal = document.getElementById('inputAccount').value.trim() || 'Cash';
    const isCredit = document.getElementById('btnTypeCredit').classList.contains('active');
    const selectedCatBtn = document.querySelector('#sheetCategoryPicker .cat-chip.active');
    const categoryVal = selectedCatBtn ? selectedCatBtn.dataset.category : 'Food';

    if (!merchantVal || isNaN(amountVal) || amountVal <= 0) return;

    if (editingTransactionId) {
      const idx = transactions.findIndex(t => t.id === editingTransactionId);
      if (idx !== -1) {
        transactions[idx] = {
          ...transactions[idx],
          merchantName: merchantVal,
          amount: amountVal,
          category: categoryVal,
          accountSource: accountVal,
          transactionType: isCredit ? 'CREDIT' : 'DEBIT'
        };
        showToast(`Updated · ${merchantVal}`);
      }
    } else {
      const newTx = {
        id: Date.now(),
        merchantName: merchantVal,
        amount: amountVal,
        currency: 'INR',
        date: '2026-10-31',
        time: 'Just now',
        category: categoryVal,
        accountSource: accountVal,
        transactionType: isCredit ? 'CREDIT' : 'DEBIT',
        isAutopay: false,
        rawNotificationText: `Manual entry added on 31-Oct-2026`
      };
      transactions.unshift(newTx);
      showToast(`Added ${formatMoney(amountVal)} · ${merchantVal}`);
    }

    document.getElementById('entrySheetModal').classList.remove('open');
    renderTransactions();
  });

  // Delete from Entry Sheet
  document.getElementById('btnDeleteEntry').addEventListener('click', () => {
    if (editingTransactionId) {
      transactions = transactions.filter(t => t.id !== editingTransactionId);
      document.getElementById('entrySheetModal').classList.remove('open');
      renderTransactions();
      showToast('Entry deleted');
    }
  });

  // Detail Sheet Actions
  document.getElementById('btnEditFromDetail').addEventListener('click', () => {
    const tx = currentViewingTransaction;
    document.getElementById('detailSheetModal').classList.remove('open');
    if (tx) {
      openAddEntrySheet(tx);
    }
  });

  document.getElementById('btnDeleteFromDetail').addEventListener('click', () => {
    const tx = currentViewingTransaction;
    if (tx) {
      transactions = transactions.filter(t => t.id !== tx.id);
      document.getElementById('detailSheetModal').classList.remove('open');
      renderTransactions();
      showToast(`Deleted ${tx.merchantName}`);
    }
  });

  // Raw Notification Disclosure Toggle
  const toggleDisclosureBtn = document.getElementById('btnDisclosureToggle');
  const toggleHeader = document.getElementById('toggleSourceDisclosure');
  const rawSourceContent = document.getElementById('rawSourceContent');

  function toggleDisclosure() {
    const isHidden = rawSourceContent.classList.contains('hidden');
    if (isHidden) {
      rawSourceContent.classList.remove('hidden');
      toggleDisclosureBtn.textContent = 'Hide';
    } else {
      rawSourceContent.classList.add('hidden');
      toggleDisclosureBtn.textContent = 'Show';
    }
  }

  toggleHeader.addEventListener('click', toggleDisclosure);

  // Notification Banner dismiss
  document.getElementById('enableNotificationBtn').addEventListener('click', () => {
    document.getElementById('notificationBanner').style.display = 'none';
    showToast('Notification listener enabled');
  });

  // Initial render
  renderTransactions();
  renderAutopays();
});
