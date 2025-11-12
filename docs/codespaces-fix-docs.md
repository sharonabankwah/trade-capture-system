### Codespaces Setup and Configuration Fixes

### Overview

This document summarises the fixes implemented to enable seamless frontend–backend communication in GitHub Codespaces.
It covers key issues such as CORS restrictions, environment variable setup, and Swagger configuration, ensuring the application runs reliably in the Codespaces environment.

---

### **fix(WebConfig): Allow Codespaces frontend to access backend**

**Problem:** The frontend running in Codespaces could not communicate with the backend due to CORS errors.

**Root Cause:** The CORS configuration in WebConfig.java did not include the public Codespaces URL.

**Solution:** Added the Codespaces frontend URL to allowedOrigins in the CORS configuration.

**Impact:** The frontend can now successfully make API calls to the backend when running in Codespaces.

---

### **refactor(api.ts): Update API base URL for Codespaces support**

**Problem:** The frontend attempted to call localhost when running in Codespaces, causing all API requests to fail.

**Root Cause:** The Axios instance used a hardcoded localhost URL instead of referencing an environment variable.

**Solution:** Updated baseURL to use import.meta.env.VITE_API_BASE_URL, allowing it to dynamically use the Codespaces URL from the .env file.

**Impact:** API calls now correctly reach the backend in both local and Codespaces environments.

---

### **chore(env): Add VITE_API_BASE_URL for Codespaces environment**

**Problem:** The frontend could not locate the backend API when deployed in Codespaces.

**Root Cause:** The .env file did not include a VITE_API_BASE_URL variable pointing to the exposed backend port URL.

**Solution:** Added VITE_API_BASE_URL=https://<your-codespace-id>--8080.app.github.dev/api to the .env file.

**Impact:** The Vite development server now correctly injects the Codespaces API URL at build time.

---

### **chore(vite-env.d.ts): Define VITE_API_BASE_URL type for environment variables**

**Problem:** TypeScript did not recognise the VITE_API_BASE_URL variable referenced in api.ts.

**Root Cause:** The vite-env.d.ts file lacked a type definition for the VITE_API_BASE_URL variable.

**Solution:** Declared the variable within the ImportMetaEnv interface to ensure type safety and IDE recognition.

**Impact:** TypeScript now properly validates the VITE_API_BASE_URL reference and provides autocomplete support.

---

### **feat(OpenApiConfig): Display correct Codespaces URL in Swagger UI**

**Problem:** Swagger UI displayed http://localhost:8080 even when running in Codespaces, leading to incorrect API routing.

**Root Cause:** Springdoc generated the default server list using the internal container host instead of the public Codespaces domain.

**Solution:** Updated OpenApiConfig to explicitly define the Codespaces endpoint in the Swagger configuration.

**Impact:** Swagger UI now correctly displays and uses the public Codespaces URL for all API requests.

---

### **chore(application.properties): Configure Swagger UI to use Codespaces backend URL**

**Problem:** Swagger UI attempted to send API requests to localhost instead of the Codespaces backend.

**Root Cause:** The springdoc.swagger-ui.server-url property was not defined in application.properties.

**Solution:** Added a springdoc.swagger-ui.server-url property pointing to the Codespaces backend URL.

**Impact:** Swagger UI now targets the correct backend endpoint when accessed via Codespaces.
