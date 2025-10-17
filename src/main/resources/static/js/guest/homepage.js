const productContainer = document.getElementById('productCategoryContainer')
const categoryContainer = document.getElementById('category');

async function loadAllCategory() {
    categoryContainer.innerHTML = '';
    const loadingOption = document.createElement('option');
    loadingOption.textContent = '⏳ Đang tải danh mục...';
    loadingOption.disabled = true;
    loadingOption.selected = true;
    categoryContainer.appendChild(loadingOption);
    let categoryData = [];
    try {
        const categories = await fetch('http://localhost:8080/api/categories')
        categoryData = await categories.json();
    } catch (error) {
        console.error('Error fetching categories:', error);
    }
    categoryContainer.innerHTML = '';
    const defaultOption = document.createElement('option');
    defaultOption.value = 0;
    defaultOption.textContent = ' Tất cả danh mục';
    categoryContainer.appendChild(defaultOption);
    categoryData.forEach(category => {
        const option = document.createElement('option');
        option.value = category.id;
        option.textContent = `📁 ${category.name}`;
        categoryContainer.appendChild(option);
    });
}
async function loadProductsByCategory(categoryId, container, sortBy = 'default') {
    let url = `http://localhost:8080/api/products?page=0&size=6&sortBy=${sortBy}`;
    if (categoryId && categoryId != 0) {
        url += `&categoryId=${categoryId}`;
    }
    let products = [];
    try {
        const response = await fetch(url);
        products = await response.json();
    } catch (error) {
        console.error('Error fetching products:', error);
    }
    container.innerHTML = '';
    products.forEach(product => {
        const productCard = document.createElement('div');
        productCard.className = 'product-card';
        productCard.innerHTML = `
            <a href="/product/${product.id}" class="product-card-link">
                <div class="card product-card h-100">
                    <div class="product-image">
                        <img src="${product.main_image_url}" class="card-img-top" alt="Product Image">
                    </div>
                    <div class="card-body product-info">
                        <div class="product-title">
                            <span>${product.name}</span>
                        </div>
                        <div class="product-price">
                            <span>${Number(product.price).toLocaleString('en-US')}</span>
                        </div>
                    </div>
                </div>
            </a>
        `;
        container.appendChild(productCard);
    });
}


document.addEventListener('DOMContentLoaded', () => {
    loadAllCategory();
    loadProductsByCategory(0, productContainer);
    loadProductsByCategory(0, document.getElementById('bestSellerContainer'), 'best-seller');
    loadProductsByCategory(0, document.getElementById('newestProductContainer'), 'newest');
});

categoryContainer.addEventListener('change', (event) => {
    const selectedCategoryId = event.target.value;
    loadProductsByCategory(selectedCategoryId, productContainer);
});

