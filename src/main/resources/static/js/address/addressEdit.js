// get saved codes from hidden inputs (null if not present)
const savedProvinceCode = document.getElementById("savedProvinceCode")?.value || null;
const savedWardCode = document.getElementById("savedWardCode")?.value || null;
console.log("Saved Province Code:", savedProvinceCode);
console.log("Saved Ward Code:", savedWardCode);
async function loadProvinces() {
  const res = await fetch("/admin/address/provinces");
  const provinces = await res.json();

  const provinceSelect = document.getElementById("provinceCityCode");
  provinceSelect.innerHTML = "<option value=''>-- Select Province --</option>";

  provinces.forEach(p => {
    const option = document.createElement("option");
    option.value = p.code;
    option.text = p.name;
    if (savedProvinceCode && p.code === savedProvinceCode) {
      option.selected = true; // preselect province
    }
    provinceSelect.add(option);
  });

  // load wards if province already chosen
  if (savedProvinceCode) {
    await loadWards(savedProvinceCode);
  }

  provinceSelect.onchange = () => loadWards(provinceSelect.value);
}

async function loadWards(provinceCode) {
  const wardSelect = document.getElementById("communeWardCode");
  if (!provinceCode) {
    wardSelect.innerHTML = "<option value=''>-- Select Ward --</option>";
    return;
  }

  const res = await fetch(`/admin/address/wards/${provinceCode}`);
  const wards = await res.json();

  wardSelect.innerHTML = "<option value=''>-- Select Ward --</option>";

  wards.forEach(w => {
    const option = document.createElement("option");
    option.value = w.code;
    option.text = w.name;
    if (savedWardCode && w.code === savedWardCode) {
      option.selected = true; // preselect ward
    }
    wardSelect.add(option);
  });
}

window.onload = loadProvinces;
