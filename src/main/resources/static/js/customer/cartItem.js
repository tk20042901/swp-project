document.addEventListener('DOMContentLoaded', () => {
    fetch('http://localhost:8080/customer/cartItemNumber')
        .then(response => {
            if (!response.ok) {
                throw new Error('HTTP error status: ' + response.status);
            }
            return response.text();
        })
        .then(data => {
            document.getElementById('cartNumber').textContent = data
        })
        .catch(err => console.error(err));
})