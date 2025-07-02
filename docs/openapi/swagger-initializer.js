window.onload = function() {
  window.ui = SwaggerUIBundle({
    urls: [
      { name: "generic", url: "openapi/generic.json" },
      { name: "combined", url: "openapi/combined.json" },
      { name: "custody", url: "openapi/custody.json" },
      { name: "prisme", url: "openapi/prisme.json" },
      { name: "subscription", url: "openapi/subscription.json" },
      { name: "eskat", url: "openapi/eskat.json" }
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
