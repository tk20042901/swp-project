
async function loadProvinces() {
  const res = await fetch("/admin/address/provinces"); // backend API
  const provinces = await res.json();

  const provinceSelect = document.getElementById("provinceCityCode");
  provinceSelect.innerHTML = "<option value=''>-- Select Province --</option>";

  provinces.forEach(p => {
    const option = document.createElement("option");
    option.value = p.code;  
    option.text = p.name;
    provinceSelect.add(option);
  });

  // when province changes → load wards
  provinceSelect.onchange = () => loadWards(provinceSelect.value);
}

async function loadWards(provinceCode) {
  if (!provinceCode) {
    document.getElementById("communeWardCode").innerHTML = "<option value=''>-- Select Ward --</option>";
    return;
  }

  const res = await fetch(`/admin/address/wards/${provinceCode}`);
  const wards = await res.json();

  const wardSelect = document.getElementById("communeWardCode");
  wardSelect.innerHTML = "<option value=''>-- Select Ward --</option>";

  wards.forEach(w => {
    const option = document.createElement("option");
    option.value = w.code;   
    option.text = w.name;
    wardSelect.add(option);
  });
}


window.onload = loadProvinces;
