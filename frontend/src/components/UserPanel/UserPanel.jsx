import "./UserPanel.css";
import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";

const mockLogs = [
  { date: "2025-06-01 08:23", ip: "192.168.1.10", location: "Warszawa", browser: "Chrome", status: "Sukces" },
  { date: "2025-06-01 08:25", ip: "192.168.1.11", location: "Kraków", browser: "Firefox", status: "Błąd" },
  { date: "2025-06-01 08:30", ip: "192.168.1.10", location: "Warszawa", browser: "Edge", status: "Sukces" },
  { date: "2025-06-01 08:35", ip: "192.168.1.20", location: "Poznań", browser: "Safari", status: "Zablokowano" },
];

const pieData = [
  { name: "Sukces", value: 2 },
  { name: "Błąd", value: 1 },
  { name: "Zablokowano", value: 1 },
];

const COLORS = ["#34d399", "#fbbf24", "#ef4444"];

export default function UserPanel() {
  return (
    <div className="user-panel-wrapper">
      <div className="user-panel-box">
        <header className="user-panel-header">
          <h1>Witaj, Jan Kowalski 👋</h1>
          <button className="logout-button">Wyloguj</button>
        </header>

        <section className="user-panel-content">


          <div className="info-card">
            <h2>Typy logowań</h2>
            <div style={{ width: "100%", height: 300 }}>
              <ResponsiveContainer>
                <PieChart>
                  <Pie
                    data={pieData}
                    cx="50%"
                    cy="50%"
                    labelLine={false}
                    outerRadius={100}
                    dataKey="value"
                  >
                    {pieData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className="info-card">
            <h2>Historia logowań</h2>
            <table className="logs-table">
              <thead>
                <tr>
                  <th>Data</th>
                  <th>IP</th>
                  <th>Miasto</th>
                  <th>Przeglądarka</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {mockLogs.map((log, index) => (
                  <tr key={index}>
                    <td>{log.date}</td>
                    <td>{log.ip}</td>
                    <td>{log.location}</td>
                    <td>{log.browser}</td>
                    <td>{log.status}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      </div>
    </div>
  );
}
