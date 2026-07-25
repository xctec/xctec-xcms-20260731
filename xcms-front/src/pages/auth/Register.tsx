import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { User, Lock, Phone, Eye, EyeOff } from 'lucide-react';

export default function RegisterPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', password: '', confirmPassword: '', phone: '' });
  const [showPwd, setShowPwd] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (key: string, val: string) => setForm({ ...form, [key]: val });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    if (form.password !== form.confirmPassword) {
      setError('两次密码不一致');
      return;
    }
    setLoading(true);
    try {
      // TODO: 调用注册 API
      navigate('/login');
    } catch (err) {
      setError(err instanceof Error ? err.message : '注册失败');
    } finally {
      setLoading(false);
    }
  };

  const inputCls = 'h-10 w-full rounded-md border border-gray-300 pl-10 pr-3 text-sm outline-none focus:border-primary-500 focus:ring-1 focus:ring-primary-500';

  return (
    <div>
      <h2 className="mb-1 text-xl font-bold text-gray-900">{t('register.title')}</h2>
      <p className="mb-6 text-sm text-gray-500">创建您的账号</p>

      {error && <div className="mb-4 rounded-md bg-danger-50 px-3 py-2 text-xs text-danger-500">{error}</div>}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">{t('register.username')}</label>
          <div className="relative">
            <User size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
            <input value={form.username} onChange={(e) => handleChange('username', e.target.value)} className={inputCls} placeholder="请输入用户名" />
          </div>
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">{t('register.password')}</label>
          <div className="relative">
            <Lock size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
            <input type={showPwd ? 'text' : 'password'} value={form.password} onChange={(e) => handleChange('password', e.target.value)} className={inputCls} placeholder="请输入密码" />
            <button type="button" onClick={() => setShowPwd(!showPwd)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400">{showPwd ? <EyeOff size={16} /> : <Eye size={16} />}</button>
          </div>
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">{t('register.confirmPassword')}</label>
          <div className="relative">
            <Lock size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
            <input type={showPwd ? 'text' : 'password'} value={form.confirmPassword} onChange={(e) => handleChange('confirmPassword', e.target.value)} className={inputCls} placeholder="请再次输入密码" />
          </div>
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">{t('register.phone')}</label>
          <div className="relative">
            <Phone size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
            <input value={form.phone} onChange={(e) => handleChange('phone', e.target.value)} className={inputCls} placeholder="请输入手机号" />
          </div>
        </div>
        <button type="submit" disabled={loading} className="h-10 w-full rounded-md bg-primary-500 text-sm font-medium text-white transition-colors hover:bg-primary-600 disabled:opacity-50">
          {loading ? '注册中...' : t('register.submit')}
        </button>
      </form>

      <p className="mt-4 text-center text-xs text-gray-400">
        <Link to="/login" className="text-primary-500 hover:underline">{t('register.toLogin')}</Link>
      </p>
    </div>
  );
}
