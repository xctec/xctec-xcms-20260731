import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Mail, ArrowLeft, CheckCircle, KeyRound } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { toast } from '@/components/ui/Toast';

type Step = 'email' | 'verify' | 'done';

export default function ForgotPasswordPage() {
  const navigate = useNavigate();
  const [step, setStep] = useState<Step>('email');
  const [email, setEmail] = useState('');
  const [code, setCode] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const handleSendCode = (e: React.FormEvent) => {
    e.preventDefault();
    if (!email) { toast.error('请输入邮箱'); return; }
    toast.success('验证码已发送到您的邮箱');
    setStep('verify');
  };

  const handleReset = (e: React.FormEvent) => {
    e.preventDefault();
    if (!code) { toast.error('请输入验证码'); return; }
    if (newPassword.length < 8) { toast.error('密码长度至少 8 位'); return; }
    if (newPassword !== confirmPassword) { toast.error('两次密码不一致'); return; }
    toast.success('密码重置成功，请重新登录');
    setStep('done');
  };

  return (
    <div>
      <Link to="/login" className="mb-4 flex items-center gap-1 text-xs text-gray-400 hover:text-primary-500">
        <ArrowLeft size={14} /> 返回登录
      </Link>

      <h2 className="mb-1 text-xl font-bold text-gray-900">找回密码</h2>
      <p className="mb-6 text-sm text-gray-500">通过邮箱验证重置您的密码</p>

      {/* Steps indicator */}
      <div className="mb-6 flex items-center justify-center gap-2">
        {['发送验证码', '验证并重置', '完成'].map((s, i) => {
          const stepIndex = ['email', 'verify', 'done'].indexOf(step);
          const active = i <= stepIndex;
          return (
            <div key={s} className="flex items-center">
              <div className={`flex h-7 w-7 items-center justify-center rounded-full text-xs font-medium ${active ? 'bg-primary-500 text-white' : 'bg-gray-100 text-gray-400'}`}>
                {i + 1}
              </div>
              {i < 2 && <div className={`h-0.5 w-8 ${active && i < stepIndex ? 'bg-primary-500' : 'bg-gray-200'}`} />}
            </div>
          );
        })}
      </div>

      {step === 'email' && (
        <form onSubmit={handleSendCode} className="space-y-4">
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">注册邮箱</label>
            <div className="relative">
              <Mail size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
              <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="请输入注册邮箱" className="h-10 w-full rounded-md border border-gray-300 pl-10 pr-3 text-sm outline-none focus:border-primary-500 focus:ring-1 focus:ring-primary-500" />
            </div>
          </div>
          <button type="submit" className="h-10 w-full rounded-md bg-primary-500 text-sm font-medium text-white transition-colors hover:bg-primary-600">发送验证码</button>
        </form>
      )}

      {step === 'verify' && (
        <form onSubmit={handleReset} className="space-y-4">
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">验证码</label>
            <input value={code} onChange={(e) => setCode(e.target.value)} placeholder="请输入 6 位验证码" maxLength={6} className="h-10 w-full rounded-md border border-gray-300 px-3 text-center text-lg tracking-widest outline-none focus:border-primary-500 focus:ring-1 focus:ring-primary-500" />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">新密码</label>
            <div className="relative">
              <KeyRound size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
              <input type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} placeholder="至少 8 位，含大小写和数字" className="h-10 w-full rounded-md border border-gray-300 pl-10 pr-3 text-sm outline-none focus:border-primary-500 focus:ring-1 focus:ring-primary-500" />
            </div>
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">确认密码</label>
            <div className="relative">
              <KeyRound size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
              <input type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} placeholder="请再次输入新密码" className="h-10 w-full rounded-md border border-gray-300 pl-10 pr-3 text-sm outline-none focus:border-primary-500 focus:ring-1 focus:ring-primary-500" />
            </div>
          </div>
          <button type="submit" className="h-10 w-full rounded-md bg-primary-500 text-sm font-medium text-white transition-colors hover:bg-primary-600">重置密码</button>
        </form>
      )}

      {step === 'done' && (
        <div className="py-8 text-center">
          <CheckCircle size={48} className="mx-auto text-success-500" />
          <h3 className="mt-4 text-lg font-semibold text-gray-800">密码重置成功</h3>
          <p className="mt-2 text-sm text-gray-500">请使用新密码登录</p>
          <Link to="/login">
            <Button variant="primary" className="mt-6">前往登录</Button>
          </Link>
        </div>
      )}
    </div>
  );
}
