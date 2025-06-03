import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "../../services/authService";
import { Link } from "react-router-dom";
import "./LoginForm.css";

export default function LoginForm() {
  const [formData, setFormData] = useState({ email: "", password: "" });
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    try {
      await login(formData); // wywołuje POST do /api/auth/login
      navigate("/user");
    } catch (err) {
      setError("Nieprawidłowy e-mail lub hasło.");
      console.error(err);
    }
  };

  return (
    <div className="login-wrapper">
      <div className="login-box">
        <h2>Logowanie</h2>
        <form onSubmit={handleSubmit}>
          <input
            type="email"
            name="email"
            placeholder="Email"
            onChange={handleChange}
            required
          />
          <input
            type="password"
            name="password"
            placeholder="Hasło"
            onChange={handleChange}
            required
          />
          <button type="submit">Zaloguj</button>
        </form>

        {error && <p className="text-error">{error}</p>}

        <p className="text-muted">
          Nie masz konta?{" "}
          <Link className="text-link" to="/register">Zarejestruj się</Link>
        </p>
      </div>
    </div>
  );
}
