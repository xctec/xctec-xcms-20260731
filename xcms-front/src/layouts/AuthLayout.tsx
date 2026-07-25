import { Outlet } from 'react-router-dom';

export default function AuthLayout() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-primary-500 to-primary-700 p-4">
      <div className="w-full max-w-md">
        <div className="mb-8 text-center text-white">
          <div className="mx-auto mb-3 flex h-16 w-16 items-center justify-center rounded-2xl bg-white/20 backdrop-blur">
            <span className="text-3xl font-bold">X</span>
          </div>
          <h1 className="text-2xl font-bold">XCMS 集团中台</h1>
          <p className="mt-1 text-sm text-white/80">Enterprise Middle Platform</p>
        </div>
        <div className="rounded-xl bg-white p-8 shadow-2xl">
          <Outlet />
        </div>
      </div>
    </div>
  );
}
