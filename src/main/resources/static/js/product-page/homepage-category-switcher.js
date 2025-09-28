/**
 * Homepage Category Switcher
 * Handles dynamic category switching without page reloads
 */

let productsByCategory = {};
let categories = [];

/**
 * Initialize the category switcher with data from the server
 * @param {Object} productsData - Products grouped by category
 * @param {Array} categoriesData - List of all categories
 */
function initializeCategorySwitcher(productsData, categoriesData) {
    console.log('initializeCategorySwitcher called with:', productsData, categoriesData);
    
    productsByCategory = productsData;
    categories = categoriesData;
    
    // Load all products by default
    switchCategory("0");
}

/**
 * Switch products display based on selected category
 * @param {string} categoryId - ID of the category to display
 */
function switchCategory(categoryId) {
    console.log('switchCategory called with:', categoryId);
    
    const products = productsByCategory[categoryId] || [];
    const container = document.getElementById('products-container');
    
    if (!container) {
        console.error('Products container not found');
        return;
    }
    
    // Clear current products
    container.innerHTML = '';
    
    // Add new products
    products.forEach(product => {
        const productCard = createProductCard(product);
        container.appendChild(productCard);
    });
    
    // Update "View All" button
    updateViewAllButton(categoryId);
}

// Make switchCategory globally accessible
window.switchCategory = switchCategory;

/**
 * Create a product card element
 * @param {Object} product - Product data
 * @returns {HTMLElement} Product card element
 */
function createProductCard(product) {
    const col = document.createElement('div');
    col.className = 'col';
    
    col.innerHTML = `
        <div class="card h-100">
            <img src="${product.mainImageUrl}" class="card-img-top" alt="Product Image"
                 style="width: 100%; height: 200px; object-fit: cover;">
            <div class="card-body">
                <a href="/product/${product.id}">
                    <h5 class="card-title">${escapeHtml(product.name)}</h5>
                </a>
                <p class="card-text">Giá: ${product.price} VND</p>
            </div>
        </div>
    `;
    
    return col;
}

/**
 * Update the "View All" button based on selected category
 * @param {string} categoryId - ID of the selected category
 */
function updateViewAllButton(categoryId) {
    const button = document.getElementById('view-all-button');
    
    if (!button) {
        console.error('View all button not found');
        return;
    }
    
    const category = categories.find(c => c.id == categoryId);

    button.href = `/product-category-display/${categoryId}`;

    if (categoryId == "0") {
        button.innerHTML = '<i class="fas fa-th-large me-2"></i>Xem tất cả sản phẩm';
    } else {
        const categoryName = category ? category.name : '';
        button.innerHTML = `<i class="fas fa-th-large me-2"></i>Xem tất cả sản phẩm ${escapeHtml(categoryName)}`;
    }
}

/**
 * Escape HTML characters to prevent XSS
 * @param {string} text - Text to escape
 * @returns {string} Escaped text
 */
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {

});