import { type RouteConfig, index, route } from "@react-router/dev/routes";

export default [
	index("routes/home.tsx"),
	route("accept-invitation", "routes/accept-invitation.tsx"),
] satisfies RouteConfig;
