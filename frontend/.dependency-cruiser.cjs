/** @type {import('dependency-cruiser').IConfiguration} */
module.exports = {
  forbidden: [
    {
      name: "no-circular-dependencies",
      severity: "error",
      from: {},
      to: {
        circular: true,
      },
    },
    {
      name: "no-admin-portal-cross-imports",
      severity: "error",
      from: {
        path: "^(app/admin|components/admin)/",
      },
      to: {
        path: "^(app/portal|components/portal)/",
      },
    },
    {
      name: "no-portal-admin-cross-imports",
      severity: "error",
      from: {
        path: "^(app/portal|components/portal)/",
      },
      to: {
        path: "^(app/admin|components/admin)/",
      },
    },
    {
      name: "no-hooks-depend-on-pages",
      severity: "error",
      from: {
        path: "^hooks/",
      },
      to: {
        path: "^app/",
      },
    },
    {
      name: "no-lib-depend-on-pages",
      severity: "error",
      from: {
        path: "^lib/",
      },
      to: {
        path: "^app/",
      },
    },
  ],
  options: {
    doNotFollow: {
      path: "node_modules",
    },
    exclude: {
      path: "node_modules",
    },
    includeOnly: "^(app|components|hooks|lib)/",
    tsPreCompilationDeps: true,
    combinedDependencies: true,
    reporterOptions: {
      dot: {
        collapsePattern: "node_modules/[^/]+",
      },
    },
  },
};
