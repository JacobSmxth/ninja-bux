import { route, initRouter } from "./router";
import { renderLogin } from "./pages/login";
import { renderDashboard } from "./pages/dashboard";
import { renderNinjas } from "./pages/ninjas";
import { renderShop } from "./pages/shop";
import { renderPurchases } from "./pages/purchases";
import { renderAdmins } from "./pages/admins";
import "./styles/main.scss";

// Register routes
route("/login", renderLogin);
route("/dashboard", renderDashboard);
route("/ninjas", renderNinjas);
route("/shop", renderShop);
route("/purchases", renderPurchases);
route("/admins", renderAdmins);

// Initialize router
initRouter();
