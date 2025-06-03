import axios from "axios";

const API_URL = "http://localhost:8080/api/auth";

export async function login(credentials) {
  const response = await axios.post(`${API_URL}/login`, credentials);
  const token = response.data.token;
  localStorage.setItem("token", token);
  return token;
}

export async function register(data) {
  const response = await axios.post(`${API_URL}/register`, data);
  return response.data;
}

export function logout() {
  localStorage.removeItem("token");
}

export function getToken() {
  return localStorage.getItem("token");
}

export function isAuthenticated() {
  return !!getToken();
}
