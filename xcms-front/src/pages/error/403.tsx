import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { ShieldX, Home } from 'lucide-react';

export default function ForbiddenPage() {
  const { t } = useTranslation();
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-gray-50">
      <ShieldX size={64} className="text-danger-500" />
      <h1 className="mt-4 text-xl font-semibold text-gray-800">403</h1>
      <p className="mt-2 text-sm text-gray-500">{t('error.403.desc')}</p>
      <Link to="/" className="mt-6 flex items-center gap-1.5 rounded-md bg-primary-500 px-4 py-2 text-sm font-medium text-white hover:bg-primary-600">
        <Home size={16} /> 返回首页
      </Link>
    </div>
  );
}
