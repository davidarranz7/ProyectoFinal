fetch("../templates/menu.html")
    .then(response => response.text())
    .then(data => {
        document.getElementById("menu-container").innerHTML = data;
        inicializarMenu();
    })
    .catch(error => console.error("Error al cargar el menú:", error));

function inicializarMenu() {
    const loginLink = document.getElementById("login-link");
    const profileMenu = document.getElementById("profile-menu");
    const profileName = document.getElementById("profile-name");
    const profileBtn = document.getElementById("profile-btn");
    const profileDropdown = document.getElementById("profile-dropdown");
    const logoutBtn = document.getElementById("logout-btn");

    const usuarioLogueado = localStorage.getItem("usuarioLogueado") === "true";
    const nombreUsuario = localStorage.getItem("nombreUsuario");

    if (usuarioLogueado && nombreUsuario) {
        loginLink.style.display = "none";
        profileMenu.style.display = "block";
        profileName.textContent = nombreUsuario;
    } else {
        loginLink.style.display = "inline-flex";
        profileMenu.style.display = "none";
    }

    if (profileBtn) {
        profileBtn.addEventListener("click", (e) => {
            e.stopPropagation();
            profileDropdown.classList.toggle("activo");
        });
    }

    document.addEventListener("click", (e) => {
        if (profileMenu && !profileMenu.contains(e.target)) {
            profileDropdown.classList.remove("activo");
        }
    });

    if (logoutBtn) {
        logoutBtn.addEventListener("click", (e) => {
            e.preventDefault();
            localStorage.removeItem("usuarioLogueado");
            localStorage.removeItem("nombreUsuario");
            window.location.href = "index.html";
        });
    }
}