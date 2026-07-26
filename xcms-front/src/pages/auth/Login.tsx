import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { User, Lock, Eye, EyeOff } from 'lucide-react';
import { authApi } from '@/api/auth';
import { menuApi } from '@/api/menu';
import { useAuthStore } from '@/stores/auth';
import { getLoginTenantId } from '@/utils/tenant';

export default function LoginPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { setAuth, setMenus } = useAuthStore();
  // 仅开发环境预填演示账号，生产环境留空（避免硬编码凭证）
  const [username, setUsername] = useState(import.meta.env.DEV ? 'admin' : '');
  const [password, setPassword] = useState(import.meta.env.DEV ? 'admin123' : '');
  const [showPwd, setShowPwd] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const result = await authApi.login({ username, password, tenantId: getLoginTenantId() });
      setAuth(result);
      try {
        const [adminMenus, portalMenus] = await Promise.all([
          menuApi.getUserMenus('admin'),
          menuApi.getUserMenus('portal'),
        ]);
        setMenus(adminMenus, portalMenus);
      } catch {
        // 菜单拉取失败不阻断登录，使用空菜单
      }
      navigate('/admin');
    } catch (err) {
      setError(err instanceof Error ? err.message : '登录失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h2 className="mb-1 text-xl font-bold text-gray-900">{t('login.welcome')}</h2>
      <p className="mb-6 text-sm text-gray-500">{t('login.title')}</p>

      {error && (
        <div className="mb-4 rounded-md bg-danger-50 px-3 py-2 text-xs text-danger-500">{error}</div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">{t('login.username')}</label>
          <div className="relative">
            <User size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
            <input
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder={t('login.placeholder.username')}
              className="h-10 w-full rounded-md border border-gray-300 pl-10 pr-3 text-sm outline-none focus:border-primary-500 focus:ring-1 focus:ring-primary-500"
            />
          </div>
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">{t('login.password')}</label>
          <div className="relative">
            <Lock size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
            <input
              type={showPwd ? 'text' : 'password'}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder={t('login.placeholder.password')}
              className="h-10 w-full rounded-md border border-gray-300 pl-10 pr-10 text-sm outline-none focus:border-primary-500 focus:ring-1 focus:ring-primary-500"
            />
            <button type="button" onClick={() => setShowPwd(!showPwd)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600">
              {showPwd ? <EyeOff size={16} /> : <Eye size={16} />}
            </button>
          </div>
        </div>
        <div className="flex items-center justify-between text-xs">
          <label className="flex items-center gap-1.5 text-gray-500">
            <input type="checkbox" className="rounded border-gray-300 text-primary-500 focus:ring-primary-500" /> 记住我
          </label>
          <a href="/forgot-password" className="text-primary-500 hover:underline">{t('login.forgot')}</a>
        </div>
        <button
          type="submit"
          disabled={loading}
          className="h-10 w-full rounded-md bg-primary-500 text-sm font-medium text-white transition-colors hover:bg-primary-600 disabled:opacity-50"
        >
          {loading ? '登录中...' : t('login.submit')}
        </button>
      </form>

      <p className="mt-4 text-center text-xs text-gray-400">
        {t('login.register')}？<Link to="/register" className="text-primary-500 hover:underline">{t('register.submit')}</Link>
      </p>
    </div>
  );
}
