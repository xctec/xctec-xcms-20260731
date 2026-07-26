import { Outlet } from 'react-router-dom';
import PortalSidebar from '@/components/layout/PortalSidebar';
import Topbar from '@/components/layout/Topbar';

export default function PortalLayout() {
  return (
    <div className="flex min-h-screen bg-gray-50">
      <PortalSidebar />
      <div className="flex flex-1 flex-col min-w-0">
        <Topbar />
        <main className="flex-1 overflow-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
