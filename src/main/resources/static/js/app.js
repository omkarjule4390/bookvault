/**
 * BookVault - Library Management System Frontend Client
 * Communicates with Spring Boot 3 REST APIs
 * Includes seamless Interactive Demo Mode when running offline or standalone.
 */

const API_BASE = '/api/v1';

// Global Application State
let currentTab = 'dashboard-tab';
let currentLoanFilter = 'ALL';
let searchDebounceTimeout = null;
let isApiOnline = false;

// Seed data for standalone demo mode
const SEED_DATA = {
    books: [
        { id: 1, title: 'Clean Code: A Handbook of Agile Software Craftsmanship', author: 'Robert C. Martin', isbn: '9780132350884', category: 'Software Engineering', publicationYear: 2008, totalCopies: 5, availableCopies: 4 },
        { id: 2, title: 'Design Patterns: Elements of Reusable Object-Oriented Software', author: 'Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides', isbn: '9780201633610', category: 'Architecture', publicationYear: 1994, totalCopies: 3, availableCopies: 2 },
        { id: 3, title: 'The Pragmatic Programmer: Your Journey To Mastery', author: 'David Thomas, Andrew Hunt', isbn: '9780135957059', category: 'Software Engineering', publicationYear: 2019, totalCopies: 4, availableCopies: 4 },
        { id: 4, title: 'Domain-Driven Design: Tackling Complexity in the Heart of Software', author: 'Eric Evans', isbn: '9780321125217', category: 'Architecture', publicationYear: 2003, totalCopies: 2, availableCopies: 1 }
    ],
    members: [
        { id: 1, name: 'Alice Johnson', email: 'alice.johnson@example.com', phone: '+1-555-0199', membershipDate: '2026-01-15', status: 'ACTIVE' },
        { id: 2, name: 'Bob Smith', email: 'bob.smith@example.com', phone: '+1-555-0248', membershipDate: '2026-02-10', status: 'ACTIVE' },
        { id: 3, name: 'Charlie Davis', email: 'charlie.davis@example.com', phone: '+1-555-0371', membershipDate: '2026-03-01', status: 'ACTIVE' }
    ],
    loans: [
        { id: 1, bookId: 1, bookTitle: 'Clean Code: A Handbook of Agile Software Craftsmanship', bookIsbn: '9780132350884', memberId: 1, memberName: 'Alice Johnson', memberEmail: 'alice.johnson@example.com', borrowDate: '2026-09-01', dueDate: '2026-09-15', returnDate: null, status: 'BORROWED', fineAmount: 0.00, remarks: 'Quarterly review project' },
        { id: 2, bookId: 2, bookTitle: 'Design Patterns: Elements of Reusable Object-Oriented Software', bookIsbn: '9780201633610', memberId: 2, memberName: 'Bob Smith', memberEmail: 'bob.smith@example.com', borrowDate: '2026-08-15', dueDate: '2026-08-29', returnDate: null, status: 'BORROWED', fineAmount: 15.00, remarks: 'Semester thesis' },
        { id: 3, bookId: 4, bookTitle: 'Domain-Driven Design', bookIsbn: '9780321125217', memberId: 3, memberName: 'Charlie Davis', memberEmail: 'charlie.davis@example.com', borrowDate: '2026-08-01', dueDate: '2026-08-15', returnDate: '2026-08-14', status: 'RETURNED', fineAmount: 0.00, remarks: 'Returned early' }
    ]
};

// Initialize LocalStorage if not set
function initDemoStore() {
    if (!localStorage.getItem('bookvault_books')) {
        localStorage.setItem('bookvault_books', JSON.stringify(SEED_DATA.books));
    }
    if (!localStorage.getItem('bookvault_members')) {
        localStorage.setItem('bookvault_members', JSON.stringify(SEED_DATA.members));
    }
    if (!localStorage.getItem('bookvault_loans')) {
        localStorage.setItem('bookvault_loans', JSON.stringify(SEED_DATA.loans));
    }
}

function getDemoStore(key) {
    initDemoStore();
    return JSON.parse(localStorage.getItem(`bookvault_${key}`) || '[]');
}

function saveDemoStore(key, data) {
    localStorage.setItem(`bookvault_${key}`, JSON.stringify(data));
}

// Initialization on DOM load
document.addEventListener('DOMContentLoaded', async () => {
    initDemoStore();
    setupNavigation();
    await checkApiHealth();
    loadDashboardData();
    loadBooks();
    loadMembers();
    loadLoans();
    loadOverdueLoans();
});

// ==============================================================================
// Navigation & Tabs
// ==============================================================================
function setupNavigation() {
    const navButtons = document.querySelectorAll('.nav-btn');
    navButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const targetTab = btn.getAttribute('data-tab');
            switchTab(targetTab);
        });
    });
}

function switchTab(tabId) {
    currentTab = tabId;

    document.querySelectorAll('.nav-btn').forEach(btn => {
        if (btn.getAttribute('data-tab') === tabId) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });

    document.querySelectorAll('.tab-content').forEach(tab => {
        if (tab.id === tabId) {
            tab.classList.add('active');
        } else {
            tab.classList.remove('active');
        }
    });

    if (tabId === 'dashboard-tab') loadDashboardData();
    if (tabId === 'books-tab') loadBooks();
    if (tabId === 'members-tab') loadMembers();
    if (tabId === 'loans-tab') loadLoans();
    if (tabId === 'overdue-tab') loadOverdueLoans();
}

// ==============================================================================
// API Health Check
// ==============================================================================
async function checkApiHealth() {
    const badge = document.getElementById('apiStatusBadge');
    const badgeText = document.getElementById('apiStatusText');

    if (window.location.protocol === 'file:') {
        isApiOnline = false;
        badgeText.textContent = 'Live Demo Mode';
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/books?page=0&size=1`, { signal: AbortSignal.timeout(2000) });
        if (res.ok) {
            isApiOnline = true;
            badgeText.textContent = 'PostgreSQL Connected';
        } else {
            throw new Error();
        }
    } catch (err) {
        isApiOnline = false;
        badgeText.textContent = 'Live Demo Mode';
    }
}

// ==============================================================================
// Dashboard & Stats
// ==============================================================================
async function loadDashboardData() {
    try {
        let books = [], members = [], loans = [];

        if (isApiOnline) {
            const [bRes, mRes, lRes] = await Promise.all([
                fetch(`${API_BASE}/books?page=0&size=100`),
                fetch(`${API_BASE}/members?page=0&size=100`),
                fetch(`${API_BASE}/borrow?page=0&size=100`)
            ]);
            if (bRes.ok) books = (await bRes.json()).content || [];
            if (mRes.ok) members = (await mRes.json()).content || [];
            if (lRes.ok) loans = (await lRes.json()).content || [];
        } else {
            books = getDemoStore('books');
            members = getDemoStore('members');
            loans = getDemoStore('loans');
        }

        document.getElementById('statTotalBooks').textContent = books.length;
        let totalCopies = 0, availableCopies = 0;
        books.forEach(b => {
            totalCopies += (b.totalCopies || 0);
            availableCopies += (b.availableCopies || 0);
        });
        document.getElementById('statTotalCopies').textContent = `${totalCopies} copies in stock`;
        document.getElementById('statAvailableCopies').textContent = availableCopies;

        document.getElementById('statTotalMembers').textContent = members.length;

        const activeLoans = loans.filter(l => l.status === 'BORROWED');
        document.getElementById('statActiveLoans').textContent = activeLoans.length;

        const today = new Date().toISOString().split('T')[0];
        const overdueLoans = activeLoans.filter(l => l.dueDate < today);
        document.getElementById('statOverdueCount').textContent = overdueLoans.length;

        let totalFines = 0;
        overdueLoans.forEach(o => {
            const days = Math.max(1, Math.ceil((new Date() - new Date(o.dueDate)) / (1000 * 60 * 60 * 24)));
            totalFines += days * 1.00;
        });
        document.getElementById('statOverdueFine').textContent = `$${totalFines.toFixed(2)} estimated fine`;

        renderRecentBooks(books.slice(0, 5));
        renderRecentLoans(loans.slice(0, 5));
    } catch (err) {
        console.error('Error loading dashboard:', err);
    }
}

function renderRecentBooks(books) {
    const container = document.getElementById('dashboardRecentBooks');
    if (!books || books.length === 0) {
        container.innerHTML = '<div class="empty-state">No books in catalog yet. Click "+ Add New Book" above.</div>';
        return;
    }

    container.innerHTML = books.map(book => `
        <div class="compact-item">
            <div>
                <strong>${escapeHtml(book.title)}</strong>
                <div style="font-size: 12px; color: var(--text-muted);">${escapeHtml(book.author)} • ISBN: ${escapeHtml(book.isbn)}</div>
            </div>
            <span class="badge ${book.availableCopies > 0 ? 'badge-active' : 'badge-suspended'}">
                ${book.availableCopies} / ${book.totalCopies} Avail
            </span>
        </div>
    `).join('');
}

function renderRecentLoans(loans) {
    const container = document.getElementById('dashboardRecentLoans');
    if (!loans || loans.length === 0) {
        container.innerHTML = '<div class="empty-state">No borrow transactions yet.</div>';
        return;
    }

    container.innerHTML = loans.map(loan => `
        <div class="compact-item">
            <div>
                <strong>${escapeHtml(loan.bookTitle)}</strong>
                <div style="font-size: 12px; color: var(--text-muted);">Borrowed by: ${escapeHtml(loan.memberName)}</div>
            </div>
            <span class="badge badge-${loan.status.toLowerCase()}">${loan.status}</span>
        </div>
    `).join('');
}

// ==============================================================================
// Books Management
// ==============================================================================
async function loadBooks() {
    const grid = document.getElementById('booksGrid');
    grid.innerHTML = '<div class="loading-state"><i class="fa-solid fa-spinner fa-spin"></i> Loading books inventory...</div>';

    let books = [];
    if (isApiOnline) {
        try {
            const res = await fetch(`${API_BASE}/books?page=0&size=50&sortBy=id&direction=desc`);
            if (res.ok) {
                const data = await res.json();
                books = data.content || [];
            }
        } catch (err) {
            books = getDemoStore('books');
        }
    } else {
        books = getDemoStore('books');
    }

    if (books.length === 0) {
        grid.innerHTML = `
            <div class="empty-state">
                <i class="fa-solid fa-book-open" style="font-size: 32px; color: var(--text-muted);"></i>
                <p>No books found in the catalog.</p>
                <button class="btn btn-primary btn-sm" onclick="openModal('addBookModal')">+ Add First Book</button>
            </div>
        `;
        return;
    }

    grid.innerHTML = books.map(book => {
        const availPct = book.totalCopies > 0 ? Math.round((book.availableCopies / book.totalCopies) * 100) : 0;
        return `
            <div class="book-card" id="book-card-${book.id}">
                <div class="book-card-top">
                    <span class="category-tag">${escapeHtml(book.category || 'General')}</span>
                    <span class="book-year">${book.publicationYear ? book.publicationYear : ''}</span>
                </div>
                <div>
                    <h4 class="book-title">${escapeHtml(book.title)}</h4>
                    <div class="book-author"><i class="fa-solid fa-feather-pointed"></i> ${escapeHtml(book.author)}</div>
                    <div class="book-meta">
                        <div class="meta-item">
                            <span class="meta-label">ISBN</span>
                            <span class="meta-val" style="font-family: monospace;">${escapeHtml(book.isbn)}</span>
                        </div>
                        <div class="meta-item">
                            <span class="meta-label">Availability</span>
                            <span class="meta-val">${book.availableCopies} of ${book.totalCopies} in stock</span>
                        </div>
                        <div class="stock-bar">
                            <div class="stock-fill" style="width: ${availPct}%;"></div>
                        </div>
                    </div>
                </div>
                <div class="book-card-actions">
                    ${book.availableCopies > 0 ? `
                        <button class="btn btn-accent btn-sm" onclick="quickBorrow(${book.id}, '${escapeHtml(book.title)}')">
                            <i class="fa-solid fa-arrow-right-arrow-left"></i> Issue
                        </button>
                    ` : `
                        <button class="btn btn-secondary btn-sm" disabled style="opacity: 0.5; cursor: not-allowed;">
                            Out of Stock
                        </button>
                    `}
                    <button class="btn btn-secondary btn-sm" onclick="editBook(${JSON.stringify(book).replace(/"/g, '&quot;')})">
                        <i class="fa-solid fa-pen-to-square"></i>
                    </button>
                    <button class="btn btn-danger btn-sm" onclick="deleteBook(${book.id}, '${escapeHtml(book.title)}')">
                        <i class="fa-solid fa-trash"></i>
                    </button>
                </div>
            </div>
        `;
    }).join('');
}

function handleBookSearch(query) {
    const clearBtn = document.getElementById('clearBookSearch');
    clearBtn.style.display = query.trim().length > 0 ? 'block' : 'none';

    clearTimeout(searchDebounceTimeout);
    searchDebounceTimeout = setTimeout(async () => {
        if (!query.trim()) {
            loadBooks();
            return;
        }

        const grid = document.getElementById('booksGrid');
        grid.innerHTML = '<div class="loading-state"><i class="fa-solid fa-spinner fa-spin"></i> Searching books...</div>';

        let books = [];
        if (isApiOnline) {
            try {
                const res = await fetch(`${API_BASE}/books/search?query=${encodeURIComponent(query.trim())}`);
                if (res.ok) books = (await res.json()).content || [];
            } catch (err) {
                books = searchDemoBooks(query);
            }
        } else {
            books = searchDemoBooks(query);
        }

        if (books.length === 0) {
            grid.innerHTML = `<div class="empty-state">No books matching "${escapeHtml(query)}".</div>`;
            return;
        }

        grid.innerHTML = books.map(book => `
            <div class="book-card">
                <div class="book-card-top">
                    <span class="category-tag">${escapeHtml(book.category || 'General')}</span>
                    <span class="book-year">${book.publicationYear || ''}</span>
                </div>
                <h4 class="book-title">${escapeHtml(book.title)}</h4>
                <div class="book-author"><i class="fa-solid fa-feather-pointed"></i> ${escapeHtml(book.author)}</div>
                <div class="book-meta">
                    <div class="meta-item">
                        <span class="meta-label">ISBN</span>
                        <span class="meta-val">${escapeHtml(book.isbn)}</span>
                    </div>
                    <div class="meta-item">
                        <span class="meta-label">Availability</span>
                        <span class="meta-val">${book.availableCopies} of ${book.totalCopies} copies</span>
                    </div>
                </div>
                <div class="book-card-actions">
                    <button class="btn btn-secondary btn-sm" onclick="editBook(${JSON.stringify(book).replace(/"/g, '&quot;')})">Edit</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteBook(${book.id}, '${escapeHtml(book.title)}')">Delete</button>
                </div>
            </div>
        `).join('');
    }, 250);
}

function searchDemoBooks(query) {
    const q = query.toLowerCase();
    return getDemoStore('books').filter(b =>
        b.title.toLowerCase().includes(q) ||
        b.author.toLowerCase().includes(q) ||
        (b.category && b.category.toLowerCase().includes(q))
    );
}

function clearBookSearch() {
    const input = document.getElementById('bookSearchInput');
    input.value = '';
    document.getElementById('clearBookSearch').style.display = 'none';
    loadBooks();
}

async function handleBookSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('bookId').value;
    const saveBtn = document.getElementById('saveBookBtn');
    saveBtn.disabled = true;
    saveBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Saving...';

    const payload = {
        title: document.getElementById('bookTitle').value.trim(),
        author: document.getElementById('bookAuthor').value.trim(),
        isbn: document.getElementById('bookIsbn').value.trim(),
        category: document.getElementById('bookCategory').value.trim() || null,
        publicationYear: document.getElementById('bookYear').value ? parseInt(document.getElementById('bookYear').value) : null,
        totalCopies: parseInt(document.getElementById('bookCopies').value)
    };

    try {
        if (isApiOnline) {
            const url = id ? `${API_BASE}/books/${id}` : `${API_BASE}/books`;
            const res = await fetch(url, {
                method: id ? 'PUT' : 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const data = await res.json();
            if (!res.ok) throw new Error(data.message || 'Failed to save book');
        } else {
            // Local demo save
            let books = getDemoStore('books');
            if (id) {
                const idx = books.findIndex(b => b.id == id);
                if (idx !== -1) {
                    books[idx] = { ...books[idx], ...payload, availableCopies: payload.totalCopies };
                }
            } else {
                const newId = books.length > 0 ? Math.max(...books.map(b => b.id)) + 1 : 1;
                books.unshift({ id: newId, ...payload, availableCopies: payload.totalCopies });
            }
            saveDemoStore('books', books);
        }

        showToast('Success', id ? 'Book updated successfully' : 'Book added to catalog', 'success');
        closeModal('addBookModal');
        loadBooks();
        loadDashboardData();
    } catch (err) {
        showToast('Error', err.message, 'error');
    } finally {
        saveBtn.disabled = false;
        saveBtn.innerHTML = 'Save Book';
    }
}

function editBook(book) {
    document.getElementById('bookModalTitle').innerHTML = '<i class="fa-solid fa-pen-to-square"></i> Edit Book';
    document.getElementById('bookId').value = book.id;
    document.getElementById('bookTitle').value = book.title;
    document.getElementById('bookAuthor').value = book.author;
    document.getElementById('bookIsbn').value = book.isbn;
    document.getElementById('bookCategory').value = book.category || '';
    document.getElementById('bookYear').value = book.publicationYear || '';
    document.getElementById('bookCopies').value = book.totalCopies;
    openModal('addBookModal');
}

async function deleteBook(id, title) {
    if (!confirm(`Are you sure you want to delete "${title}"?`)) return;

    try {
        if (isApiOnline) {
            const res = await fetch(`${API_BASE}/books/${id}`, { method: 'DELETE' });
            if (!res.ok) {
                const data = await res.json();
                throw new Error(data.message || 'Failed to delete book');
            }
        } else {
            let books = getDemoStore('books');
            books = books.filter(b => b.id != id);
            saveDemoStore('books', books);
        }
        showToast('Deleted', `"${title}" was deleted`, 'success');
        loadBooks();
        loadDashboardData();
    } catch (err) {
        showToast('Delete Failed', err.message, 'error');
    }
}

// ==============================================================================
// Members Management
// ==============================================================================
async function loadMembers() {
    const tbody = document.getElementById('membersTableBody');
    tbody.innerHTML = '<tr><td colspan="8" class="text-center py-4"><i class="fa-solid fa-spinner fa-spin"></i> Loading members...</td></tr>';

    let members = [];
    if (isApiOnline) {
        try {
            const res = await fetch(`${API_BASE}/members?page=0&size=50&sortBy=id&direction=desc`);
            if (res.ok) members = (await res.json()).content || [];
        } catch (err) {
            members = getDemoStore('members');
        }
    } else {
        members = getDemoStore('members');
    }

    if (members.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center py-4">No members registered yet.</td></tr>';
        return;
    }

    tbody.innerHTML = members.map(m => `
        <tr>
            <td>#${m.id}</td>
            <td><strong>${escapeHtml(m.name)}</strong></td>
            <td>${escapeHtml(m.email)}</td>
            <td>${escapeHtml(m.phone || '-')}</td>
            <td>${m.membershipDate || '-'}</td>
            <td><span class="badge badge-${m.status.toLowerCase()}">${m.status}</span></td>
            <td>
                <button class="btn btn-secondary btn-sm" onclick="viewMemberLoans(${m.id}, '${escapeHtml(m.name)}')">
                    <i class="fa-solid fa-book"></i> Loans
                </button>
            </td>
            <td class="text-right">
                <button class="btn btn-secondary btn-sm" onclick="editMember(${JSON.stringify(m).replace(/"/g, '&quot;')})">
                    <i class="fa-solid fa-pen"></i>
                </button>
                <button class="btn btn-danger btn-sm" onclick="deleteMember(${m.id}, '${escapeHtml(m.name)}')">
                    <i class="fa-solid fa-trash"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

function handleMemberSearch(query) {
    clearTimeout(searchDebounceTimeout);
    searchDebounceTimeout = setTimeout(async () => {
        if (!query.trim()) {
            loadMembers();
            return;
        }

        const q = query.toLowerCase();
        let members = getDemoStore('members').filter(m =>
            m.name.toLowerCase().includes(q) || m.email.toLowerCase().includes(q)
        );

        const tbody = document.getElementById('membersTableBody');
        if (members.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" class="text-center py-4">No members matching "${escapeHtml(query)}".</td></tr>`;
            return;
        }

        tbody.innerHTML = members.map(m => `
            <tr>
                <td>#${m.id}</td>
                <td><strong>${escapeHtml(m.name)}</strong></td>
                <td>${escapeHtml(m.email)}</td>
                <td>${escapeHtml(m.phone || '-')}</td>
                <td>${m.membershipDate || '-'}</td>
                <td><span class="badge badge-${m.status.toLowerCase()}">${m.status}</span></td>
                <td>
                    <button class="btn btn-secondary btn-sm" onclick="viewMemberLoans(${m.id}, '${escapeHtml(m.name)}')">Loans</button>
                </td>
                <td class="text-right">
                    <button class="btn btn-secondary btn-sm" onclick="editMember(${JSON.stringify(m).replace(/"/g, '&quot;')})">Edit</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteMember(${m.id}, '${escapeHtml(m.name)}')">Delete</button>
                </td>
            </tr>
        `).join('');
    }, 250);
}

async function handleMemberSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('memberId').value;
    const saveBtn = document.getElementById('saveMemberBtn');
    saveBtn.disabled = true;
    saveBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Saving...';

    const payload = {
        name: document.getElementById('memberName').value.trim(),
        email: document.getElementById('memberEmail').value.trim(),
        phone: document.getElementById('memberPhone').value.trim(),
        status: document.getElementById('memberStatus').value
    };

    try {
        if (isApiOnline) {
            const url = id ? `${API_BASE}/members/${id}` : `${API_BASE}/members`;
            const res = await fetch(url, {
                method: id ? 'PUT' : 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const data = await res.json();
            if (!res.ok) throw new Error(data.message || 'Failed to save member profile');
        } else {
            let members = getDemoStore('members');
            if (id) {
                const idx = members.findIndex(m => m.id == id);
                if (idx !== -1) members[idx] = { ...members[idx], ...payload };
            } else {
                const newId = members.length > 0 ? Math.max(...members.map(m => m.id)) + 1 : 1;
                members.unshift({ id: newId, ...payload, membershipDate: new Date().toISOString().split('T')[0] });
            }
            saveDemoStore('members', members);
        }

        showToast('Success', id ? 'Member updated successfully' : 'Member registered successfully', 'success');
        closeModal('addMemberModal');
        loadMembers();
        loadDashboardData();
    } catch (err) {
        showToast('Error', err.message, 'error');
    } finally {
        saveBtn.disabled = false;
        saveBtn.innerHTML = 'Register Member';
    }
}

function editMember(m) {
    document.getElementById('memberModalTitle').innerHTML = '<i class="fa-solid fa-user-pen"></i> Edit Member Profile';
    document.getElementById('memberId').value = m.id;
    document.getElementById('memberName').value = m.name;
    document.getElementById('memberEmail').value = m.email;
    document.getElementById('memberPhone').value = m.phone || '';
    document.getElementById('memberStatus').value = m.status;
    openModal('addMemberModal');
}

async function deleteMember(id, name) {
    if (!confirm(`Are you sure you want to delete member "${name}"?`)) return;

    try {
        if (isApiOnline) {
            const res = await fetch(`${API_BASE}/members/${id}`, { method: 'DELETE' });
            if (!res.ok) {
                const data = await res.json();
                throw new Error(data.message || 'Failed to delete member');
            }
        } else {
            let members = getDemoStore('members');
            members = members.filter(m => m.id != id);
            saveDemoStore('members', members);
        }
        showToast('Deleted', `Member "${name}" was deleted`, 'success');
        loadMembers();
        loadDashboardData();
    } catch (err) {
        showToast('Delete Failed', err.message, 'error');
    }
}

async function viewMemberLoans(memberId, memberName) {
    switchTab('loans-tab');
    const loans = getDemoStore('loans').filter(l => l.memberId == memberId);
    renderLoansTable(loans);
}

// ==============================================================================
// Borrow & Return Hub
// ==============================================================================
async function loadLoans() {
    const tbody = document.getElementById('loansTableBody');
    tbody.innerHTML = '<tr><td colspan="9" class="text-center py-4"><i class="fa-solid fa-spinner fa-spin"></i> Loading transactions...</td></tr>';

    let loans = [];
    if (isApiOnline) {
        try {
            let url = `${API_BASE}/borrow?page=0&size=50&sortBy=id&direction=desc`;
            if (currentLoanFilter !== 'ALL') url = `${API_BASE}/borrow/status/${currentLoanFilter}`;
            const res = await fetch(url);
            if (res.ok) loans = (await res.json()).content || [];
        } catch (err) {
            loans = getDemoStore('loans');
        }
    } else {
        loans = getDemoStore('loans');
    }

    if (currentLoanFilter !== 'ALL') {
        loans = loans.filter(l => l.status === currentLoanFilter);
    }

    renderLoansTable(loans);
}

function filterLoans(status, btnElement) {
    currentLoanFilter = status;
    document.querySelectorAll('.filter-pills .pill').forEach(p => p.classList.remove('active'));
    if (btnElement) btnElement.classList.add('active');
    loadLoans();
}

function renderLoansTable(loans) {
    const tbody = document.getElementById('loansTableBody');
    if (!loans || loans.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="text-center py-4">No borrow records found.</td></tr>';
        return;
    }

    tbody.innerHTML = loans.map(l => `
        <tr>
            <td>#${l.id}</td>
            <td><strong>${escapeHtml(l.bookTitle)}</strong><br><span style="font-size:12px;color:var(--text-muted);">ISBN: ${escapeHtml(l.bookIsbn)}</span></td>
            <td>${escapeHtml(l.memberName)}<br><span style="font-size:12px;color:var(--text-muted);">${escapeHtml(l.memberEmail)}</span></td>
            <td>${l.borrowDate}</td>
            <td>${l.dueDate}</td>
            <td>${l.returnDate || '-'}</td>
            <td><span class="badge badge-${l.status.toLowerCase()}">${l.status}</span></td>
            <td>${l.fineAmount > 0 ? `<strong style="color:var(--red);">$${l.fineAmount.toFixed(2)}</strong>` : '$0.00'}</td>
            <td class="text-right">
                ${l.status === 'BORROWED' ? `
                    <button class="btn btn-accent btn-sm" onclick="returnBook(${l.id}, '${escapeHtml(l.bookTitle)}')">
                        <i class="fa-solid fa-arrow-turn-down"></i> Return
                    </button>
                ` : `
                    <span style="color:var(--text-muted);font-size:12px;">Completed</span>
                `}
            </td>
        </tr>
    `).join('');
}

async function quickBorrow(bookId, bookTitle) {
    openModal('borrowBookModal');
    await populateBorrowDropdowns();
    const bookSelect = document.getElementById('borrowBookSelect');
    bookSelect.value = bookId;
}

async function populateBorrowDropdowns() {
    const memberSelect = document.getElementById('borrowMemberSelect');
    const bookSelect = document.getElementById('borrowBookSelect');

    let members = [], books = [];
    if (isApiOnline) {
        try {
            const [mRes, bRes] = await Promise.all([
                fetch(`${API_BASE}/members?page=0&size=100`),
                fetch(`${API_BASE}/books?page=0&size=100`)
            ]);
            if (mRes.ok) members = (await mRes.json()).content || [];
            if (bRes.ok) books = (await bRes.json()).content || [];
        } catch (e) {
            members = getDemoStore('members');
            books = getDemoStore('books');
        }
    } else {
        members = getDemoStore('members');
        books = getDemoStore('books');
    }

    const activeMembers = members.filter(m => m.status === 'ACTIVE');
    memberSelect.innerHTML = '<option value="">-- Choose active member --</option>' +
        activeMembers.map(m => `<option value="${m.id}">${escapeHtml(m.name)} (${escapeHtml(m.email)})</option>`).join('');

    const availableBooks = books.filter(b => b.availableCopies > 0);
    bookSelect.innerHTML = '<option value="">-- Choose available book --</option>' +
        availableBooks.map(b => `<option value="${b.id}">${escapeHtml(b.title)} (${b.availableCopies} avail)</option>`).join('');
}

async function handleBorrowSubmit(e) {
    e.preventDefault();
    const btn = document.getElementById('confirmBorrowBtn');
    btn.disabled = true;
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Issuing...';

    const memberId = parseInt(document.getElementById('borrowMemberSelect').value);
    const bookId = parseInt(document.getElementById('borrowBookSelect').value);
    const borrowDays = parseInt(document.getElementById('borrowDays').value) || 14;
    const remarks = document.getElementById('borrowRemarks').value.trim();

    try {
        if (isApiOnline) {
            const res = await fetch(`${API_BASE}/borrow`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ memberId, bookId, borrowDays, remarks })
            });
            const data = await res.json();
            if (!res.ok) throw new Error(data.message || 'Failed to borrow book');
        } else {
            // Local demo issue
            let books = getDemoStore('books');
            let members = getDemoStore('members');
            let loans = getDemoStore('loans');

            const book = books.find(b => b.id == bookId);
            const member = members.find(m => m.id == memberId);

            if (!book || book.availableCopies <= 0) throw new Error('No copies available for loan');

            // Decrement copies
            book.availableCopies -= 1;
            saveDemoStore('books', books);

            const today = new Date();
            const dueDate = new Date();
            dueDate.setDate(today.getDate() + borrowDays);

            const newLoan = {
                id: loans.length > 0 ? Math.max(...loans.map(l => l.id)) + 1 : 1,
                bookId: book.id,
                bookTitle: book.title,
                bookIsbn: book.isbn,
                memberId: member.id,
                memberName: member.name,
                memberEmail: member.email,
                borrowDate: today.toISOString().split('T')[0],
                dueDate: dueDate.toISOString().split('T')[0],
                returnDate: null,
                status: 'BORROWED',
                fineAmount: 0.00,
                remarks: remarks
            };
            loans.unshift(newLoan);
            saveDemoStore('loans', loans);
        }

        showToast('Book Issued!', 'Loan issued successfully and inventory updated', 'success');
        closeModal('borrowBookModal');
        loadDashboardData();
        loadBooks();
        loadLoans();
    } catch (err) {
        showToast('Issue Loan Failed', err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = 'Confirm Loan';
    }
}

async function returnBook(borrowRecordId, bookTitle) {
    if (!confirm(`Confirm return of "${bookTitle}"?`)) return;

    try {
        let fineAmount = 0;
        if (isApiOnline) {
            const res = await fetch(`${API_BASE}/borrow/${borrowRecordId}/return`, { method: 'PUT' });
            const data = await res.json();
            if (!res.ok) throw new Error(data.message || 'Failed to return book');
            fineAmount = data.fineAmount || 0;
        } else {
            let loans = getDemoStore('loans');
            let books = getDemoStore('books');
            const loan = loans.find(l => l.id == borrowRecordId);
            if (!loan) throw new Error('Loan record not found');

            const today = new Date();
            const todayStr = today.toISOString().split('T')[0];
            loan.returnDate = todayStr;
            loan.status = 'RETURNED';

            if (todayStr > loan.dueDate) {
                const days = Math.max(1, Math.ceil((today - new Date(loan.dueDate)) / (1000 * 60 * 60 * 24)));
                fineAmount = days * 1.00;
                loan.fineAmount = fineAmount;
            }

            // Restock book
            const book = books.find(b => b.id == loan.bookId);
            if (book) {
                book.availableCopies = Math.min(book.totalCopies, book.availableCopies + 1);
                saveDemoStore('books', books);
            }
            saveDemoStore('loans', loans);
        }

        let msg = `"${bookTitle}" returned successfully.`;
        if (fineAmount > 0) {
            msg += ` Late fine imposed: $${fineAmount.toFixed(2)}`;
        }
        showToast('Book Returned', msg, fineAmount > 0 ? 'info' : 'success');

        loadDashboardData();
        loadBooks();
        loadLoans();
        loadOverdueLoans();
    } catch (err) {
        showToast('Return Failed', err.message, 'error');
    }
}

// ==============================================================================
// Overdue Loans
// ==============================================================================
async function loadOverdueLoans() {
    const tbody = document.getElementById('overdueTableBody');
    tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4"><i class="fa-solid fa-spinner fa-spin"></i> Checking overdue records...</td></tr>';

    let overdueList = [];
    const today = new Date();
    const todayStr = today.toISOString().split('T')[0];

    if (isApiOnline) {
        try {
            const res = await fetch(`${API_BASE}/borrow/overdue`);
            if (res.ok) overdueList = await res.json();
        } catch (e) {
            overdueList = getDemoStore('loans').filter(l => l.status === 'BORROWED' && l.dueDate < todayStr);
        }
    } else {
        overdueList = getDemoStore('loans').filter(l => l.status === 'BORROWED' && l.dueDate < todayStr);
    }

    if (!overdueList || overdueList.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4" style="color:var(--accent);"><i class="fa-solid fa-circle-check"></i> Great job! No books are currently overdue.</td></tr>';
        return;
    }

    tbody.innerHTML = overdueList.map(item => {
        const dueDate = new Date(item.dueDate);
        const diffTime = Math.abs(today - dueDate);
        const daysOverdue = Math.max(1, Math.ceil(diffTime / (1000 * 60 * 60 * 24)));
        const fine = (daysOverdue * 1.00).toFixed(2);

        return `
            <tr>
                <td>#${item.id}</td>
                <td><strong>${escapeHtml(item.bookTitle)}</strong><br><span style="font-size:12px;color:var(--text-muted);">ISBN: ${escapeHtml(item.bookIsbn)}</span></td>
                <td>${escapeHtml(item.memberName)}<br><span style="font-size:12px;color:var(--text-muted);">${escapeHtml(item.memberEmail)}</span></td>
                <td style="color:var(--red); font-weight:600;">${item.dueDate}</td>
                <td><span class="badge badge-overdue">${daysOverdue} Day(s) Late</span></td>
                <td><strong style="color:var(--red);">$${fine}</strong></td>
                <td class="text-right">
                    <button class="btn btn-accent btn-sm" onclick="returnBook(${item.id}, '${escapeHtml(item.bookTitle)}')">
                        <i class="fa-solid fa-arrow-turn-down"></i> Return & Settle
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

// ==============================================================================
// Modal Control
// ==============================================================================
function openModal(modalId) {
    if (modalId === 'addBookModal' && !document.getElementById('bookId').value) {
        document.getElementById('bookModalTitle').innerHTML = '<i class="fa-solid fa-book"></i> Add New Book';
        document.getElementById('bookForm').reset();
    }
    if (modalId === 'addMemberModal' && !document.getElementById('memberId').value) {
        document.getElementById('memberModalTitle').innerHTML = '<i class="fa-solid fa-user-plus"></i> Register Library Member';
        document.getElementById('memberForm').reset();
    }
    if (modalId === 'borrowBookModal') {
        populateBorrowDropdowns();
    }

    const modal = document.getElementById(modalId);
    if (modal) modal.classList.add('active');
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove('active');
        if (modalId === 'addBookModal') document.getElementById('bookId').value = '';
        if (modalId === 'addMemberModal') document.getElementById('memberId').value = '';
    }
}

window.addEventListener('click', (e) => {
    if (e.target.classList.contains('modal-overlay')) {
        e.target.classList.remove('active');
        document.getElementById('bookId').value = '';
        document.getElementById('memberId').value = '';
    }
});

// ==============================================================================
// Toast Notifications
// ==============================================================================
function showToast(title, message, type = 'info') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;

    let iconClass = 'fa-circle-info';
    if (type === 'success') iconClass = 'fa-circle-check';
    if (type === 'error') iconClass = 'fa-triangle-exclamation';

    toast.innerHTML = `
        <i class="fa-solid ${iconClass} toast-icon"></i>
        <div class="toast-body">
            <h4>${escapeHtml(title)}</h4>
            <p>${escapeHtml(message)}</p>
        </div>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}
