const mainImageDisplay = document.getElementById('previewMainImage');
const subImageDisplay1 = document.getElementById('previewSubImage1');
const subImageDisplay2 = document.getElementById('previewSubImage2');
const subImageDisplay3 = document.getElementById('previewSubImage3');
const mainImageInput = document.getElementById('mainImageFile');
const subImageInput1 = document.getElementById('subImageFile1');
const subImageInput2 = document.getElementById('subImageFile2');
const subImageInput3 = document.getElementById('subImageFile3');
const mainRevert = document.getElementById('mainImageRevertButton');
const subImageRevert1 = document.getElementById('subImage1RevertButton');
const subImageRevert2 = document.getElementById('subImage2RevertButton');
const subImageRevert3 = document.getElementById('subImage3RevertButton');
const placeholder1 = document.getElementById('placeholder1');
const placeholder2 = document.getElementById('placeholder2');
const placeholder3 = document.getElementById('placeholder3');
const productQuantity = document.getElementById('productQuantity');

document.addEventListener('DOMContentLoaded', () => {
    mainImageInput.addEventListener('change', (event)=>{
        showPreviewImage(event, mainImageDisplay, mainRevert, null);
    });
    subImageInput1.addEventListener('change',(event) =>{
        showPreviewImage(event, subImageDisplay1, subImageRevert1, placeholder1);
    });
    subImageInput2.addEventListener('change',(event) =>{
        showPreviewImage(event, subImageDisplay2, subImageRevert2, placeholder2);
    });
    subImageInput3.addEventListener('change',(event) =>{
        showPreviewImage(event, subImageDisplay3, subImageRevert3, placeholder3);
    });
    mainRevert.addEventListener('click',()=>{
        revertImage(mainImageInput, mainImageDisplay, mainRevert, null);
    });
    subImageRevert1.addEventListener('click',()=>{
        revertImage(subImageInput1, subImageDisplay1, subImageRevert1, placeholder1);
    });
    subImageRevert2.addEventListener('click',()=>{
        revertImage(subImageInput2, subImageDisplay2, subImageRevert2, placeholder2);
    });
    subImageRevert3.addEventListener('click',()=>{
        revertImage(subImageInput3, subImageDisplay3, subImageRevert3, placeholder3);
    });
    document.getElementById('productUnit').addEventListener('change',(event)=>{
        const select = event.target;
        const selectedValue = select.value;
        const selectedText = select.options[selectedValue].text;
        if(selectedText === "Kg"){
            productQuantity.setAttribute('step','0.01');
        }else{
            productQuantity.removeAttribute('step');
        }
    });
})

function revertImage(input, previewImage, revertBtn, placeholder){
    input.value = "";
    previewImage.src = "#";
    previewImage.style.display = "none";
    revertBtn.style.display = "none";
    if (placeholder) {
        placeholder.style.display = "flex";
    }
}

function showPreviewImage(event, preview, revertBtn, placeholder){
    const file = event.target.files[0];
    if (!file) return; 

    if (file.type.startsWith('image/')) {
      const reader = new FileReader();
      reader.onload = (e) => {
        preview.src = e.target.result;  
        preview.style.display = 'block';
        revertBtn.style.display = 'inline-block';
        if (placeholder) {
            placeholder.style.display = "none";
        }
      };
      reader.readAsDataURL(file); 
    } else {
      preview.style.display = 'none';
      if (placeholder) {
          placeholder.style.display = "flex";
      }
      alert('Please select an image file.');
    }
}
