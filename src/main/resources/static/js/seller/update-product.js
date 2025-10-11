document.addEventListener("DOMContentLoaded", function() {
    // Get all file inputs and revert buttons
    const fileInputs = document.querySelectorAll("input[type='file']");
    const revertButtons = document.querySelectorAll(".revert-btn");

    // Store the original image URLs
    const originalImages = {};

    document.querySelectorAll("img[id^='preview']").forEach(img => {
        originalImages[img.id] = img.src;
    });

    // Handle image preview for each file input
    fileInputs.forEach(input => {
        input.addEventListener("change", function(event) {
            const previewId = "preview" + input.id.replace("image", "");
            const previewImg = document.getElementById(previewId);
            const file = event.target.files[0];
            if (file && previewImg) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    previewImg.src = e.target.result;
                };
                reader.readAsDataURL(file);
            }
        });
    });

    // Handle revert button click
    revertButtons.forEach(button => {
        button.addEventListener("click", function() {
            const previewId = this.getAttribute("data-preview");
            const inputId = this.getAttribute("data-input");
            const previewImg = document.getElementById(previewId);
            const fileInput = document.getElementById(inputId);

            if (previewImg && originalImages[previewId]) {
                previewImg.src = originalImages[previewId];
            }
            if (fileInput) {
                fileInput.value = ""; // clear the selected file
            }
        });
    });
});
