document.addEventListener('DOMContentLoaded', () => {
    fetch('http://localhost:8080/customer/cartItemNumber')
        .then(response => {
            if (!response.ok) {
                throw new Error('HTTP error status: ' + response.status);
            }
            return response.text();
        })
        .then(data => {
            const cartElement = document.getElementById('cartNumber');
            if(cartElement != null){
                cartElement.textContent = data
            }
        })
        .catch(err => console.error(err));
})