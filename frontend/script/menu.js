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

    const authOverlay = document.getElementById("auth-overlay");
    const authBackdrop = document.getElementById("auth-backdrop");
    const authClose = document.getElementById("auth-close");
    const authFrame = document.getElementById("auth-frame");

    function actualizarEstadoUsuario() {
        const usuarioLogueado = localStorage.getItem("usuarioLogueado") === "true";
        const nombreUsuario = localStorage.getItem("nombreUsuario");

        if (usuarioLogueado && nombreUsuario) {
            if (loginLink) {
                loginLink.style.display = "none";
            }
            if (profileMenu) {
                profileMenu.style.display = "block";
            }
            if (profileName) {
                profileName.textContent = nombreUsuario;
            }
        } else {
            if (loginLink) {
                loginLink.style.display = "inline-flex";
            }
            if (profileMenu) {
                profileMenu.style.display = "none";
            }
        }
    }

    function abrirLogin() {
        if (authFrame) {
            authFrame.src = "login.html";
        }
        if (authOverlay) {
            authOverlay.classList.add("activo");
        }
        document.body.style.overflow = "hidden";
    }

    function abrirRegistro() {
        if (authFrame) {
            authFrame.src = "registro.html";
        }
        if (authOverlay) {
            authOverlay.classList.add("activo");
        }
        document.body.style.overflow = "hidden";
    }

    function cerrarOverlay() {
        if (authOverlay) {
            authOverlay.classList.remove("activo");
        }
        if (authFrame) {
            authFrame.src = "";
        }
        document.body.style.overflow = "";
    }

    actualizarEstadoUsuario();

    if (loginLink) {
        loginLink.addEventListener("click", abrirLogin);
    }

    if (authClose) {
        authClose.addEventListener("click", cerrarOverlay);
    }

    if (authBackdrop) {
        authBackdrop.addEventListener("click", cerrarOverlay);
    }

    if (profileBtn) {
        profileBtn.addEventListener("click", (e) => {
            e.stopPropagation();
            if (profileDropdown) {
                profileDropdown.classList.toggle("activo");
            }
        });
    }

    document.addEventListener("click", (e) => {
        if (profileMenu && !profileMenu.contains(e.target)) {
            if (profileDropdown) {
                profileDropdown.classList.remove("activo");
            }
        }
    });

    if (logoutBtn) {
        logoutBtn.addEventListener("click", (e) => {
            e.preventDefault();
            localStorage.removeItem("usuarioLogueado");
            localStorage.removeItem("nombreUsuario");
            actualizarEstadoUsuario();
            cerrarOverlay();
        });
    }

    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape" && authOverlay && authOverlay.classList.contains("activo")) {
            cerrarOverlay();
        }
    });

    window.abrirRegistro = abrirRegistro;
    window.abrirLogin = abrirLogin;
    window.cerrarAuthOverlay = cerrarOverlay;
    window.actualizarMenuUsuario = actualizarEstadoUsuario;
}