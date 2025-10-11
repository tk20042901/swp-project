document.addEventListener("DOMContentLoaded", function() {
    
    // Get all file inputs and revert buttons
    const fileInputs = document.querySelectorAll("input[type='file']");
    const revertButtons = document.querySelectorAll(".revert-btn");
    
    // Initially hide all preview images
    document.querySelectorAll("img[id^='preview']").forEach(img => {
        img.style.display = 'none';
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
                    previewImg.style.display = 'block'; // Show the preview
                };
                reader.readAsDataURL(file);
            } else if (previewImg) {                
                previewImg.style.display = 'none';
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
            // Clear the file input and hide preview
            if (fileInput) {
                fileInput.value = ""; // clear the selected file
            }
            if (previewImg) {
                previewImg.style.display = 'none';
                previewImg.src = '';
            }
        });
    });
});
