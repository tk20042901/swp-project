// Lưu selection vào localStorage
function saveSelection() {
    const checked = document.querySelectorAll('.item-checkbox:checked');
    const selectedIds = Array.from(checked).map(cb => cb.value);
    localStorage.setItem('selectedCartItems', JSON.stringify(selectedIds));
}

// Khôi phục selection từ localStorage
function restoreSelection() {
    const savedIds = localStorage.getItem('selectedCartItems');
    if (savedIds) {
        const selectedIds = JSON.parse(savedIds);
        selectedIds.forEach(id => {
            const checkbox = document.querySelector(`.item-checkbox[value="${id}"]`);
            if (checkbox) {
                checkbox.checked = true;
            }
        });
        updateTotal();
    }
}

function toggleAll() {
    const checkboxes = document.querySelectorAll('.item-checkbox');
    const selectAll = document.getElementById('selectAll');
    checkboxes.forEach(cb => cb.checked = selectAll.checked);
    updateTotal();
    saveSelection(); // Lưu sau khi thay đổi
}

function updateTotal() {
    const checked = document.querySelectorAll('.item-checkbox:checked');
    const form = document.getElementById('checkoutForm');
    const btn = document.getElementById('checkoutBtn');
    const total = document.getElementById('total');

    form.querySelectorAll('input[name="cartIds"]').forEach(input => input.remove());

    let sum = 0;
    checked.forEach(cb => {
        const row = cb.closest('tr');
        const subtotalElement = row.querySelector('.item-subtotal');
        const subtotalText = subtotalElement.textContent;
        const subtotal = parseInt(subtotalText.replace(/[^\d]/g, ''));
        sum += subtotal;

        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'cartIds';
        input.value = cb.value;
        form.appendChild(input);
    });

    total.textContent = sum.toLocaleString() + ' VND';
    btn.disabled = checked.length === 0;
    saveSelection();
}

// Khôi phục selection khi trang load
document.addEventListener('DOMContentLoaded', function() {
    restoreSelection();
});