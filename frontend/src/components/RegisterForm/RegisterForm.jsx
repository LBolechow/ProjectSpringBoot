import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { register } from "../../services/authService";
import "./RegisterForm.css";

export default function RegisterForm() {
  const [formData, setFormData] = useState({ name: "", email: "", password: "" });
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
      await register(formData);
      navigate("/");
    } catch (err) {
      setError("Błąd podczas rejestracji.");
    }
  };

  return (
    <div className="register-wrapper">
      <div className="register-box">
        <h2>Rejestracja</h2>
        <form onSubmit={handleSubmit}>
          <input
            type="text"
            name="name"
            placeholder="Imię i nazwisko"
            onChange={handleChange}
            required
          />
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
          <button type="submit">Zarejestruj</button>
        </form>

        {error && <p className="text-error">{error}</p>}

        <p className="text-muted">
          Masz już konto?{" "}
          <Link className="text-link" to="/">Zaloguj się</Link>
        </p>
      </div>
    </div>
  );
}
