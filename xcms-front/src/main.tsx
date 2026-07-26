import React from 'react';
import ReactDOM from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { router } from './routes';
import { ThemeProvider } from './components/ThemeProvider';
import { ToastContainer } from './components/ui/Toast';
import './index.css';
import './i18n';
import { initTenantIdFromUrl } from './utils/tenant';

// 启动时从 URL ?tenantId=xxx 解析并持久化（支持多租户通过链接切换）
initTenantIdFromUrl();

async function enableMocking() {
  if (import.meta.env.VITE_MOCK === 'true') {
    const { worker } = await import('./mocks/browser');
    await worker.start({
      onUnhandledRequest: 'bypass',
      serviceWorker: { url: `${import.meta.env.BASE_URL}mockServiceWorker.js` },
    });
  }
}

const queryClient = new QueryClient({
  defaultOptions: { queries: { staleTime: 30_000, refetchOnWindowFocus: false } },
});

enableMocking().then(() => {
  ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
      <QueryClientProvider client={queryClient}>
        <ThemeProvider>
          <RouterProvider router={router} />
          <ToastContainer />
        </ThemeProvider>
      </QueryClientProvider>
    </React.StrictMode>
  );
});
