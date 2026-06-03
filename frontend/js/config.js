const FRONTEND_HOST = window.location.hostname;
const API_HOST = !FRONTEND_HOST || FRONTEND_HOST === "localhost" || FRONTEND_HOST === "127.0.0.1"
    ? "localhost"
    : FRONTEND_HOST;

const BASE_URL = `http://${API_HOST}:8080`;
