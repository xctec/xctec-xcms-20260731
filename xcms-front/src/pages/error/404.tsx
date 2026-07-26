import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Home, ArrowLeft } from 'lucide-react';

export default function NotFoundPage() {
  const { t } = useTranslation();
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-gray-50">
      <div className="text-7xl font-bold text-primary-500">404</div>
      <h1 className="mt-4 text-xl font-semibold text-gray-800">{t('error.404.title')}</h1>
      <p className="mt-2 text-sm text-gray-500">{t('error.404.desc')}</p>
      <div className="mt-6 flex gap-3">
        <Link to="/" className="flex items-center gap-1.5 rounded-md bg-primary-500 px-4 py-2 text-sm font-medium text-white hover:bg-primary-600">
          <Home size={16} /> 返回首页
        </Link>
        <button onClick={() => window.history.back()} className="flex items-center gap-1.5 rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-600 hover:bg-gray-50">
          <ArrowLeft size={16} /> 返回上一页
        </button>
      </div>
    </div>
  );
}
