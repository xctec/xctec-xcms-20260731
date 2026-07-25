import { Construction } from 'lucide-react';

export default function Placeholder({ title }: { title: string }) {
  return (
    <div className="flex flex-col items-center justify-center py-20">
      <Construction size={48} className="text-gray-300" />
      <h2 className="mt-4 text-lg font-semibold text-gray-700">{title}</h2>
      <p className="mt-1 text-sm text-gray-400">该页面正在开发中...</p>
    </div>
  );
}
