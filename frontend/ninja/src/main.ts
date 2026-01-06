import { route, initRouter } from "./router";
import { renderLogin } from "./pages/login";
import { renderDashboard } from "./pages/dashboard";
import { renderShop } from "./pages/shop";
import { renderLeaderboard } from "./pages/leaderboard";
import { attachNavbarHandlers } from "./components/navbar";
import "./styles/main.scss";

// Register routes
route("/", renderLogin);
route("/dashboard", async () => {
  await renderDashboard();
  attachNavbarHandlers();
});
route("/shop", async () => {
  await renderShop();
  attachNavbarHandlers();
});
route("/leaderboard", async () => {
  await renderLeaderboard();
  attachNavbarHandlers();
});

// Initialize router
initRouter();
