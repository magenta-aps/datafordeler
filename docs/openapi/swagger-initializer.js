window.onload = function() {
  window.ui = SwaggerUIBundle({
    urls: [
      { name: "generic", url: "openapi/openapi.json" },
      { name: "combined", url: "openapi/combinedapi.json" },
      { name: "custody", url: "openapi/cprapi.json" },
      { name: "prisme", url: "openapi/prismeapi.json" },
      { name: "subscription", url: "openapi/subscriptionapi.json" },
      { name: "eskat", url: "openapi/eskatapi.json" }
    ],
    dom_id: '#swagger-ui',
    deepLinking: true,
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    plugins: [
      SwaggerUIBundle.plugins.DownloadUrl
    ],
    layout: "StandaloneLayout",
    supportedSubmitMethods: [],
  });
};
